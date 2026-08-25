# Implementation Plan: Herramientas — precio de metales y peso de chapas

**Branch**: `007-herramientas` | **Date**: 2026-08-25 | **Spec**: [spec.md](./spec.md)

## Summary

`Route.Herramientas` deja de caer en `PlaceholderScreen` y pasa al módulo real: un selector
de dos sub-herramientas —PRECIO METALES y PESO DE CHAPAS— que en la primera visita se muestra
solo, con una invitación a elegir, y debajo el contenido de la elegida.

**PRECIO METALES** consulta a Metal Sentinel (RapidAPI) la cotización en euros de oro, plata,
cobre, paladio y rodio —una petición por metal, en paralelo—, la muestra por gramo, kilo u
onza troy con flecha de tendencia, ofrece la tarjeta «Información del mercado» del metal
pulsado (ask, bid, máximo, mínimo, variación, variación %, unidad, hora local) y **conserva
los precios una hora también tras cerrar la app**: mientras un metal tenga precio vigente no
se vuelve a consultar. Los fallos son por metal (los demás se muestran; el fallido enseña su
motivo y su último dato conocido), «Reintentar» solo aparece con errores y respeta una espera
(1 min; 5 tras 429), y una build sin credencial muestra «servicio no configurado».

**PESO DE CHAPAS** calcula el peso de una chapa rectangular
(`ancho × largo × espesor × densidad / 1000`) para ocho materiales (oro 18K/14K/12K/9K, plata
950/925/900/800) con cálculo automático al completar las tres medidas, y una ilustración
isométrica dibujada en la app que se construye al entrar y se redibuja con cada medida, en el
color del metal. Botones Limpiar y Guardar en favoritos como en las otras calculadoras.

Es la **primera feature con red, corrutinas y persistencia** del proyecto y la que estrena
`DispatcherProvider`, y lo hace **sin ninguna dependencia nueva**: `HttpURLConnection` tras
una interfaz propia, `SharedPreferences` con un único blob JSON tras otra, y la
`kotlinx-serialization` que ya usaban las rutas. Estrena también el **cuarto motor** de
`domain/` (`MaterialChapa` + `CalculoChapa`, sin un solo redondeo) y el primer `Canvas` de la
app. La lógica de chapas es transcripción del documento técnico
`UI_Plantillas/Feature_Herramientas/Especificacion_Calculadora_Peso_Chapas_Oro_Plata.md`
(§ citados desde aquí), que **prevalece sobre el mockup**; el contrato del proveedor está en
[contracts/metal-quote.md](./contracts/metal-quote.md) y las decisiones técnicas razonadas en
[research.md](./research.md).

## Technical Context

**Language/Version**: Kotlin 2.2.10 (el que fija AGP 9.3.1) · Java 17

**Primary Dependencies**: Compose BOM 2026.08.00, Material 3, Navigation Compose con rutas
`@Serializable`, Koin 4.2.2, Firebase BoM (Analytics + Crashlytics),
`kotlinx-serialization-json` 1.9.0 (ya presente; ahora también para los DTO del proveedor),
`kotlinx-coroutines` 1.11.0; del JDK: `java.math.BigDecimal`, `java.net.HttpURLConnection`; del
SDK: `android.text.format.DateUtils` para las fechas en la vista

**Storage**: `SharedPreferences` (fichero `cotizaciones`, una clave `instantanea_json`) para la
instantánea de cotizaciones; excluido de backup. Nada más se persiste

**Testing**: JUnit4 + MockK + Turbine + `kotlinx-coroutines-test` (JVM), con los primeros
`TestDispatcherProvider`, `RelojFalso` y fakes del proyecto; `com.sun.net.httpserver.HttpServer`
del JDK para el cliente HTTP; `compose-ui-test-junit4` (instrumentado)

**Target Platform**: Android, minSdk 24 / targetSdk 36 / compileSdk 37, solo vertical; **sin
`java.time`** (API 26+) ni desugaring: las fechas viajan como `Long` y las formatea `DateUtils`

**Project Type**: app Android de un solo módulo (`:app`), MVVM `ui → domain ← data`

**Performance Goals**: precios visibles en < 1 s desde caché y en el tiempo de red del proveedor
(< 1 s de media según el proveedor) en la primera carga; recálculo de chapa por pulsación de
tecla sin jank; ilustración a 60 fps invalidando solo la fase de dibujo

**Constraints**: sin dependencias nuevas; sin `material-icons`; credencial fuera del repositorio
y de los registros; **una carga completa = 5 peticiones** y nunca más de una carga por hora y
metal; `BigDecimal` desde literales, sin redondeos intermedios; textos traducibles; los valores
dibujados jamás alimentan el cálculo (FR-024)

**Scale/Scope**: una pantalla con dos secciones y tres ViewModels, un motor nuevo (2 modelos,
1 caso de uso), la primera capa de red y caché (≈ 14 tipos de dominio, 9 de datos, 3 casos de
uso), 2 componentes compartidos ampliados, 1 bitmap y 5 iconos vectoriales nuevos, 1
`buildConfigField`, 1 permiso

**Esta feature no añade ninguna dependencia.** Los recursos nuevos son `rodio.png` (del encargo,
redimensionado a 512 px como los demás bitmaps de metal) y cinco vectores dibujados a mano
(`ic_grafica`, `ic_capas`, `ic_ancho`, `ic_espesor`, `ic_regla`). Oro, plata, cobre y paladio
reutilizan `modulo_oro.png`, `modulo_plata.png`, `cobre.png` y `paladio.png`; `ic_balanza`,
`ic_info`, `ic_lingotes`, `ic_refrescar`, `ic_estrella`, `ic_chevron` y `ic_aviso` ya existen.

### Decisiones y sus porqués

| Decisión | Motivo |
|---|---|
| Cliente HTTP = `java.net.HttpURLConnection` tras `ClienteHttp` (`data/source/remote`), sin OkHttp/Retrofit | 5 GET por hora, un host, ~1 KB. Cero dependencias, cero reglas R8; en Android va sobre el OkHttp interno. La interfaz permite cambiar de cliente cuando llegue el backend. Test JVM con el `HttpServer` del JDK (R2) |
| JSON con `kotlinx-serialization` ya presente; `BigDecimalExactoSerializer` toma el literal del cable | Regla «`BigDecimal` desde `String`, nunca desde `Double`» aplicada a la red; los campos no usados no se declaran (`ignoreUnknownKeys`) (R3) |
| Persistencia = `SharedPreferences`, **una clave** con el JSON de la instantánea, tras `CotizacionesLocalDataSource`; `CodificadorInstantanea` puro | Una escritura por hora, nadie observa la caché, blob único = atómico (los instantes viajan dentro). Cero dependencias frente a DataStore. Excluido de backup: una caché no se restaura en otro dispositivo (R4) |
| `PoliticaCacheCotizaciones.decidir(guardada, ahora)` como **función pura** en `domain/model/`: `Servir` / `Esperar` / `Actualizar(pendientes)` | El test más importante (¿cuándo se toca la red?) no necesita corrutinas. Vigencia **por metal** (`obtenidoEn` de cada éxito) + un instante global de intento; solo se consultan los metales sin precio vigente; reloj atrasado = no vigente (R5) |
| TTL 1 h (encargo), no los 15 min de la guía; esperas de reintento 60 s / 300 s tras 429; sin parámetro `forzar` | Protege una cuota compartida por todos los usuarios (3 000 cargas/mes). «Reintentar» solo aparece con errores y vuelve a pasar por la política; un snapshot completo y vigente bloquea la red siempre (R5, R16) |
| `CotizacionesRepositoryImpl` con `Mutex` alrededor de toda la operación (single-flight) y `supervisorScope` + `async`/`awaitAll` para los pendientes | Dos llamadas concurrentes → una sola ronda de red; un fallo de rodio no tumba a los otros cuatro; solo se captura `MetalSentinelException` (una excepción inesperada es un bug y sube al ViewModel) |
| `CotizacionesRepository.obtenerCotizaciones()` **suspend**, no `Flow` | Un consumidor, una lectura por entrada, sin push ni actualización en segundo plano (R5) |
| `Reloj` en `core/util/` registrado en `coreModule`; data sources registrados **concretos + `bind`**; repositorio por interfaz | `verify()` de Koin 4.2.2 solo inspecciona el tipo primario: con `bind` sí verifica los constructores de los data sources; `Reloj` en el grafo evita depender de defaults que `viewModelOf`/`factoryOf` ignoran en runtime (R8) |
| `RAPIDAPI_KEY`: `providers.environmentVariable` → `gradleProperty` → `fileContents(local.properties)` → `buildConfigField`; vacía = aviso de build y estado «no configurado» | Compatible con `org.gradle.configuration-cache=true` (la guía usaba `Properties().load(File)`, que no lo es). Riesgo asumido por el autor: clave extraíble del APK, prototipo (R6) |
| Fechas: `Long` epoch-millis en dominio **y en el UiState**; la vista las formatea con `android.text.format.DateUtils` | `java.time` es API 26+ (minSdk 24 sin desugaring) y unos meses abreviados hardcodeados en el ViewModel romperían FR-030: el formato de fecha depende del idioma y lo pone el sistema (R7) |
| `PreciosMetalesViewModel` lanza siempre con `viewModelScope.launch(dispatchers.main)`; tests con `TestDispatcherProvider` sin `setMain` | Constitución IV. `viewModelScope` en lifecycle 2.11 cae a `EmptyCoroutineContext` sin `Main`: pasando el dispatcher inyectado el test es determinista (R9) |
| `core/ui/UiState` genérico **no se reutiliza**; `PreciosMetalesUiState` plano con `fase` y motivos de error como enum | `Error(message: String)` obligaría al VM a fabricar texto; el estado es un producto, no una suma de tres casos; las cinco pantallas existentes usan data class plano (R10) |
| Precio principal = `mid`, fallback `ask`, luego `bid`; `open`/`close` ignorados | Decisión del autor; en la muestra real `open`/`close` llegan a 0 |
| Formato de precios: ≥ 1 → 2 decimales, < 1 → 4; miles con punto; % siempre 2 con signo; `HALF_UP` | Cobre por gramo ≈ 0,0089 €; kilo de oro ≈ 148 000 €. Primera cifra de seis dígitos de la app; solo precios llevan miles (R14) |
| Conversión de unidades en dominio (`ConversorUnidadesPrecio`, `GRAMOS_POR_ONZA_TROY = "31.1034768"`, `ESCALA = 10`, una sola división `HALF_UP`) partiendo **siempre** del valor del proveedor | Criterio de los otros motores; nunca se convierte desde una cifra ya redondeada (FR-009). Unidad de origen desconocida → `unidadOrigen = null` y se muestra sin convertir (FR-010) |
| `MaterialChapa` enum propio (8 materiales con densidad) + `CalculoChapa` **sin ningún redondeo** (`movePointLeft(3)`); test de paridad con `LeyOro`/`LeyPlata` | Doctrina «cada motor es fiel a su documento»; §19 prevé densidades por color que una ley no expresa. Sin división no hay `ESCALA` que fijar (R11) |
| Vista de chapas: peso 2 decimales `HALF_UP`, volumen y metal fino 3, pureza 1, densidad literal | Documento §7/§21 y decisión del autor: densidades orientativas → un tercer decimal sería precisión aparente. **Cuarta política de redondeo** de la app, documentada en `CLAUDE.md` («no las unifiques») |
| Límites operativos (§11.4) en el ViewModel, no en el motor | Son controles de interfaz; el motor solo exige medidas > 0 |
| Una ruta, **tres ViewModels** (`HerramientasViewModel`, `PreciosMetalesViewModel`, `PesoChapasViewModel`) resueltos perezosamente con `koinViewModel()` dentro de cada sección; `HerramientasContent` con dos *slots* | El `ViewModelStoreOwner` es la `NavBackStackEntry`: el estado sobrevive al cambio de sub-herramienta y la API no se toca hasta abrir PRECIO METALES. Los slots hacen el armazón testeable con marcadores (R15) |
| `subherramienta: Subherramienta? = null` en el estado inicial, con tarjeta «Elige una herramienta» | Decisión del autor (mockup preselecciona precios y no se sigue); patrón `familia = null` de soldaduras; `SelectorSegmentado` con `seleccionada = -1` |
| Secciones sin scaffold, scroll, `imePadding` ni padding exterior | Los pone el armazón una sola vez; cada sección pinta una `Column(spacedBy(Md))` y tiene su `@Preview` |
| `DibujoChapa` en `Canvas` con proyección oblicua *cabinet*, proporciones normalizadas (`sqrt`/`cbrt` con topes), `drawWithCache`, `animateFloatAsState(spring)` + `Animatable` con `PathMeasure` para la construcción | Efecto «se va construyendo» pedido por el autor; legible con proporciones extremas; invalida solo el dibujo. `chapa.png` no se importa: último recurso (R12) |
| Orden de secciones de chapas el del mockup (ilustración, material, medidas, resultado, botones); acentos ORO dorado / PLATA teal | Decisiones del autor. Cambiar de familia conserva las medidas: la geometría no depende del metal |
| `OpcionSegmento.iconRes: Int? = null`; `CampoMedida` + `MarcoCampo` privado en `Formularios.kt`; `core/util/Decimales.kt` | Regla del segundo consumidor. Defaults neutros: oro, plata y soldaduras no cambian un píxel (R13) |
| Telemetría: pantalla `"herramientas"` (nombre del placeholder) desde el armazón; `"herramientas_precios"` y `"herramientas_chapas"` desde las secciones; eventos `herramientas_*` con params de enum; `recordError` estrenado para causas inesperadas | FR-028. Conserva la serie histórica del placeholder; cada sub-herramienta estrena la suya. Dedupe del cálculo de chapa por material (patrón plata); sin dedupe en unidad/metal (acciones, no tecleo) |
| Strings nuevos en bloques `herramientas_*`, `precios_*`, `chapas_*`; metales nuevos (`metal_oro`, `metal_plata`, `metal_rodio`, `metal_oro_fino` + `_imagen`) al bloque compartido | Precedente 005/006: los nombres de material son compartidos, los textos de la feature llevan su prefijo. Se reutilizan `oro_ley_*`, `plata_ley_*`, `oro_aviso_12k`, `plata_aviso_*`, `accion_*`, `aviso_proximamente`, `unidad_gramos` |

## Constitution Check

*GATE: revisado antes de la Fase 0 y de nuevo tras el diseño. Sin violaciones.*

| Principio | Cumplimiento |
|---|---|
| I. SDD obligatorio | spec → plan → tasks → implement. Ningún fichero de producto se toca antes de `tasks.md` aprobado. Las cuatro decisiones de alcance se cerraron con el autor antes de la spec (Assumptions); el contrato del proveedor se confirma con la credencial real como primera tarea, y si difiriera se actualizaría `contracts/` antes de escribir el cliente |
| II. MVVM con capas estancas | `domain/` sigue siendo Kotlin puro: los nuevos tipos usan solo `java.math` y `kotlinx.coroutines` (`suspend` en la interfaz del repositorio). `data/` implementa `CotizacionesRepository` y confina HTTP, JSON del proveedor y `SharedPreferences` en `data/source/`. Las fechas viajan como `Long` y las formatea la vista con `DateUtils` (dependen del idioma). Los tres ViewModels exponen un único `StateFlow`, no importan Compose y reciben todo por constructor; `PreciosMetalesViewModel` recibe `DispatcherProvider`. Cada sección se parte en resolutor + `*Content` sin estado con `@Preview`; los mapeos enum → recurso viven en `Presentacion*.kt` internos al paquete |
| III. DI solo por Koin | `coreModule` + `Reloj`; `dataModule` + `ClienteHttp`, dos data sources (concretos + `bind`) y el repositorio **por su interfaz**; `domainModule` + 3 `factoryOf`; `viewModelModule` + 3 `viewModelOf`. Sin módulo nuevo, sin `get()` interno, sin primitivos en el grafo (la credencial entra por defecto de constructor en una definición con lambda explícita) |
| IV. Test obligatorio | Test por caso de uso (3: `ObtenerCotizaciones`, `ConvertirCotizacion`, `CalcularPesoChapa`) y por ViewModel (3); además política de caché, conversor, instantánea, codificador, data source remoto con la muestra real, cliente HTTP, repositorio (caché, parcial, espera, muerte de proceso, single-flight), formato y proporciones. Corrutinas con `DispatcherProvider` inyectado y `TestDispatcher`; `Dispatchers.IO` no aparece fuera de `DefaultDispatcherProvider`. `KoinModulesTest` cubre los registros nuevos sin tocarlo |
| V. Versiones en `libs.versions.toml` | No se añaden dependencias ni versiones |

Restricciones técnicas: `java.time` evitado (minSdk 24); ningún producto nuevo de Firebase
(`recordError` ya existía en la interfaz); `google-services.json` intacto; nombres en español.

## Project Structure

### Documentation (this feature)

```text
specs/007-herramientas/
├── spec.md
├── plan.md              # este fichero
├── research.md          # decisiones técnicas razonadas (R1–R16)
├── data-model.md        # entidades de precios, caché y chapas (Fase 1)
├── quickstart.md        # guía de validación: credencial, curl, gradle, emulador
├── contracts/
│   └── metal-quote.md   # contrato del proveedor + verificación
├── tasks.md             # salida de /speckit-tasks
└── checklists/
    └── requirements.md
```

A diferencia de la 004–006 sí se generan `research.md`, `contracts/` y `quickstart.md`: es la
primera feature con proveedor externo, cliente HTTP, persistencia y corrutinas, y su
validación exige pasos fuera de la app (credencial, `curl`, panel de RapidAPI, modo avión).

### Source Code (repository root)

```text
app/
├── build.gradle.kts                          (M) RAPIDAPI_KEY vía providers → buildConfigField
└── src/main/
    ├── AndroidManifest.xml                   (M) + uses-permission INTERNET
    ├── java/com/jrblanco/calculadoradejoyeros2021/
    │   ├── core/
    │   │   ├── util/Reloj.kt                 (N) interface Reloj { ahoraMillis() } + RelojSistema
    │   │   ├── util/Decimales.kt             (N) parsearDecimalPositivo(texto): BigDecimal?
    │   │   └── di/
    │   │       ├── CoreModule.kt             (M) + single<Reloj>
    │   │       ├── DataModule.kt             (M) + ClienteHttp, 2 data sources (bind), repositorio
    │   │       ├── DomainModule.kt           (M) + 3 factoryOf
    │   │       └── ViewModelModule.kt        (M) + 3 viewModelOf
    │   ├── domain/
    │   │   ├── model/
    │   │   │   ├── MetalCotizado.kt          (N) ORO(AU)…RODIO(RH), orden de pintado, analyticsId
    │   │   │   ├── UnidadPrecio.kt           (N) GRAMO / KILO / ONZA_TROY
    │   │   │   ├── Tendencia.kt              (N) SUBE / BAJA / PLANA + de(variacion)
    │   │   │   ├── OrigenDatos.kt            (N) RED / CACHE / CACHE_EN_ESPERA
    │   │   │   ├── MotivoErrorCotizacion.kt  (N) SIN_CREDENCIAL … DESCONOCIDO
    │   │   │   ├── CotizacionMetal.kt        (N) ask/bid/mid/…/unidadOrigen?/instantes + precioPrincipal
    │   │   │   ├── ResultadoCotizacion.kt    (N) sealed Exito / Error(motivo, ultimaConocida?, causa?)
    │   │   │   ├── InstantaneaCotizaciones.kt(N) resultados + instanteIntento + origen; esVigente, fusionarCon
    │   │   │   ├── PoliticaCacheCotizaciones.kt (N) decidir(guardada, ahora): Servir/Esperar/Actualizar
    │   │   │   ├── ConversorUnidadesPrecio.kt(N) GRAMOS_POR_ONZA_TROY, GRAMOS_POR_KILO, ESCALA; convertir
    │   │   │   ├── MaterialChapa.kt          (N) FamiliaChapa + 8 materiales con densidad (§5.1)
    │   │   │   └── CalculoChapa.kt           (N) MM3_POR_CM3; de(…) sin redondeo (§4, §8.1, §8.2)
    │   │   ├── repository/
    │   │   │   └── CotizacionesRepository.kt (N) suspend fun obtenerCotizaciones()
    │   │   └── usecase/
    │   │       ├── ObtenerCotizacionesUseCase.kt   (N) suspend; delega en el repositorio
    │   │       ├── ConvertirCotizacionUseCase.kt   (N) convierte todos los importes, no el %
    │   │       └── CalcularPesoChapaUseCase.kt     (N) require(> 0) ×3 → CalculoChapa.de
    │   ├── data/
    │   │   ├── source/remote/
    │   │   │   ├── ClienteHttp.kt            (N) interface + RespuestaHttp(codigo, cuerpo)
    │   │   │   ├── ClienteHttpUrlConnection.kt (N) HttpURLConnection, timeouts 10/15 s
    │   │   │   ├── MetalSentinelDto.kt       (N) @Serializable respuesta + cotización
    │   │   │   ├── BigDecimalExactoSerializer.kt (N) literal del cable → BigDecimal(String)
    │   │   │   ├── MetalSentinelException.kt (N) motivo + mensaje sin credencial
    │   │   │   ├── CotizacionesRemoteDataSource.kt (N) interface obtener(metal)
    │   │   │   └── MetalSentinelDataSource.kt(N) URL, PARAMETRO_METAL, cabeceras, mapeo de errores y unidad
    │   │   ├── source/local/
    │   │   │   ├── CotizacionesLocalDataSource.kt (N) interface leer()/guardar()
    │   │   │   ├── InstantaneaPersistidaDto.kt    (N) @Serializable version=1, BigDecimal como String
    │   │   │   ├── CodificadorInstantanea.kt      (N) codificar/decodificar (puro)
    │   │   │   └── SharedPreferencesCotizacionesLocalDataSource.kt (N) fichero «cotizaciones», clave única
    │   │   └── repository/
    │   │       └── CotizacionesRepositoryImpl.kt (N) Mutex + política + supervisorScope + fusión + persistencia
    │   └── ui/
    │       ├── components/
    │       │   ├── SelectorSegmentado.kt     (M) OpcionSegmento.iconRes: Int? = null
    │       │   └── Formularios.kt            (M) + CampoMedida; MarcoCampo privado compartido
    │       ├── navigation/AppNavHost.kt      (M) Route.Herramientas → HerramientasScreen
    │       └── herramientas/                 (N) paquete de la feature, una ruta, tres ViewModels
    │           ├── HerramientasUiState.kt    (N) Subherramienta? = null
    │           ├── HerramientasViewModel.kt  (N) selección + screen_view "herramientas"
    │           ├── HerramientasScreen.kt     (N) Screen + Content(slots) + TarjetaPrimeraVisita + previews
    │           ├── PresentacionHerramientas.kt (N) Subherramienta → etiqueta/icono
    │           ├── precios/
    │           │   ├── PreciosMetalesUiState.kt (N) fase, filas, unidad, seleccionado, detalle, error…
    │           │   ├── FormatoPrecios.kt     (N) importes 2/4 + miles, %, variación (sin fechas)
    │           │   ├── PreciosMetalesViewModel.kt (N) carga en init, derivación, reintento, telemetría
    │           │   ├── PreciosMetalesSection.kt (N) koinViewModel + Content
    │           │   ├── PreciosMetalesContent.kt (N) lista, selector de unidad, tarjeta de mercado, estados
    │           │   └── PresentacionPrecios.kt (N) metal → nombre/imagen; unidad → etiqueta; motivo → mensaje; tendencia → color; fechaHoraLocal (DateUtils)
    │           └── chapas/
    │               ├── PesoChapasUiState.kt  (N) MedidaChapa, medidas, fueraDeRango, dibujo, resultado
    │               ├── ProporcionesChapa.kt  (N) normalización pura de las tres medidas
    │               ├── PesoChapasViewModel.kt(N) recálculo automático, límites, dibujo, telemetría
    │               ├── PesoChapasSection.kt  (N) koinViewModel + Toast + Content
    │               ├── PesoChapasContent.kt  (N) ilustración, material, medidas, resultado, botones
    │               ├── DibujoChapa.kt        (N) Canvas: proyección, caras, cotas, animaciones
    │               └── PresentacionChapas.kt (N) familia → acento/etiqueta; material → etiqueta/aviso; medida → etiqueta/icono
    └── res/
        ├── values/strings.xml                (M) metal_oro/plata/rodio/oro_fino (+_imagen), unidad_*,
        │                                         bloques herramientas_*, precios_*, chapas_*
        ├── drawable/                         (N) ic_grafica, ic_capas, ic_ancho, ic_espesor, ic_regla
        ├── drawable-nodpi/rodio.png          (N) desde UI_Plantillas, 512 px
        └── xml/backup_rules.xml, data_extraction_rules.xml (M) exclude sharedpref cotizaciones.xml

app/src/test/.../core/util/TestDispatcherProvider.kt, RelojFalso.kt, DecimalesTest.kt        (N)
app/src/test/.../domain/model/{ConversorUnidadesPrecio,CotizacionMetal,InstantaneaCotizaciones,
                                PoliticaCacheCotizaciones,ProporcionesChapa*}Test.kt          (N)
app/src/test/.../domain/usecase/{ConvertirCotizacion,CalcularPesoChapa}UseCaseTest.kt         (N)
app/src/test/.../data/source/remote/{MetalSentinelDataSource,ClienteHttpUrlConnection}Test.kt (N)
app/src/test/.../data/source/local/CodificadorInstantaneaTest.kt                              (N)
app/src/test/.../data/repository/CotizacionesRepositoryImplTest.kt (+ fakes en data/)         (N)
app/src/test/.../ui/herramientas/{HerramientasViewModel,precios/FormatoPrecios,
                                   precios/PreciosMetalesViewModel,chapas/PesoChapasViewModel}Test.kt (N)
app/src/androidTest/.../ui/herramientas/{Herramientas,precios/PreciosMetales,chapas/PesoChapas}ScreenTest.kt (N)

CLAUDE.md                                     (M) destinos pendientes → dos; cuarto motor; capa de red y caché;
                                                  cuarta política de redondeo; ui/herramientas como sexto ejemplo;
                                                  componentes y utilidades nuevos; nota de verify()
```

`*ProporcionesChapaTest` vive junto a su clase en `ui/herramientas/chapas/` (es presentación,
no dominio).

**Structure Decision**: se mantiene el módulo único `:app` con `ui → domain ← data`. La feature
añade a `domain/` el cuarto motor y el primer contrato asíncrono (`CotizacionesRepository`), a
`data/` las primeras fuentes remota y local reales junto a la de Firebase, y a `ui/` un paquete
de pantalla que por primera vez tiene **sub-paquetes** (`precios/`, `chapas/`): una ruta, tres
ViewModels, dos secciones con contrato común. `core/util/` gana `Reloj` (hermano de
`DispatcherProvider`) y `Decimales`.

## Complexity Tracking

Cero dependencias nuevas y ninguna violación de la constitución. Cinco puntos merecen
declaración expresa.

| Divergencia | Por qué es necesaria | Alternativa más simple, y por qué se rechaza |
|---|---|---|
| Primer código de red y de persistencia del proyecto, escrito a mano sobre el JDK y el SDK (`HttpURLConnection`, `SharedPreferences`) | El encargo exige consultar a un proveedor y conservar una hora los datos aunque se cierre la app. Cinco GET por hora y un blob por hora no justifican OkHttp ni DataStore; ambos quedan detrás de interfaces propias para poder cambiarlos | Añadir OkHttp + DataStore «porque es lo estándar». Se rechaza: dos dependencias (y sus versiones a vigilar) para capacidades que no se usan; el proyecto ha hecho bandera de no añadir dependencias sin segundo consumidor |
| La credencial del proveedor viaja en el APK (`BuildConfig`) | El autor decide consultar directamente desde la app como prototipo; no existe backend | Backend propio con caché compartida (guía §3.2). Es la solución correcta para una app pública y queda como feature aparte; mientras, la caché de 1 h, las esperas de reintento y la nota de la pantalla acotan el consumo |
| Paquete de pantalla con **sub-paquetes** y **tres ViewModels** bajo una sola ruta | Dos herramientas independientes (una con red, otra pura) comparten pantalla y selector; separarlas mantiene cada ViewModel pequeño y testeable y evita que la API se consulte al abrir chapas | Un ViewModel único (mezcla red y cálculo puro, acopla dos motores) o dos rutas (pierde el selector persistente del mockup) |
| **Cuarta política de redondeo de vista** (chapas 2 decimales `HALF_UP`; precios 2/4 con miles) junto a oro/soldaduras (3 `HALF_UP`) y plata (3 `DOWN`) | El documento de chapas fija 2 decimales y el autor lo confirma; los precios necesitan 4 decimales por debajo de 1 € y miles por encima de 1 000 € | Unificar a 3 decimales. Se rechaza: contradice el documento técnico (caso obligatorio «1,56 g»), da precisión aparente sobre densidades orientativas y no sirve para el cobre ni para el kilo |
| Se modifican dos componentes compartidos (`SelectorSegmentado`, `Formularios.kt`) consumidos por oro, plata y soldaduras | El selector de sub-herramientas lleva icono (mockup) y las medidas necesitan un campo con etiqueta, icono y unidad | Componentes paralelos (duplican píldora y marco). Se rechazan; los cambios son parámetros opcionales con default neutro y la puerta de regresión (`testDebugUnitTest` + `assembleDebug` + androidTest de oro/plata/soldaduras) va antes de las historias |
