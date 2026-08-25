# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

App Android (Kotlin + Jetpack Compose) de cálculo de precios para joyeros.

## Regla número uno: SDD obligatorio

Este proyecto usa **Spec-Driven Development con GitHub Spec Kit**. Toda feature
recorre el ciclo completo antes de tocar código de producto:

```
/speckit-specify  →  /speckit-plan  →  /speckit-tasks  →  /speckit-implement
```

- **No escribas código de producto sin un `tasks.md` aprobado** en `specs/<NNN>-<slug>/`.
  Si te piden una feature directamente, arranca por `/speckit-specify`.
- `/speckit-specify` crea también la rama `NNN-slug` (extensión git de Spec Kit).
- Opcionales pero recomendados: `/speckit-clarify` antes de planificar,
  `/speckit-analyze` antes de implementar.
- **Exentos** del ciclo: arreglos de build, subidas de versión, typos y documentación.

Las normas del proyecto viven en `.specify/memory/constitution.md`. Este fichero es
la guía operativa; la constitución es la norma. Si enmiendas una, actualiza la otra
en el mismo cambio.

## Compilar y testear

`java` **no está en el PATH**. Cualquier `./gradlew` sin `JAVA_HOME` falla:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

./gradlew :app:assembleDebug        # APK debug
./gradlew :app:testDebugUnitTest    # tests unitarios (JVM)
./gradlew :app:lint                 # lint de Android; puerta de calidad desde la 008
                                    # (MissingTranslation / ExtraTranslation)

# un solo test o una sola clase
./gradlew :app:testDebugUnitTest --tests "*HomeViewModelTest"
./gradlew :app:testDebugUnitTest --tests "*HomeViewModelTest.registra*"

# comprobar que Firebase resuelve el google-services.json
./gradlew :app:processDebugGoogleServices
```

Los resultados de test en XML quedan en `app/build/test-results/testDebugUnitTest/`;
son más rápidos de leer que el HTML cuando algo falla.

## Arquitectura

MVVM con tres capas y una regla de dependencia estricta: **`ui → domain ← data`**.
Todo cuelga de `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/`.

| Capa | Qué contiene | Qué tiene prohibido importar |
|---|---|---|
| `domain/` | `model/`, `repository/` (interfaces), `usecase/` | `android.*`, `androidx.*`, `com.google.firebase.*`, `data.*` |
| `data/` | `source/remote/`, `source/local/`, `repository/` (implementaciones) | `ui.*` |
| `ui/` | `navigation/`, `theme/`, `components/` (reutilizables), `placeholder/`, y una carpeta por pantalla | `data.*` |
| `core/` | `di/` (módulos Koin), `ui/UiState.kt`, `util/DispatcherProvider.kt` | — |

Los SDK externos están confinados: **`FirebaseAnalyticsDataSource` es el único
fichero del proyecto que importa `com.google.firebase.*`** (junto a `core/di/FirebaseModule.kt`,
que los registra). Todo lo demás pasa por `domain/repository/AnalyticsRepository`.

La capa `domain/` tiene **cuatro motores** de cálculo, paralelos y sin dependencia entre
ellos, más un conversor de precios. Todos son Kotlin puro con `BigDecimal` construido desde
literales `String` y no redondean pasos intermedios. En oro y plata la única división redondea
**a favor de la ley**: a la baja en el modo directo y al alza en el inverso, para que la ley
resultante nunca quede por debajo de la objetivo; en soldaduras no hay ley que proteger y la
división va a la media (`HALF_UP`); en chapas **no hay ninguna división** (el ÷ 1 000 es un
`movePointLeft(3)` exacto) y por eso tampoco hay `ESCALA`. Cada motor lleva sus propias
constantes de precisión (`FINURA_ORIGEN`, `ESCALA`, `TOLERANCIA`, `MM3_POR_CM3`,
`GRAMOS_POR_ONZA_TROY`) a propósito: son documentos técnicos distintos y ninguno debe
depender de un tipo que por dentro lleve el nombre de otro metal.

- **Oro** (feature 004): `RecetasOro` es la **única fuente de verdad** de las 16 recetas
  color×ley; `CalculoAleacion` reparte la liga entre varios metales.
- **Plata** (feature 005): `LeyPlata` y `CalculoPlata`, más simples. **No hay
  `RecetasPlata`**: el cobre es el único metal de liga y §28 de su documento técnico
  prohíbe tabular coeficientes por ley, así que solo existe la fórmula general.
- **Soldaduras** (feature 006): `RecetasSoldadura` es la única fuente de verdad de §7 de
  su documento (3 recetas clásicas, la receta de la BASE y los factores de plata y de
  ley); `CalculoSoldadura` escala recetas con orden de presentación propio (por eso
  `RecetaSoldadura` lleva **lista**, no mapa) y `CalculoSoldaduraLey` resuelve la mezcla
  base + oro 18K con el color a bordo. **Ojo**: los valores de la BASE son los del
  documento (cobre 0,54 / plata 0,80 / zinc 0,92 / cadmio 1,00 por 10 g de oro); el
  mockup los muestra intercambiados y no es fuente. `ColorOroSoldadura` es un enum propio
  de 3 colores: `ColorOro` tiene ROJO y su documento no lo admite.
- **Chapas** (feature 007): `MaterialChapa` es un enum propio con los 8 materiales
  (oro 18K/14K/12K/9K, plata 950/925/900/800), sus **densidades orientativas** de §5.1 de su
  documento y la bandera `esSoloTecnica` (12K, 950, 900). **No reutiliza `LeyOro`/`LeyPlata`**:
  la densidad es un dato de ese documento y su §19 prevé densidades por color de una misma
  ley; un test de paridad vigila que milésimas y bandera coincidan. `CalculoChapa` calcula
  `ancho × largo × espesor × densidad / 1000` sin un solo redondeo. Los límites operativos
  (10 000 / 1 000 mm) son control de interfaz y viven en el ViewModel, no en el motor.
- **Cotizaciones** (feature 007): `ConversorUnidadesPrecio` (gramo / kilo / onza troy, y libra solo de origen,
  `GRAMOS_POR_ONZA_TROY = "31.1034768"`, una sola división a escala 10 `HALF_UP`, siempre desde
  la cifra del proveedor) y `PoliticaCacheCotizaciones`, la regla de la caché de una hora como
  **función pura** (`Servir` / `Esperar` / `Actualizar(pendientes)`): vigencia por metal, espera
  de 60 s entre reintentos (300 s tras un 429) y solo se consultan los metales sin precio
  vigente. Es la pieza que decide cuándo se gasta cuota, y se prueba sin corrutinas.

Los diecinueve casos de uso se registran en `domainModule` con `factoryOf`;
`ObtenerCotizacionesUseCase` es el primero `suspend` y delega en `CotizacionesRepository`, y
`ObservarIdiomaUseCase` (008) el primero que devuelve un `Flow`. Uno de ellos,
`CalcularSoldaduraLeyUseCase` (mezcla desde la base disponible), **no tiene UI**: existe
y se prueba por mandato de su documento, el mismo precedente que el modo inverso de plata
en la 005.

**El redondeo de vista es exclusivo del ViewModel, y no es el mismo en las cinco
calculadoras**: `OroViewModel` y los dos ViewModels de soldaduras redondean a la media
(`HALF_UP`) y `PlataViewModel` **trunca** (`DOWN`). No los unifiques. En plata la cifra
mostrada es la que el joyero pesa y la Ley 17/1985 no admite tolerancia en menos: con
`HALF_UP`, 100 g de plata fina hacia 950‰ mostrarían 5,158 g de cobre y la ley real caería
a 949,999‰. Truncar a 3 decimales da 5,157 g y 950,008‰, y equivale al «modo taller
seguro» del documento con la resolución de balanza de 0,001 g que recomienda por defecto.
En soldaduras no hay ley que proteger (son recetas de taller) y la suma visible puede
desviarse una milésima en los repartos con división infinita: la respuesta es la nota
«la suma puede variar mínimamente por redondeo» (§8.3 de su documento), nunca ajustar un
ingrediente para cuadrar la vista.

Las dos herramientas de la 007 añaden políticas propias, también a propósito. **Chapas** muestra
el peso con **2 decimales** `HALF_UP` (1,558 g → «1,56 g»; volumen y metal fino a 3, pureza a 1):
lo fija su documento (§7, §21) y las densidades son orientativas, así que un tercer decimal
sería precisión aparente. **Precios** muestra los importes con 2 decimales si son ≥ 1 y 4 si
son menores (el cobre por gramo ronda 0,0089 €), con **punto de miles** (el kilo de oro ronda
148.000 €) y el porcentaje siempre a 2; todo en `FormatoPrecios`, sin `Locale`. La única
excepción a «el ViewModel formatea todo» son las **fechas**: viajan como `Long` en el `UiState`
y las formatea la vista, porque el nombre del mes depende del idioma y el ViewModel no conoce
recursos. Se formatean con `android.text.format.DateFormat.getMediumDateFormat(contexto)` y
`getTimeFormat(contexto)` —API 3, y `java.time` es API 26+ con minSdk 24—, **no con
`DateUtils.formatDateTime`**: esa toma el orden de la fecha de `Locale.getDefault()`, que es el del
sistema, y con la app en un idioma y el móvil en otro mostraba «25/08/2026» en inglés. Las dos
`DateFormat` usan el locale de la configuración del contexto, que es el que `ui/idioma/` deja ya
localizado.

`ui/home/` es la pantalla de referencia: copia su forma al crear una nueva. `ui/info/`
es el segundo ejemplo, con tarjetas propias de pantalla y apertura de enlaces externos.
`ui/oro/` es el tercero: formulario reactivo sobre un motor de dominio, con el estado
ya formateado en el `UiState`. `ui/plata/` es el cuarto y el más corto,
porque se pinta entero con componentes de `ui/components/`: es el que conviene copiar para
una calculadora nueva. `ui/soldaduras/` es el quinto y el único paquete con **dos
pantallas** (la calculadora y la soldadura BASE, con ruta propia `Route.SoldaduraBase`):
sus mapeos enum→recursos compartidos viven en `PresentacionSoldadura.kt`, interno al
paquete, y su `SoldadurasUiState` usa `familia = null` para la primera visita (solo se ve
el selector de familias). Sus cinco bitmaps (`granalla`, `cadmio`, `zinc`, `laton`,
`proceso`) viven en `drawable-nodpi/` junto a los demás.
`ui/herramientas/` es el sexto y el primero con **sub-paquetes**: una sola ruta
(`Route.Herramientas`) y **tres ViewModels**. `HerramientasViewModel` solo guarda la
sub-herramienta elegida (`subherramienta = null` en la primera visita: solo el selector y una
invitación) y emite la `screen_view` `"herramientas"` del placeholder; `precios/` y `chapas/`
tienen su propio `XSection` (resuelve `koinViewModel()` **al componerse por primera vez**, así la
API no se toca hasta abrir PRECIO METALES) y su `XContent` sin estado, que pinta una `Column`
**sin** scaffold, scroll, `imePadding` ni padding exterior: los pone el armazón una sola vez.
`HerramientasContent` recibe las dos secciones como *slots* `@Composable`, y por eso se prueba
con marcadores sin conocer los ViewModels. El dueño de los tres ViewModels es la
`NavBackStackEntry`: el estado sobrevive al cambio de sub-herramienta. `chapas/DibujoChapa.kt` es
el primer `Canvas` de la app (proyección oblicua, `drawWithCache`, animaciones leídas solo dentro
de `onDrawBehind`, construcción con `PathMeasure`); en las `@Preview` arranca ya construido vía
`LocalInspectionMode`. `rodio.png` se suma a los bitmaps de `drawable-nodpi/`.
`ui/ajustes/` es el séptimo, y el más corto de todos: una tarjeta con seis filas de selección
(«Automático» más los cinco idiomas) sobre `TarjetaAcento`, con `FilaIdioma` privada del fichero.
`ui/idioma/` es el octavo y **el primer paquete de `ui/` sin ruta ni pantalla**: no es de nadie en
particular, es del árbol entero (ver la sección de idioma).

### Componentes compartidos

`ui/components/` tiene el armazón que usan todas las pantallas salvo la portada:

- **`JewelryScaffold`** — barra superior, contenido y barra inferior opcional. **Cada
  pantalla declara aquí su propio *chrome***; no hay un `Scaffold` global que deduzca
  las barras husmeando la ruta actual. La app es edge-to-edge y los WindowInsets se
  reparten así: `JewelryTopBar` consume la barra de estado, `JewelryBottomBar` la de
  navegación, y **cuando la pantalla no lleva barra inferior es el scaffold quien
  reserva ese hueco** (`windowInsetsPadding(navigationBars.only(Bottom))`) — sin eso,
  los 3 botones de Android caen encima del contenido. No añadas insets inferiores en
  las pantallas: ya vienen resueltos de aquí (los `imePadding()` no se duplican porque
  Compose descuenta lo consumido).
- **`JewelryTopBar`** — sin `title` pinta el logo centrado (zonas principales); con
  `title` y `onBack` pinta flecha y nombre de sección. `onInfo` es **nulable**: la
  pantalla de información lo pasa a `null` y el icono se cambia por un hueco de 48 dp,
  porque un atajo a la pantalla en la que ya estás no significa nada.
- **`JewelryBottomBar`** — solo en Home, Favoritos y Ajustes. `MainTab` es su enum de
  destinos.
- **`ModuleCard`** — tarjeta del menú, con color de acento por módulo.
- **`DiamondDivider`** (en `Ornamentos.kt`) — filete dorado con rombo al centro. Nació
  privado en la portada; lo comparten ahora la portada y las tarjetas de información.
  `widthFraction` lo ajusta al hueco: 0.7 en pantalla completa, 1 dentro de una tarjeta.
- **`TarjetaAcento`** (en `Tarjetas.kt`) — envoltorio de tarjeta con degradado y borde
  del color de acento (dorado por defecto). Nació privada en Info como `TarjetaDorada`;
  la comparten Info y las dos calculadoras: oro la usa en dorado y en el tono del oro
  elegido, plata en plateado (entrada y total) y en teal (resultado).
- **`SelectorSegmentado`** — fila de opciones excluyentes con píldora degradada y check
  en el acento. Hecho a mano: `SegmentedButton` de Material impone su geometría. El
  acento va **por opción** (`OpcionSegmento`), que es lo que permite elegir cada color de
  oro en su propio tono; con el valor por defecto toda la fila sale dorada. `maxPorFila`
  (opcional, por defecto sin límite) parte las opciones en varias filas con índices
  globales: lo estrenaron las 5 durezas de soldaduras con `maxPorFila = 3`. Acepta
  `seleccionada = -1` para pintar el grupo sin ninguna opción activa (la primera visita
  de soldaduras). `OpcionSegmento.peso` (por defecto 1f) reparte el ancho de la fila:
  una etiqueta claramente más larga que sus vecinas puede pedir más sitio — «Muy floja
  (18K)» va con 1.5f junto a «Floja» y «Fuerte» en la clásica de soldaduras. `OpcionSegmento.iconRes` (por defecto `null`) pinta un icono en el hueco del check: lo
  estrenó el selector de sub-herramientas de Herramientas; con icono no hay check.

Los siete siguientes nacieron privados en `ui/oro/OroScreen.kt` y subieron aquí con la
feature 005, cuando la calculadora de plata pidió los mismos. Es la regla del proyecto: en
cuanto un segundo consumidor lo pide, deja de ser privado. Ninguno conoce `domain/` — es
cada pantalla la que mapea sus enums a imágenes y textos.

- **`CampoCantidad`** y **`CabeceraSeccion`** (en `Formularios.kt`) — el campo de gramos
  con cifra grande y sufijo «gr», y la cabecera de sección con icono. Los dos toman su
  acento por parámetro: dorado por defecto, plateado en plata.
- **`CampoMedida`** (en `Formularios.kt`) — campo con etiqueta pequeña, icono, cifra de 20 sp y
  unidad, para medidas seguidas (ancho / espesor / largo en mm); `error` pinta el filete en
  `Danger` e `imeAction` encadena los campos. Comparte con `CampoCantidad` el `MarcoCampo`
  privado (la caja redondeada con filete), que es lo que legitima la extracción.
- **`BotonDorado`** (en `Botones.kt`) — botón de acción principal. **No** se parametriza el
  color: el dorado es el lenguaje de acción de la app, no el acento de un módulo, así que
  «Limpiar» y «Guardar en favoritos» son dorados también en la pantalla de plata.
- **`AvisoTecnico`** (en `Avisos.kt`) — advertencia ámbar con región viva para el lector
  de pantalla. El texto va por parámetro porque oro tiene un aviso (12 K), plata dos
  (950‰ y 900‰) y soldaduras usa el mismo componente para la advertencia de seguridad de
  cadmio/zinc (obligatoria por §9 de su documento, y **antes** del proceso de taller en
  la pantalla de la base).
- **`FilaMetal`** y **`TarjetaTotal`** (en `Tarjetas.kt`) — fila de resultado por metal y
  tarjeta de total con balanza. En `TarjetaTotal` el acento tiñe icono, cifra y unidad; la
  etiqueta se queda en `TextPrimary`.
- **`LineaPunteada`** (en `Ornamentos.kt`) — los puntos que guían del nombre a la cifra.

`JewelryBottomBar` y el botón de la portada **no usan los componentes de Material**:
`NavigationBar` impone su propia altura y una píldora tras el icono activo, y `Button`
impone un contenedor opaco que taparía el fondo. Ambos están escritos a mano a
propósito.

### Iconos

**`material-icons` no está en el classpath**: Material 3 1.4.0 dejó de arrastrarlo, así
que `Icons.Default.*` no compila. Los iconos son vectores propios en `res/drawable`
(`ic_home`, `ic_favoritos`, `ic_ajustes`, `ic_chevron`, `ic_info`, `ic_atras`,
`ic_linkedin`, `ic_instagram`, `ic_enlace_externo`, `ic_check`, `ic_aviso`, `ic_refrescar`,
`ic_estrella`, `ic_balanza`, `ic_lingotes`, `ic_paleta`, `ic_grafica`, `ic_capas`, `ic_ancho`,
`ic_espesor`, `ic_regla`, `ic_idioma`), de trazo
1.5–1.8 y tintados en tiempo de ejecución con `Icon(tint = ...)`. Las **cinco banderas**
(`ic_bandera_es`, `_en`, `_fr`, `_de`, `_it`) también son vectores propios, pero de relleno y en
3:2: la de España va sin escudo y la británica está redibujada con paths de relleno, porque
`<use>` no existe en VectorDrawable y su `clip-path` sobre un trazo no se reproduce. Si necesitas uno
nuevo, dibújalo ahí en lugar de añadir la librería, que está deprecada.

### Pantallas aún sin desarrollar

`ui/placeholder/PlaceholderScreen` es **un composable parametrizado**. Desde la 008 le queda un
solo destino, Favoritos: Ajustes ya tiene pantalla propia. Recibe
`title` (traducible) y `analyticsName` (identificador estable para telemetría, que no
debe traducirse). Cuando un destino reciba su feature
real, cambia solo su cableado en `AppNavHost`.

### Contrato de ViewModel

- Expone **un único** `StateFlow` inmutable de un data class propio de la pantalla
  (`HomeUiState`). Nada de `LiveData` ni de estado mutable público.
- No importa `androidx.compose.*`: no conoce a su vista.
- Recibe todo por constructor. Para corrutinas usa el `DispatcherProvider` inyectado,
  nunca `Dispatchers.IO` directo — es lo que permite testearlo con `TestDispatcher`.
  `PreciosMetalesViewModel` es el primero que lo hace: lanza siempre con
  `viewModelScope.launch(dispatchers.main)`, y su test inyecta `TestDispatcherProvider`
  (`app/src/test/.../core/util/`, junto a `RelojFalso` y los fakes de `data/`) sin tocar
  `Dispatchers.setMain`. Todo lo textual del `UiState` va formateado salvo las fechas (ver
  arriba); lo que la vista traduce o colorea viaja como enum y se mapea en el
  `Presentacion*.kt` del paquete. `core/util/Decimales.kt` (`parsearDecimalPositivo`) es el
  parser de texto compartido: coma y punto valen; vacío, no numérico o ≤ 0 → `null`.

### Contrato de pantalla

Cada pantalla se parte en dos Composables: uno resuelve el ViewModel con
`koinViewModel()` y otro pinta, sin estado y con `@Preview`. Ver `ui/home/HomeScreen.kt`.

### Navegación

Rutas **type-safe** con `@Serializable` en `ui/navigation/Routes.kt`, registradas con
`composable<Route.X>` en `AppNavHost.kt`. No se usan rutas como String.

## Idioma de la app: cinco carpetas y un proveedor

La feature 008 hizo la app multilingüe: **español, inglés, francés, alemán e italiano**, con la
elección guardada en Preferences DataStore y aplicada al instante.

- **Cinco `strings.xml`**: `values/` es el **español y la fuente de verdad** —aquí se añade y se
  borra—, más `values-en`, `values-fr`, `values-de` y `values-it`. 217 cadenas, de las que **33 son
  `translatable="false"`** y 184 se traducen. La regla de lo no traducible: **no queda ni una palabra
  dentro** (marca, nombres propios, «Ask»/«Bid», símbolos del SI, cifras con su quilate, plantillas
  de formato y los cinco endónimos de idioma). Cuando una cadena mezcla palabra y nombre propio se
  parte en `%1$s`: así están `precios_fuente` + `precios_fuente_nombre` y `welcome_developer` +
  `info_perfil_nombre`. Contrato completo en `specs/008-ajustes-idioma/contracts/traducciones.md`.
- **`TraduccionesTest`** (`app/src/test/.../recursos/`) parsea los cinco ficheros y falla si falta o
  sobra una clave, si se traduce algo no traducible, si cambian los `%n$s`, el `%%` o los `\n`, o si
  una cadena larga es idéntica al español en los cuatro idiomas. **`./gradlew :app:lint` es la
  segunda red y puerta de calidad de la app**: `MissingTranslation` y `ExtraTranslation`.
- **El cambio de idioma no recrea nada**: `ui/idioma/ProveedorIdioma` envuelve al `NavHost` en
  `MainActivity` y provee `LocalContext` (contexto de `createConfigurationContext` con el idioma) y
  `LocalConfiguration`. `stringResource` lee `LocalResources`, que es un
  `compositionLocalWithComputedDefaultOf` derivado de esos dos, así que se recalcula solo: **no
  proveas `LocalResources` a mano**. La configuración se lee de `LocalConfiguration.current` y no de
  `contexto.resources.configuration` —lint lo vigila con `LocalContextConfigurationRead`—, porque si
  no, un cambio real del sistema (el tamaño de letra) dejaría el contexto con la configuración vieja.
  De regalo, los cinco `Toast` y las fechas de precios salen en el idioma elegido.
- **Nada de AppCompat**: `AppCompatDelegate.setApplicationLocales` exigiría la dependencia,
  `AppCompatActivity` y un tema AppCompat —y el actual está ajustado a mano por el splash de Android
  12+—, y por debajo de API 33 no actúa sobre una `ComponentActivity`. Tampoco `attachBaseContext` +
  `recreate()`: leería DataStore bloqueando el arranque y perdería lo que el joyero tenga escrito.
- **La precedencia vive en el dominio**: `SeleccionIdioma.efectivo = elegido ?: sistema`. `IdiomaApp`
  son los cinco idiomas y **«Automático» es `null`**, no un sexto valor del enum: un valor que nunca
  puede llegar a `Locale.forLanguageTag` acabaría colándose en una conversión. `IdiomaApp.desdeEtiqueta`
  ignora la región (`es-ES`, `es_MX`, `es-419` → `ESPANOL`) y devuelve `null` si no está soportado,
  para que el llamante decida: el idioma del sistema cae a `PREDETERMINADO` (español, el de `values/`)
  y una preferencia ilegible se comporta como si no hubiera preferencia.
- **`IdiomaSistema`** (`core/util/`, hermano de `Reloj`) es el idioma del dispositivo tras interfaz,
  JVM puro sobre `Locale.getDefault()`. **No lo leas del `Configuration` de la app**: con el proveedor
  en marcha, la configuración dice el idioma *elegido* y «Automático» dejaría de saber a qué seguir.
- **La raíz no pinta hasta saberlo**: `IdiomaAppUiState.idioma` nulo significa «aún no sé» y en ese
  estado `MainActivity` no compone el `NavHost`; el hueco lo cubre el `windowBackground` del tema, que
  ya es el azul de la portada. Así no hay un fotograma en el idioma equivocado.
- **Dos ViewModels observan el mismo flujo**: `IdiomaAppViewModel` (de la Activity, solo observa y no
  emite telemetría) y `AjustesViewModel` (de la pantalla, escribe y registra `ajustes_idioma`; su
  `screen_view` sigue siendo `"ajustes"`, el nombre del placeholder). El estado de Ajustes se escribe
  cuando **el flujo lo confirma**, no al guardar.
- **Persistencia**: `data/source/local/DataStoreAjustesLocalDataSource`, fichero `ajustes`, una sola
  clave con la etiqueta BCP-47. **La ausencia de clave es «Automático»**, no hay valor centinela. Una
  etiqueta desconocida se ignora **sin borrarla** (al contrario que la caché de cotizaciones, que sí
  descarta lo que no entiende: aquí es una decisión del joyero, no un dato derivado). El almacén se
  crea `by lazy` dentro del data source y **no** se registra en Koin: `verify()` solo inspecciona
  constructores del tipo primario, y un `DataStore` de fábrica habría que meterlo en `extraTypes`.
- **El fichero de DataStore sí entra en la copia de seguridad**, y los dos XML de `res/xml/` lo dicen
  en un comentario: el idioma elegido debe acompañar al joyero a un móvil nuevo.
- **`bundle { language { enableSplit = false } }`** en `app/build.gradle.kts`. Sin eso, un App Bundle
  publicado en Play instala solo el idioma del dispositivo y elegir otra bandera no cambiaría nada en
  producción; el APK de debug no lo delata porque lleva los cinco idiomas dentro.
- **No se declara `android:localeConfig`**: el selector de idioma del sistema (API 33+) sería una
  segunda fuente de verdad que nuestra preferencia ignoraría.
- **El formato de las cifras no se localiza** (decisión del autor): `FormatoPrecios` y los cinco
  ViewModels que formatean siguen con coma decimal determinista, así que en inglés se ve «127,89
  €/g». Alemán, francés e italiano comparten la coma con el español. Es deuda conocida de una feature
  aparte, junto con la moneda cableada al euro.
- **Al traducir, vigila los desbordes**: el alemán es más largo. `JewelryBottomBar` y `BotonDorado`
  pintan su etiqueta con `BasicText` + `TextAutoSize` a una línea, y las pestañas de la barra ocupan
  un tercio del ancho cada una (`weight(1f)` con 96 dp de mínimo) porque «Einstellungen» no cabía en
  96 dp con la fuente del sistema al doble.
- **Para probar en el emulador**: `adb shell cmd locale set-app-locales <pkg> --locales fr-FR` cambia
  lo que el sistema entrega a la app y **no necesita root**, al contrario que
  `setprop persist.sys.locale`, que en una imagen de producción falla en silencio.

## Red y caché: cotizaciones de metales

La feature 007 estrenó red, corrutinas y persistencia **sin dependencias nuevas**. Todo vive en
`data/source/` y detrás de interfaces:

- **`ClienteHttp`** (`data/source/remote/`) con la implementación `ClienteHttpUrlConnection`
  sobre `java.net.HttpURLConnection` (que en Android va sobre el OkHttp interno). Cinco GET por
  hora no justifican OkHttp/Retrofit; si llega un backend propio, se cambia la implementación.
- **`MetalSentinelDataSource`** es el **único punto que habla con el proveedor**
  (Metal Sentinel vía RapidAPI). Contrato en `specs/007-herramientas/contracts/metal-quote.md`.
  Ruta `/metal-quote` y `PARAMETRO_METAL = "symbol"`, **confirmados con la credencial real**
  (la ruta `/api/metal-quote` de la web pública no existe para la suscripción y `metal=`
  devuelve un 200 con cuerpo de error); prohibido probar variantes en cada carga, gasta cuota.
  El cobre cotiza por **libra** (`POUND` → `UnidadPrecio.LIBRA`, unidad solo de origen; el
  selector usa `UnidadPrecio.seleccionables`). Los DTO son `@Serializable` con `ignoreUnknownKeys` y
  `BigDecimalExactoSerializer` toma el **literal del cable**, nunca `Double`.
- **Caché**: `SharedPreferences` (fichero `cotizaciones`, **una sola clave** con el JSON de la
  instantánea: escritura atómica) tras `CotizacionesLocalDataSource`; `CodificadorInstantanea`
  es puro y se prueba en JVM. El fichero está **excluido del backup** (`res/xml/`). La decisión
  de tocar la red es de `PoliticaCacheCotizaciones` (dominio) y el `Mutex` de
  `CotizacionesRepositoryImpl` es el *single-flight*; solo se captura `MetalSentinelException`.
- **`Reloj`** (`core/util/`, registrado en `coreModule`) es la hora del sistema tras interfaz:
  la caché de una hora se prueba con `RelojFalso` en milisegundos de ejecución.
- **Credencial**: `RAPIDAPI_KEY` en `local.properties` (ignorado por git), leída en
  `app/build.gradle.kts` con la API de `providers` (compatible con la caché de configuración) y
  volcada a `BuildConfig.RAPIDAPI_KEY`. Vacía, la build avisa y la pantalla muestra «servicio
  no configurado» sin tocar la red. **Es extraíble del APK**: integración de prototipo; la
  cuota gratuita (15 000 peticiones/mes, 5 por carga) no da para una app pública. El backend con
  caché compartida es una feature aparte. La clave no aparece en código, registros ni tests
  (`git grep -i rapidapi` solo debe dar `build.gradle.kts`, `CLAUDE.md`, `specs/` y
  `UI_Plantillas/`).
- El permiso `INTERNET` está en el manifest; solo HTTPS.

## Añadir dependencias a Koin

1. Registra en el módulo que toque de `core/di/` (`coreModule`, `dataModule`,
   `domainModule`, `viewModelModule`).
2. Los repositorios van **siempre por su interfaz de dominio**:
   `single<AnalyticsRepository> { AnalyticsRepositoryImpl(get()) }`.
   Nunca registres la implementación como tipo público.
3. Los ViewModel van con `viewModelOf(::MiViewModel)`.
4. Si creas un **módulo nuevo**, añádelo a `featureModules` en `core/di/AppModule.kt`.
   Eso basta para que `KoinModulesTest` lo verifique automáticamente.

`KoinModulesTest` recorre el grafo con `verify()` y falla si a algún constructor le
falta una dependencia. **Ojo con lo que verifica**: solo inspecciona los constructores del **tipo primario** de
cada definición, así que `single<Interfaz> { Impl(get()) }` no comprueba nada de `Impl`; por eso
los data sources de cotizaciones van concretos con `bind` a su interfaz. Acepta parámetros con
valor por defecto, pero `viewModelOf`/`factoryOf` los resuelven todos con `get()` en runtime
ignorando defaults: un default de un tipo no registrado pasa el test y peta al arrancar. Regla:
defaults solo en clases registradas con lambda explícita, y los tipos de infraestructura
(`Reloj`, `DispatcherProvider`) siempre en el grafo. `firebaseModule` queda fuera de ese `verify()` a propósito:
sus definiciones nacen de fábricas estáticas (`Firebase.analytics`), no de
constructores, así que la reflexión acabaría inspeccionando tipos internos de Google
Play Services. Sus tipos entran como `extraTypes`.

## Dependencias y versiones

**Todo pasa por `gradle/libs.versions.toml`.** Nunca escribas coordenadas ni versiones
sueltas en un `build.gradle.kts`.

- Cuando hay BoM (Firebase, Compose, Koin) se usa, y los artefactos se declaran
  **sin versión**.
- **La versión de Kotlin la fija AGP**: AGP 9.3.1 trae Kotlin integrado 2.2.10 y por
  eso no existe el plugin `kotlin-android` en este proyecto. No añadas ese plugin ni
  subas `kotlin` por libre en el catálogo.
- Compose BOM 2026.08.00 exige `compileSdk 37`. `targetSdk` se queda en 36 a
  propósito: subirlo opta a comportamientos nuevos de runtime y es una decisión
  aparte que requiere probar la app.
- Las APIs `.ktx` de Firebase **ya no existen** en el BoM 34: los import correctos son
  `com.google.firebase.Firebase`, `com.google.firebase.analytics.analytics` y
  `com.google.firebase.crashlytics.crashlytics`.

## Firebase

`app/google-services.json` está **en `.gitignore` y no se commitea**. Un clon nuevo no
compila hasta descargarlo de la consola de Firebase (proyecto `calculadora-de-joyeros`)
y dejarlo en `app/`.

El `applicationId` es `com.jrblanco.calculadoradejoyeros2021` aunque el repo se llame
`...2026`: el `google-services.json` está registrado contra ese package y cambiarlo
obliga a dar de alta una app Android nueva en Firebase.

## Builds de release

`release` tiene R8 activado (`optimization { enable = true }`). El bloque
`uploadCrashlyticsMappingFileRelease` corre **dentro de `assembleRelease`**, así que el
mapping se sube a Crashlytics solo y los stack traces de producción llegan
desofuscados con nombre de fichero y número de línea.

- **No añadas `-keepattributes SourceFile,LineNumberTable`**: R8 ya guarda esa
  información en el propio mapping. Está verificado con `retrace`, y esa regla sería
  ruido heredado de ProGuard.
- Las reglas de R8 van en `app/src/main/keepRules/*.keep`. AGP 9 las recoge solas: en
  este proyecto no existe `proguardFiles` ni `proguard-rules.pro`.
- **No hay `signingConfig` de release todavía**: `assembleRelease` produce
  `app-release-unsigned.apk`. Para probar un release en el emulador hay que firmarlo a
  mano; hace falta un keystore antes de poder publicar.

```bash
# probar un release localmente (firma con la clave de debug, no vale para publicar)
$ANDROID_HOME/build-tools/37.0.0/apksigner sign --ks ~/.android/debug.keystore \
  --ks-pass pass:android --key-pass pass:android --out /tmp/rel.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk

# desofuscar un stack trace a mano
$ANDROID_HOME/cmdline-tools/latest/bin/retrace \
  app/build/outputs/mapping/release/mapping.txt traza.txt
```

## Commits

Conventional Commits (`feat:`, `fix:`, `build:`, `test:`, `docs:`, `chore:`,
`refactor:`), en español. Es también lo que genera `/speckit-git-commit`.
