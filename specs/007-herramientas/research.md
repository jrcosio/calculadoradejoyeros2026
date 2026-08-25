# Research: Herramientas — precio de metales y peso de chapas

**Feature**: `007-herramientas` · **Fecha**: 2026-08-25

Es la primera feature del proyecto con **red, corrutinas y persistencia**, y la primera
que depende de un **proveedor externo**. Por eso, y a diferencia de la 004–006, se genera
este documento: recoge las decisiones técnicas que no estaban tomadas por precedente y las
alternativas descartadas. Las decisiones de producto (una feature, consulta directa,
precio medio, dos decimales, primera visita, orden y colores) se cerraron con el autor y
están en `spec.md` → Assumptions; aquí no se repiten.

## R1 — Contrato del proveedor

- **Decision**: Metal Sentinel vía RapidAPI, ruta `/metal-quote` con `symbol=` (confirmada, ver
  abajo), `currency=EUR`, una
  petición por metal (`AU`, `AG`, `CU`, `PD`, `RH`). Contrato en
  [contracts/metal-quote.md](./contracts/metal-quote.md), extraído del ejemplo real
  embebido en el código de la web del proveedor (no de la guía del encargo, que trabajaba
  con un `JsonObject` genérico porque no conocía los campos).
- **Rationale**: los campos reales (`ask`, `mid`, `bid`, `high`, `low`, `change`,
  `changePercentage`, `unit`, `timestamp`) coinciden uno a uno con el mockup, así que la
  pantalla se puede diseñar completa y los DTO se tipan desde el principio.
- **Alternatives considered**: (a) DTO genérico + parser defensivo de la guía (§12) —
  innecesario una vez conocida la respuesta; (b) `/api/gold-price` y `/api/silver-price`,
  que ya devuelven precios por quilate y ley — descartados: son solo para dos metales, y la
  app ya tiene su propio motor de leyes; (c) backend propio (guía §3.2) — decisión de
  producto: feature aparte.
- **Resultado (2026-08-25, con la credencial real)**: la ruta es **`/metal-quote`** (la
  `/api/metal-quote` de la web pública responde 404 para esta suscripción) y el parámetro es
  **`symbol`** (`metal=` devuelve un 200 con `{"error":"The symbol field is required."}`). Los
  cinco metales responden en EUR; oro, plata, paladio y rodio en `OUNCE` y **el cobre en
  `POUND`** (libra avoirdupois, 453,59237 g), que se incorpora como `UnidadPrecio.LIBRA` solo
  de origen. Respuesta anonimizada en `UI_Plantillas/Feature_Herramientas/respuesta_ejemplo_metal_quote.json`.
- **Riesgo cerrado**: el nombre del parámetro (`metal` vs `symbol`) se contradecía en la
  documentación pública. Se fija una sola constante y se confirma con un `curl` real al
  empezar la implementación (Paso 0); nunca fallback automático.

## R2 — Cliente HTTP

- **Decision**: `java.net.HttpURLConnection` del JDK (HTTPS lo impone la URL), detrás de una interfaz propia
  `ClienteHttp` (`get(url, cabeceras): RespuestaHttp(codigo, cuerpo)`), bloqueante a
  propósito; el salto a `dispatchers.io` lo hace el data source. Timeouts 10 s conexión /
  15 s lectura. Sin reintentos en el cliente.
- **Rationale**: 5 GET por hora, un host, tres cabeceras y ~1 KB de JSON. Cero dependencias
  nuevas (doctrina del proyecto: la 006 presumió de no añadir ninguna), cero reglas R8, y
  en Android `HttpURLConnection` está implementado sobre el OkHttp interno de la
  plataforma. La interfaz permite cambiar a OkHttp cuando llegue el backend o una segunda
  API sin tocar el data source. Test JVM con `com.sun.net.httpserver.HttpServer` del JDK 17.
- **Alternatives considered**: (a) **OkHttp** (un artefacto + okio, ~0,8 MB pre-R8,
  reglas de consumidor embebidas, `Call.cancel()` real, MockWebServer como dependencia de
  test) — no aporta nada que vayamos a usar (interceptores, pooling, caché HTTP) y obliga
  a verificar y mantener versión (memoria del proyecto: nunca copiar versiones de una
  guía; `maven-metadata.xml`); (b) **Retrofit + Gson** de la guía — dos dependencias más y
  un segundo parser JSON cuando `kotlinx-serialization` ya está; (c) **Ktor client** —
  lo mismo, más pesado.

## R3 — JSON

- **Decision**: `kotlinx-serialization-json` 1.9.0, **ya presente** con el plugin aplicado
  (las rutas type-safe lo usan). DTOs `@Serializable` con `ignoreUnknownKeys = true`
  declarando solo los campos usados. Serializador propio `BigDecimalExactoSerializer`
  que toma el literal del cable (`jsonPrimitive.content` → `BigDecimal(String)`).
- **Rationale**: regla del proyecto «`BigDecimal` desde literal `String`, nunca desde
  `Double`» aplicada a la red: `-45.30000000000018` se conserva exacto. Las reglas R8 de
  kotlinx van embebidas y el release ya prueba que `@Serializable` sobrevive (Routes).
- **Alternatives considered**: `Double.toString()` → `BigDecimal` (funciona, menos fiel);
  campos como `Double` en el DTO y conversión posterior (introduce coma flotante en el
  dominio, prohibido por doctrina).

## R4 — Persistencia de la caché

- **Decision**: `SharedPreferences` (fichero `cotizaciones`, **una única clave**
  `instantanea_json`) detrás de la interfaz `CotizacionesLocalDataSource`
  (`leer(): InstantaneaCotizaciones?`, `guardar(instantanea)`), con un
  `CodificadorInstantanea` Kotlin puro (DTOs `@Serializable` propios, `version = 1`,
  `BigDecimal` como `String`) testeable en JVM. Lectura y escritura (`commit()`) en
  `dispatchers.io`. El fichero se **excluye** de las reglas de backup.
- **Rationale**: se escribe como mucho una vez por hora, nadie lo observa (el repositorio
  es la única fuente y lo lee al entrar) y un blob único es atómico: los instantes viajan
  dentro del JSON, imposible un estado a medias entre claves. Cero dependencias. Una caché
  derivada no debe restaurarse en otro dispositivo con otra hora.
- **Alternatives considered**: (a) **DataStore Preferences** 1.2.1 (verificado en el
  índice de Google Maven; 1.3.0 solo alpha) — modelo reactivo `Flow` + `edit {}` que no
  usamos, más okio y proto; (b) **Room** — una tabla para cinco filas; (c) caché **solo en
  memoria** (la de la guía) — no cumple «por muchas veces que entres» cuando el proceso
  muere.

## R5 — Política de caché

- **Decision**: `PoliticaCacheCotizaciones(vigenciaMillis = 3 600 000, esperaReintentoMillis = 60 000, esperaTrasLimiteMillis = 300 000).decidir(guardada, ahora): DecisionCache`
  como **función pura** en `domain/model/`: `Servir` si los cinco metales tienen éxito
  vigente; `Esperar` si el último intento fue hace menos de la espera aplicable; si no,
  `Actualizar(pendientes)` con **solo** los metales sin precio vigente. Vigencia por metal
  (`obtenidoEn` dentro de cada éxito) + un único instante global de intento. Delta
  negativo (reloj atrasado) = no vigente. El repositorio envuelve toda la operación en un
  `Mutex` (single-flight) y consulta los pendientes en paralelo con `supervisorScope` +
  `async`/`awaitAll`, fusionando los resultados nuevos sobre los guardados (un error nuevo
  hereda el último dato conocido).
- **Rationale**: el test más importante de la feature (¿cuándo se toca la red?) no
  necesita corrutinas ni fakes. Un snapshot parcial no rehace los cuatro que salieron bien
  (ahorra cuota). Sin parámetro `forzar`: «Reintentar» solo aparece con errores y vuelve a
  pasar por la política. TTL de 1 h (encargo) frente a los 15 min de la guía: más
  protector con la cuota.
- **Alternatives considered**: (a) política dentro del repositorio (menos testeable);
  (b) TTL global único (un fallo de rodio obligaría a repetir los cinco); (c) `Flow` con
  `stateIn` — un consumidor y una lectura por entrada no lo justifican; (d) refresco
  forzado dentro de la hora — contradice el encargo.

## R6 — Credencial

- **Decision**: `RAPIDAPI_KEY` leída en `app/build.gradle.kts` con la API de `providers`
  (`environmentVariable` → `gradleProperty` → `fileContents(local.properties)`), escapada y
  volcada con `buildConfigField("String", "RAPIDAPI_KEY", …)`. Vacía → `logger.warn` y la
  app muestra «servicio no configurado» sin llamar a la red. `buildConfig = true` ya está
  activo. `local.properties` ya está en `.gitignore`.
- **Rationale**: `org.gradle.configuration-cache=true` exige leer ficheros por `providers`
  (se registran como entradas de configuración: cambiar el fichero invalida sin romper).
  La guía usa `Properties().load(File)`, incompatible con la caché de configuración.
- **Riesgo asumido** (decisión del autor): cualquier clave dentro de un APK es extraíble
  (guía §3). Prototipo; rotar la clave si se pegó en chats y poner backend antes de
  publicar.
- **Alternatives considered**: Android Keystore (no protege un secreto compartido por
  todas las instalaciones); backend propio (feature aparte).

## R7 — Fechas y horas

- **Decision**: instantes como `Long` epoch-millis en dominio **y en el UiState**
  (`ultimaConsultaEpochMillis`, `instanteMercadoEpochMillis`); la **vista** los formatea con
  `android.text.format.DateUtils.formatDateTime(context, millis, FORMAT_SHOW_DATE or FORMAT_ABBREV_MONTH or FORMAT_SHOW_YEAR)`
  + « · » + `formatDateTime(…, FORMAT_SHOW_TIME)` («25 ago 2026 · 10:33» en español), en un
  helper `@Composable` de `PresentacionPrecios.kt`.
- **Rationale**: `java.time` es **API 26+** y el proyecto es minSdk 24 sin desugaring
  (comprobado en `app/build.gradle.kts`); `DateUtils` es API 3, localizado por el sistema y
  usa la zona horaria del dispositivo. Es la **única** excepción a «el ViewModel formatea todo»:
  el formato de una fecha depende del idioma (FR-030) y el ViewModel no conoce recursos; los
  números no tienen ese problema. El test del ViewModel comprueba el `Long`.
- **Alternatives considered**: `Calendar` + meses abreviados hardcodeados en el ViewModel
  (rechazado en `/speckit-analyze`: texto visible no traducible, FR-030);
  `isCoreLibraryDesugaringEnabled` + `desugar_jdk_libs` (dependencia nueva y build más lenta
  para una sola fecha); `SimpleDateFormat` con `Locale("es")` (mismo problema de idioma
  fijado y meses con punto según el ICU).

## R8 — Koin: qué verifica `verify()`

- **Decision**: data sources registrados **concretos + `bind`**
  (`single { MetalSentinelDataSource(get(), get(), get()) } bind CotizacionesRemoteDataSource::class`),
  repositorio por interfaz (regla del proyecto), `Reloj` como tipo del grafo en
  `coreModule` (`single<Reloj> { RelojSistema() }`), configuración por defecto solo en
  clases registradas con lambda explícita.
- **Rationale**: leído en `Verification.kt` de `koin-test 4.2.2`: `verify()` inspecciona
  solo los constructores del **tipo primario** de cada definición (una interfaz no tiene
  constructor → `single<Interfaz> { Impl() }` no verifica nada de `Impl`), acepta
  parámetros `isOptional` y tiene `String/Int/Long/Double` en lista blanca. En cambio
  `viewModelOf`/`factoryOf`/`singleOf` resuelven **todos** los parámetros con `get()` en
  runtime ignorando defaults → un default de tipo no registrado pasa el test y falla al
  arrancar. Con `Reloj` en el grafo no dependemos de defaults.
- **Alternatives considered**: `reloj: Reloj = RelojSistema` por defecto (pasa `verify()`
  por `isOptional`, pero oculta la dependencia); lambda `() -> Long` (rompe `verify()`).

## R9 — Corrutinas en el ViewModel y sus tests

- **Decision**: `PreciosMetalesViewModel` lanza siempre con
  `viewModelScope.launch(dispatchers.main)` y recibe `DispatcherProvider` por
  constructor (constitución IV). Tests con un `TestDispatcherProvider(UnconfinedTestDispatcher())`
  donde `main = io = default`, `RelojFalso`, y fakes de repositorio/data sources en
  `app/src/test/`. Sin `Dispatchers.setMain` ni `MainDispatcherRule`.
- **Rationale**: en `lifecycle-viewmodel 2.11.0`, `viewModelScope` cae a
  `EmptyCoroutineContext` si `Dispatchers.Main` no existe; al pasar siempre el dispatcher
  inyectado, el test es determinista sin tocar el `Main` global. Es el primer código
  asíncrono de la app y fija el patrón.
- **Alternatives considered**: `MainDispatcherRule` con `setMain` (estándar, pero
  innecesario aquí y estado global); `runTest` + `advanceUntilIdle` con
  `StandardTestDispatcher` (válido; `Unconfined` simplifica los asserts inmediatos).

## R10 — `core/ui/UiState` genérico

- **Decision**: **no** se reutiliza; se deja intacto (candidato a borrarse en un `chore`
  aparte). `PreciosMetalesUiState` es un data class plano con `fase` (CARGANDO / LISTO /
  PARCIAL / ERROR) y los motivos de error como **enum** que la vista mapea a strings.
- **Rationale**: `UiState.Error(message: String)` obligaría al ViewModel a fabricar texto
  (viola «la vista mapea enum → recurso»); el estado real es un producto (filas + parciales
  + caché + selección + unidad), no una suma de tres casos; las cinco pantallas existentes
  usan data class plano.

## R11 — Motor de chapas

- **Decision**: `MaterialChapa` enum propio (8 materiales con `familia`, `milesimas`,
  `densidad` y `esSoloTecnica`) + `CalculoChapa` **sin ningún redondeo**
  (multiplicaciones exactas y `movePointLeft(3)` para el ÷ 1 000). Un **test de paridad**
  vigila que milésimas y `esSoloTecnica` coincidan con `LeyOro`/`LeyPlata`. Vista: peso 2
  decimales `HALF_UP` (documento §7/§21), volumen y metal fino 3, pureza 1.
- **Rationale**: doctrina «cada motor es fiel a su documento»; la densidad es dato de
  este documento y §19 prevé densidades por color que una ley no puede expresar. Sin
  división no hay `ESCALA` que fijar (llegará con los inversos, fuera de alcance).
- **Alternatives considered**: reutilizar `LeyOro`/`LeyPlata` con un mapa de densidades
  aparte (acopla dos documentos y complica la extensión por color); adoptar el
  `SheetWeightCalculator` en inglés del documento (rompe la nomenclatura en español y
  redondea el volumen a escala 12 sin necesidad).

## R12 — Ilustración de la chapa

- **Decision**: `Canvas` propio (`DibujoChapa`) con proyección oblicua tipo *cabinet*
  (`P(x,y,z) = (ox + s·(x + 0,433·z), oy − s·(y + 0,25·z))`), proporciones normalizadas
  (`sqrt` para ancho/largo, `cbrt` para espesor, con topes), tres caras + contorno + cotas
  discontinuas con flechas y etiquetas (`drawText`), animación de proporciones con
  `animateFloatAsState(spring)` y construcción inicial con `Animatable` + `PathMeasure`
  (contorno → caras → cotas, 900 ms). `Modifier.drawWithCache`, lecturas de valores
  animados solo en `onDrawBehind`, `LocalInspectionMode` para las previews.
- **Rationale**: el autor pide expresamente el efecto «se va construyendo»; una sola
  vista isométrica basta (sin chip de vista). `ui-graphics`/`ui-text` ya están en el
  classpath vía BOM. Las proporciones dibujadas **nunca** entran en el cálculo (spec
  FR-024).
- **Alternatives considered**: `chapa.png` estático (último recurso, según el autor);
  `Image` con `graphicsLayer` escalado (no dibuja cotas ni cambia proporciones por eje);
  proyección isométrica pura a 30°/30° (menos parecida al mockup y a `chapa.png`).

## R13 — Componentes compartidos

- **Decision**: `OpcionSegmento` gana `iconRes: Int? = null` (icono en el hueco del check);
  `Formularios.kt` gana `CampoMedida(etiqueta, valor, onCambio, iconRes, unidad, acento,
  borde, error, imeAction)` y un `MarcoCampo` privado que comparte con `CampoCantidad`.
  `core/util/Decimales.kt` promueve `parsearDecimalPositivo` (quinto consumidor).
- **Rationale**: regla «en cuanto un segundo consumidor lo pide, deja de ser privado».
  Los defaults dejan oro, plata y soldaduras byte a byte iguales (sus androidTest hacen de
  regresión). Tres campos de 34 sp sin etiqueta (el `CampoCantidad` actual) son
  demasiado pesados para ancho/espesor/largo.
- **Alternatives considered**: un selector nuevo con iconos (duplica la píldora);
  `CampoCantidad` con parámetro de sufijo (sigue sin etiqueta ni icono).

## R14 — Formato de precios

- **Decision**: importes ≥ 1 → 2 decimales, < 1 → 4; separador de miles con punto; coma
  decimal; `HALF_UP`; porcentaje siempre 2 decimales con signo; variación con signo. Todo
  en `FormatoPrecios` (`internal object`, solo JDK), sin `NumberFormat`.
- **Rationale**: el cobre por gramo ronda 0,0089 € (dos decimales serían «0,01»); el kilo
  de oro ronda 148 000 € (primera cifra de seis dígitos de la app). Sin `Locale`: el resto
  de la app tampoco lo usa y el formato es determinista.
- **Alternatives considered**: `NumberFormat.getCurrencyInstance(es-ES)` de la guía
  (depende del ICU del dispositivo y estrena `Locale` en el proyecto).

## R15 — Armazón: una ruta, tres ViewModels

- **Decision**: `Route.Herramientas` única; `HerramientasViewModel` (sub-herramienta +
  `screen_view "herramientas"`), `PreciosMetalesViewModel` y `PesoChapasViewModel`
  resueltos perezosamente con `koinViewModel()` dentro de su sección; `HerramientasContent`
  con dos *slots* `@Composable` para las secciones; secciones sin scaffold/scroll/padding
  propios.
- **Rationale**: el `ViewModelStoreOwner` es la `NavBackStackEntry` de la ruta → los tres
  comparten su vida (el estado sobrevive al cambio de sub-herramienta) y la API no se toca
  hasta abrir «Precio metales». Un ViewModel único mezclaría red con una calculadora pura
  y acoplaría dos motores.
- **Alternatives considered**: dos rutas (pierde el selector persistente del mockup); un
  ViewModel gordo; sub-grafo de navegación (innecesario sin argumentos).

## R16 — Cuota y alcance

- **Decision**: integración directa **de prototipo**, con caché de 1 h persistida, sin
  refresco forzado y con esperas entre reintentos. Anotado en spec, plan, `CLAUDE.md` y en la
  nota de la pantalla («Precios orientativos»).
- **Rationale**: 15 000 peticiones/mes ÷ 5 = 3 000 cargas/mes para **todos** los usuarios;
  100 usuarios que entren tres veces al día la agotan en 10 días. Decisión de producto
  confirmada con el autor; el backend con caché compartida es una feature aparte.
