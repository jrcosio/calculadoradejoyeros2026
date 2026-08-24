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
./gradlew :app:lint                 # lint de Android

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

La capa `domain/` tiene **tres motores**, paralelos y sin dependencia entre ellos. Los
tres son Kotlin puro con `BigDecimal` construido desde literales `String` y no redondean
pasos intermedios. En oro y plata la única división redondea **a favor de la ley**: a la
baja en el modo directo y al alza en el inverso, para que la ley resultante nunca quede
por debajo de la objetivo; en soldaduras no hay ley que proteger y la división va a la
media (`HALF_UP`). Cada motor lleva sus propias constantes de precisión (`FINURA_ORIGEN`,
`ESCALA`, `TOLERANCIA`) a propósito: son tres documentos técnicos distintos y ninguno
debe depender de un tipo que por dentro lleve el nombre de otro metal.

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

Los trece casos de uso se registran en `domainModule` con `factoryOf`. Uno de ellos,
`CalcularSoldaduraLeyUseCase` (mezcla desde la base disponible), **no tiene UI**: existe
y se prueba por mandato de su documento, el mismo precedente que el modo inverso de plata
en la 005.

**El redondeo de vista es exclusivo del ViewModel, y no es el mismo en las tres
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
  (18K)» va con 1.5f junto a «Floja» y «Fuerte» en la clásica de soldaduras.

Los siete siguientes nacieron privados en `ui/oro/OroScreen.kt` y subieron aquí con la
feature 005, cuando la calculadora de plata pidió los mismos. Es la regla del proyecto: en
cuanto un segundo consumidor lo pide, deja de ser privado. Ninguno conoce `domain/` — es
cada pantalla la que mapea sus enums a imágenes y textos.

- **`CampoCantidad`** y **`CabeceraSeccion`** (en `Formularios.kt`) — el campo de gramos
  con cifra grande y sufijo «gr», y la cabecera de sección con icono. Los dos toman su
  acento por parámetro: dorado por defecto, plateado en plata.
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
`ic_estrella`, `ic_balanza`, `ic_lingotes`, `ic_paleta`), de trazo
1.5–1.8 y tintados en tiempo de ejecución con `Icon(tint = ...)`. Si necesitas uno
nuevo, dibújalo ahí en lugar de añadir la librería, que está deprecada.

### Pantallas aún sin desarrollar

`ui/placeholder/PlaceholderScreen` es **un composable parametrizado** que sirve a los
tres destinos pendientes (Favoritos, Ajustes y Herramientas). Recibe
`title` (traducible) y `analyticsName` (identificador estable para telemetría, que no
debe traducirse). Cuando un destino reciba su feature
real, cambia solo su cableado en `AppNavHost`.

### Contrato de ViewModel

- Expone **un único** `StateFlow` inmutable de un data class propio de la pantalla
  (`HomeUiState`). Nada de `LiveData` ni de estado mutable público.
- No importa `androidx.compose.*`: no conoce a su vista.
- Recibe todo por constructor. Para corrutinas usa el `DispatcherProvider` inyectado,
  nunca `Dispatchers.IO` directo — es lo que permite testearlo con `TestDispatcher`.

### Contrato de pantalla

Cada pantalla se parte en dos Composables: uno resuelve el ViewModel con
`koinViewModel()` y otro pinta, sin estado y con `@Preview`. Ver `ui/home/HomeScreen.kt`.

### Navegación

Rutas **type-safe** con `@Serializable` en `ui/navigation/Routes.kt`, registradas con
`composable<Route.X>` en `AppNavHost.kt`. No se usan rutas como String.

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
falta una dependencia. `firebaseModule` queda fuera de ese `verify()` a propósito:
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
