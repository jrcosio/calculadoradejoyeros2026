# Implementation Plan: Pantalla de Información («Acerca de»)

**Branch**: `003-info-acerca-de` | **Date**: 2026-08-23 | **Spec**: [spec.md](./spec.md)

## Summary

`Route.AcercaDe` deja de caer en `PlaceholderScreen` y pasa a una pantalla real: título,
tarjeta de perfil del autor con foto y etiquetas, dos tarjetas de enlace externo (LinkedIn
e Instagram), una tarjeta informativa de Blanco Joyeros y la versión de la app al pie. Es
el último destino accesible desde la barra superior que seguía siendo andamiaje.

El destino ya existe en el grafo de navegación desde la feature 002, así que la feature no
crea rutas: sustituye el cuerpo de un `composable<Route.AcercaDe>` y añade el paquete
`ui/info/`.

## Technical Context

**Language/Version**: Kotlin 2.2.10 (el que fija AGP 9.3.1) · Java 17
**Primary Dependencies**: Compose BOM 2026.08.00, Material 3, Navigation Compose 2.9.8 con
rutas `@Serializable`, Koin 4.2.2, Firebase BoM 34.18.0 (Analytics + Crashlytics)
**Storage**: N/A — la pantalla es contenido fijo, no persiste nada
**Testing**: JUnit4 + MockK + Turbine + `kotlinx-coroutines-test` (JVM);
`compose-ui-test-junit4` (instrumentado)
**Target Platform**: Android, minSdk 24 / targetSdk 36 / compileSdk 37, solo vertical
**Project Type**: app Android de un solo módulo (`:app`), MVVM `ui → domain ← data`
**Performance Goals**: los de una pantalla estática — sin trabajo en el hilo principal más
allá de decodificar tres imágenes
**Constraints**: sin dependencias nuevas; sin `material-icons` en el classpath; textos
traducibles; funcionamiento correcto sin navegador instalado
**Scale/Scope**: una pantalla, dos enlaces externos, tres imágenes y tres iconos nuevos

**Esta feature no añade ninguna dependencia.** Todo lo que necesita ya está en
`gradle/libs.versions.toml`.

### Decisiones y sus porqués

| Decisión | Motivo |
|---|---|
| Sin barra inferior, pese al mockup | Info es una sección, no una de las tres zonas principales. La feature 002 (FR-007) reserva `JewelryBottomBar` a Home, Favoritos y Ajustes, y `CLAUDE.md` lo recoge como norma. Confirmado con el propietario |
| `JewelryScaffold(title = null, onBack = …)` | Con `title = null` la barra superior pinta el logo centrado, que es lo que muestra el mockup, y con `onBack` añade la flecha. El título «Información» va dentro del contenido. Cero componentes nuevos |
| Título en Manrope, no en serif | `Type.kt` reserva Playfair Display a la portada («esta familia es la excepción de portada, Manrope manda en el resto»). Se usa `displayLarge` con el mismo degradado dorado del título de portada para conservar el aire del mockup sin romper la regla tipográfica |
| Sin icono de info dentro de Info | Un acceso a la pantalla en la que ya estás no significa nada. `JewelryTopBar` y `JewelryScaffold` pasan a aceptar `onInfo` **nulo**, igual que ya hacen con `onBack` y `bottomBar`; con `onInfo = null` el icono se sustituye por un hueco de 48 dp para que el logo siga centrado. Las demás pantallas no cambian: siguen pasando su lambda |
| `InfoEnlace` como enum sin `R` ni `Color` | Mismo patrón que `HomeModule`: mantiene el ViewModel libre de Android y testeable en JVM. El mapeo a icono, acento y textos vive en la capa Compose |
| URL dentro del enum, no en `strings.xml` | No es texto traducible: es el destino. La etiqueta visible sí sale de `strings.xml` |
| `LocalUriHandler` para abrir enlaces | Emite el intent estándar del sistema: la app de LinkedIn/Instagram lo captura si está instalada y, si no, lo recoge el navegador (FR-008). No necesita `<queries>` en el manifiesto ni dependencias nuevas. Se descarta Custom Tabs (`androidx.browser`) porque añadiría dependencia y no abriría la app nativa |
| `runCatching` alrededor de `openUri` | `AndroidUriHandler` lanza `IllegalArgumentException` cuando no hay ninguna actividad capaz de atender el enlace. Sin capturarla, la app se cerraría (FR-009, SC-004). El fallo se reporta por `AnalyticsRepository.recordError` |
| Versión como parámetro de `InfoContent` con `BuildConfig.VERSION_NAME` por defecto | Inyectarla por Koin metería un `String` en el grafo y debilitaría `KoinModulesTest` — el mismo motivo que ya está documentado en `PlaceholderViewModel`. Como parámetro con valor por defecto, la preview y el test instrumentado la fijan sin tocar el grafo (FR-010) |
| Acentos de LinkedIn e Instagram como `private val` de `ui/info/` | Son colores de marca de terceros, no tokens del sistema de diseño: no entran en `JewelryColors`. Se usan versiones aclaradas del azul y el magenta corporativos para mantener contraste sobre `Background` |
| Iconos de marca monocromos y teñidos | `CLAUDE.md` fija que los iconos son vectores propios tintados en tiempo de ejecución. El mockup pinta Instagram en degradado; monocromo teñido mantiene la coherencia con el resto de la iconografía y evita empaquetar un logotipo de marca a color |
| `DiamondDivider` promovido a `ui/components/` | Hoy es `private` en `WelcomeScreen`. El mockup lo pide en dos tarjetas de Info; duplicarlo sería deuda desde el primer día |
| `verticalScroll` y no `LazyColumn` | Son seis bloques fijos y todos se muestran siempre; `LazyColumn` solo añadiría complejidad de reciclado sin ganancia (FR-012) |
| Imágenes a 512 px en `drawable-nodpi/` | Es el precedente de la feature 002. Los originales pesan 1,5–1,8 MB cada uno y entrarían en el APK tal cual |

## Constitution Check

*GATE: revisado antes de la Fase 0 y de nuevo tras el diseño. Sin violaciones.*

| Principio | Cumplimiento |
|---|---|
| I. SDD obligatorio | spec → plan → tasks → implement. Ningún fichero de producto se toca antes de `tasks.md` |
| II. MVVM con capas estancas | `InfoViewModel` expone un único `StateFlow<InfoUiState>`, no importa `androidx.compose.*` ni `R`, y la telemetría entra por `AnalyticsRepository`. `InfoEnlace` es Kotlin puro. `InfoScreen` se parte en el composable que resuelve el ViewModel y `InfoContent`, sin estado y con `@Preview` |
| III. DI solo por Koin | `viewModelOf(::InfoViewModel)` en `viewModelModule`. Sin módulo nuevo, sin `get()` dentro de la clase, sin `String` en el grafo |
| IV. Test obligatorio | `InfoViewModelTest` (JVM) cubre el único ViewModel de la feature; `InfoScreenTest` (instrumentado) cubre el pintado y los callbacks. `KoinModulesTest` recoge el registro nuevo sin tocarlo |
| V. Versiones en `libs.versions.toml` | No se añaden dependencias ni versiones |

Restricciones técnicas: no se usa ninguna API por encima de minSdk 24 —`LocalUriHandler`,
`painterResource` y `verticalScroll` están disponibles desde el mínimo—, no entra ningún
producto nuevo de Firebase y no se toca `google-services.json`.

Sin desviaciones que declarar. La de la feature 001 (`WelcomeViewModel` sin `StateFlow`)
sigue vigente y documentada allí.

## Project Structure

### Documentation (this feature)

```text
specs/003-info-acerca-de/
├── spec.md
├── plan.md              # este fichero
├── tasks.md             # salida de /speckit-tasks
└── checklists/
    └── requirements.md
```

No se generan `research.md`, `data-model.md`, `contracts/` ni `quickstart.md`, igual que en
las features 001 y 002: la spec no dejó ningún `NEEDS CLARIFICATION` que investigar, la
feature no persiste datos ni expone interfaz a terceros, y la guía de validación vive en la
fase de Verificación de `tasks.md` y en los criterios de éxito de la spec.

### Source Code (repository root)

```text
app/src/main/
├── java/com/jrblanco/calculadoradejoyeros2021/
│   ├── ui/
│   │   ├── components/
│   │   │   ├── Ornamentos.kt        (N) DiamondDivider + GoldHairline, movidos desde
│   │   │   │                            WelcomeScreen y con widthFraction parametrizado
│   │   │   ├── JewelryTopBar.kt    (M) onInfo nulo oculta el icono y deja hueco simétrico
│   │   │   ├── JewelryScaffold.kt  (M) propaga el onInfo nulo
│   │   │   └── (sin cambios)            JewelryBottomBar, ModuleCard
│   │   ├── info/                    (N) paquete de la feature
│   │   │   ├── InfoEnlace.kt        (N) enum puro: LINKEDIN, INSTAGRAM (url + analyticsId)
│   │   │   ├── InfoUiState.kt       (N) data class con la lista de enlaces
│   │   │   ├── InfoViewModel.kt     (N) StateFlow único + telemetría
│   │   │   └── InfoScreen.kt        (N) InfoScreen + InfoContent + tarjetas privadas
│   │   ├── welcome/WelcomeScreen.kt (M) importa DiamondDivider en vez de definirlo
│   │   └── navigation/AppNavHost.kt (M) Route.AcercaDe → InfoScreen
│   └── core/di/ViewModelModule.kt   (M) + viewModelOf(::InfoViewModel)
└── res/
    ├── drawable-nodpi/              (N) foto_jrblanco.png, logo_blanco_joyeros.png,
    │                                    joya_lupa.png — 512 px desde UI_Plantillas/Feature_Info/
    ├── drawable/                    (N) ic_linkedin.xml, ic_instagram.xml, ic_enlace_externo.xml
    └── values/strings.xml           (M) bloque de la pantalla de información;
                                         se borra pantalla_acerca_de, que queda sin uso

app/src/test/.../ui/info/InfoViewModelTest.kt        (N)
app/src/androidTest/.../ui/info/InfoScreenTest.kt    (N)

CLAUDE.md                            (M) siete destinos pendientes pasan a seis;
                                         ui/info/ y el ornamento compartido documentados
```

**Structure Decision**: se mantiene la estructura de un solo módulo `:app` con
`ui → domain ← data` descrita en `CLAUDE.md`. La feature solo añade un paquete de pantalla
bajo `ui/`, sin tocar `domain/` ni `data/`: la telemetría ya tiene su interfaz de dominio y
el contenido es fijo, así que no hay repositorio ni caso de uso que crear.

## Complexity Tracking

Sin complejidad que justificar: no hay dependencias nuevas, ni capas nuevas, ni datos
persistidos, ni desviaciones de la constitución.

Las dos divergencias respecto al mockup —barra inferior omitida y título en Manrope en vez
de serif— no son desviaciones de la constitución sino decisiones de diseño; quedan
razonadas en «Decisiones y sus porqués» y anotadas como suposiciones en la spec.
