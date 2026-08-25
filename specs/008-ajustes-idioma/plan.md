# Implementation Plan: Ajustes — idioma de la aplicación

**Branch**: `008-ajustes-idioma` | **Date**: 2026-08-25 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/008-ajustes-idioma/spec.md`

## Summary

La pantalla de Ajustes deja de ser andamiaje y pasa a ofrecer el idioma de la app: «Automático» más
español, inglés, francés, alemán e italiano, cada uno con su bandera y su nombre en su propia lengua.
La elección se guarda en **Preferences DataStore** (primera dependencia nueva del proyecto desde la
005) y se observa como flujo, de modo que tocar una bandera repinta la app entera al instante.

La pieza técnica que hace posible ese «al instante» es un único composable en la raíz,
`ProveedorIdioma`, que provee `LocalContext` y `LocalConfiguration` con la configuración del idioma
elegido. Verificado en las fuentes de Compose UI 1.12.0: `stringResource` lee `LocalResources`, que
es un `compositionLocalWithComputedDefaultOf` derivado de esos dos; proveerlos en la raíz alcanza a
las 184 llamadas a `stringResource` de la app, a los cinco `Toast` y a las fechas de `DateUtils`, sin
recrear la Activity, sin AppCompat y sin tocar ninguna pantalla existente (ver R2).

El resto es trabajo de recursos: cuatro `values-xx/strings.xml` nuevos con las 184 cadenas
traducibles, `translatable="false"` en 33, cinco banderas como VectorDrawable, y un test de paridad
que impide que las traducciones se desincronicen.

La regla de precedencia que pide la spec (FR-008, FR-011) vive en `domain/` como función pura:
`SeleccionIdioma.efectivo = elegido ?: sistema`. Nada más de esta feature toca lógica de cálculo: los
cuatro motores, el conversor de precios y la política de caché quedan intactos.

## Technical Context

**Language/Version**: Kotlin 2.2.10 (el Kotlin integrado de AGP 9.3.1; no se sube por libre)

**Primary Dependencies**: Jetpack Compose (BoM 2026.08.00, ui 1.12.0) + Material 3, Navigation
Compose 2.9.8 con rutas `@Serializable`, Koin 4.2.2 (BoM), corrutinas 1.11.0,
**androidx.datastore:datastore-preferences 1.2.1** (nueva; última estable verificada en
`dl.google.com/dl/android/maven2` — la rama 1.3.0 solo tiene alphas)

**Storage**: Preferences DataStore, fichero `ajustes`, una sola clave con la etiqueta BCP-47 del
idioma elegido. Convive con el `SharedPreferences` de la caché de cotizaciones (007), que no se toca.

**Testing**: JUnit 4.13.2 + kotlinx-coroutines-test + turbine 1.2.1 + mockk en JVM;
`createComposeRule()` en `androidTest` para el proveedor y la pantalla. `./gradlew :app:lint` como
segunda red (`MissingTranslation`, `ExtraTranslation`).

**Target Platform**: Android, minSdk 24 · targetSdk 36 · compileSdk 37. Todas las API que usa la
feature son ≤ 24 (`Configuration.setLocales`, `LocaleList` y `createConfigurationContext` entran en
24 o antes), así que no hace falta ninguna comprobación de versión.

**Project Type**: App Android de un solo módulo (`:app`), MVVM con `ui → domain ← data`.

**Performance Goals**: el cambio de idioma se ve en el fotograma siguiente al toque; el arranque no
añade lectura de disco en el hilo principal (la preferencia llega por flujo, no por bloqueo).

**Constraints**: sin AppCompat y sin cambiar el tema XML (`android:Theme.Material.NoActionBar` está
ajustado a mano para que el splash de Android 12+ no destelle en blanco); sin recrear la Activity;
sin tocar el formato numérico de los cinco ViewModels que formatean cifras; `domain/` sigue libre de
`android.*` y `androidx.*`.

**Scale/Scope**: 9 pantallas, 184 cadenas traducibles × 5 idiomas (920 cadenas visibles), 1 pantalla
nueva, 2 ViewModels nuevos, 2 casos de uso nuevos, 1 dependencia nueva, 6 drawables nuevos.

### Decisiones y sus porqués

Detalle completo en [research.md](./research.md); aquí el resumen de lo que fija el plan.

1. **El idioma se aplica proveyendo `LocalContext` y `LocalConfiguration` en la raíz** (R2). Es lo que
   permite el requisito FR-006 —cambio visible en la propia pantalla de Ajustes— sin recrear nada. Se
   descartan `AppCompatDelegate.setApplicationLocales` (exige la dependencia, `AppCompatActivity` y un
   tema AppCompat, y por debajo de API 33 no actúa sobre una `ComponentActivity`) y
   `attachBaseContext` + `recreate()` (lectura bloqueante en el arranque y pérdida del estado de la
   pantalla, que FR-022 prohíbe).
2. **La preferencia se guarda en DataStore y se observa** (R3). Es la desviación de la feature y está
   justificada en Complexity Tracking.
3. **«Automático» es la ausencia de elección**, modelada como `IdiomaApp?` nulo, no como un sexto
   valor del enum (R4). El enum se queda con los cinco idiomas que existen de verdad; el estado
   «sigue al sistema» no es un idioma y no debe poder llegar a `Locale.forLanguageTag`.
4. **El idioma del sistema se consulta tras interfaz**, `IdiomaSistema` en `core/util/`, con el mismo
   precedente que `Reloj` (R5): JVM puro sobre `Locale.getDefault()`, sustituible por un falso en los
   tests. Nada de leer el `Configuration` desde el ViewModel.
5. **La raíz no pinta hasta saber el idioma** (R6): `IdiomaAppUiState.idioma` nulo significa «aún no
   sé», y en ese estado no se compone el `NavHost`. Cumple FR-013 sin splash artificial: el hueco lo
   cubre el `windowBackground` del tema, que ya es el azul de la portada.
6. **El DataStore no entra en el grafo de Koin como `DataStore<Preferences>`** (R7): lo crea `by lazy`
   su propio data source, igual que `SharedPreferencesCotizacionesLocalDataSource` se guarda sus
   `SharedPreferences`. Así `verify()` sigue inspeccionando un constructor real y `KoinModulesTest` no
   necesita `extraTypes` nuevos.
7. **Los nombres de los idiomas y la marca no se traducen** (R8): endónimos y marca van con
   `translatable="false"`, que además convierte a `lint` en la red que vigila FR-018.
8. **No se declara `android:localeConfig`** (R9): el selector de idioma del sistema (API 33+) sería
   una segunda fuente de verdad que nuestra preferencia ignoraría.
9. **Los desbordes de texto son trabajo de esta feature** (R10): `JewelryBottomBar` y `BotonDorado`
   pintan hoy sus etiquetas sin límite de líneas ni autoajuste, y «Einstellungen» o «In Favoriten
   speichern» no caben. Se les aplica el mismo `TextAutoSize` que ya usa `SelectorSegmentado`.
10. **El formato numérico no se toca** (R11). Queda declarado fuera de alcance en la spec, con su
    motivo: alemán, francés e italiano comparten la coma decimal con el español y el cambio afectaría
    a los cinco motores y a sus pruebas.
11. **Las fechas dejan de usar `DateUtils`** (R12): toma el orden de la fecha de `Locale.getDefault()`
    —el del sistema— y con la app en un idioma y el móvil en otro incumplía FR-019.
    `DateFormat.getMediumDateFormat(contexto)` sí usa el locale del contexto localizado.
12. **El App Bundle no reparte los idiomas por splits** (R13): con el valor por defecto, Play
    instalaría solo el idioma del dispositivo y elegir una bandera no cambiaría nada en producción.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Estado | Cómo se cumple |
|---|---|---|
| **I — SDD** | ✅ | Rama `008-ajustes-idioma` y `spec.md` creados antes de este plan; `tasks.md` antes de tocar código de producto. La spec no nombra librerías ni clases. |
| **II — MVVM con capas estancas** | ✅ | `IdiomaApp`, `SeleccionIdioma`, `PreferenciasRepository` y los dos casos de uso son Kotlin puro en `domain/`; DataStore queda confinado en `data/source/local/`; `ui/` solo habla con `domain/`. Los dos ViewModels nuevos exponen un único `StateFlow` inmutable y no importan `androidx.compose.*`. Cada pantalla se parte en `AjustesScreen` (resuelve el ViewModel) y `AjustesContent` (sin estado, con `@Preview`). |
| **III — Koin, DSL manual** | ✅ | Cinco registros nuevos repartidos en los módulos existentes; ningún módulo nuevo, así que `featureModules` no cambia. Todo por constructor; el repositorio, por su interfaz de dominio. |
| **IV — Test obligatorio** | ✅ | Test unitario para los dos ViewModels y los dos casos de uso, más el repositorio, el modelo y la paridad de traducciones; dos tests instrumentados. `KoinModulesTest` cubre el grafo sin tocarlo. |
| **V — Una sola fuente de verdad para las versiones** | ✅ | DataStore entra por `gradle/libs.versions.toml` (`datastorePreferences = "1.2.1"`) y se declara con `libs.androidx.datastore.preferences`. Kotlin no se sube. |

**Restricciones técnicas**: minSdk 24 respetado sin comprobaciones de versión (todas las API usadas
son ≤ 24). Código y comentarios en español. Ningún producto nuevo de Firebase. `google-services.json`
no se toca.

**Puerta especial de esta feature**: `./gradlew :app:lint` pasa a ser puerta de calidad junto a los
tests, porque `MissingTranslation` y `ExtraTranslation` son la única red automática que ve el fichero
de recursos completo desde fuera.

**Re-evaluación tras el diseño de la Fase 1**: sin cambios. El diseño no introduce ninguna violación
nueva y la única desviación sigue siendo la dependencia de DataStore, justificada abajo.

## Project Structure

### Documentation (this feature)

```text
specs/008-ajustes-idioma/
├── spec.md              # Qué y por qué (/speckit-specify)
├── plan.md              # Este fichero (/speckit-plan)
├── research.md          # Fase 0: R1–R11
├── data-model.md        # Fase 1: entidades, estados y formato persistido
├── quickstart.md        # Fase 1: cómo validar la feature de punta a punta
├── contracts/
│   ├── preferencia-idioma.md   # Contrato del dato persistido
│   └── traducciones.md         # Contrato del fichero de recursos y sus reglas
├── checklists/
│   └── requirements.md  # Calidad de la spec (16/16)
└── tasks.md             # Fase 2 (/speckit-tasks — NO lo crea /speckit-plan)
```

### Source Code (repository root)

Ficheros **nuevos** (`app/src/main/java/com/jrblanco/calculadoradejoyeros2021/`):

```text
domain/
├── model/IdiomaApp.kt                        # enum de 5 idiomas + desdeEtiqueta()
├── model/SeleccionIdioma.kt                  # elegido ?: sistema  → efectivo
├── repository/PreferenciasRepository.kt      # Flow<IdiomaApp?> + guardarIdioma()
├── usecase/ObservarIdiomaUseCase.kt          # Flow<SeleccionIdioma>
└── usecase/GuardarIdiomaUseCase.kt

core/util/IdiomaSistema.kt                    # interfaz + IdiomaSistemaJvm (Locale.getDefault())

data/
├── source/local/AjustesLocalDataSource.kt            # interfaz
├── source/local/DataStoreAjustesLocalDataSource.kt   # DataStore, clave única
└── repository/PreferenciasRepositoryImpl.kt

ui/
├── idioma/ProveedorIdioma.kt                 # provee LocalContext + LocalConfiguration
├── idioma/IdiomaAppUiState.kt
├── idioma/IdiomaAppViewModel.kt              # el idioma efectivo para la raíz
├── ajustes/AjustesUiState.kt
├── ajustes/AjustesViewModel.kt
├── ajustes/AjustesScreen.kt                  # AjustesScreen + AjustesContent + FilaIdioma
└── ajustes/PresentacionAjustes.kt            # IdiomaApp → bandera y nombre
```

Recursos nuevos (`app/src/main/res/`):

```text
drawable/ic_bandera_es.xml · ic_bandera_en.xml · ic_bandera_fr.xml
drawable/ic_bandera_de.xml · ic_bandera_it.xml · ic_idioma.xml
values-en/strings.xml · values-fr/strings.xml · values-de/strings.xml · values-it/strings.xml
```

Ficheros **modificados**:

```text
MainActivity.kt                               # envuelve AppNavHost en ProveedorIdioma
ui/navigation/AppNavHost.kt                   # Route.Ajustes: placeholder → AjustesScreen
ui/components/JewelryBottomBar.kt             # etiqueta a una línea, autoajuste y ancho por tercios
ui/components/Botones.kt                      # ídem en BotonDorado
ui/welcome/WelcomeScreen.kt                   # el nombre del autor, por parámetro
ui/herramientas/precios/PresentacionPrecios.kt # fecha y hora con el locale del contexto (R12)
ui/herramientas/precios/PreciosMetalesContent.kt # el nombre del proveedor, por parámetro
core/di/CoreModule.kt · DataModule.kt · DomainModule.kt · ViewModelModule.kt
res/values/strings.xml                        # translatable="false" + cadenas nuevas
res/xml/backup_rules.xml · data_extraction_rules.xml  # por qué el idioma sí viaja
gradle/libs.versions.toml · app/build.gradle.kts      # DataStore y sin splits por idioma
CLAUDE.md                                     # guía operativa, en el mismo cambio
```

Tests nuevos:

```text
app/src/test/java/.../domain/model/IdiomaAppTest.kt
app/src/test/java/.../domain/model/SeleccionIdiomaTest.kt
app/src/test/java/.../domain/usecase/ObservarIdiomaUseCaseTest.kt
app/src/test/java/.../domain/usecase/GuardarIdiomaUseCaseTest.kt
app/src/test/java/.../data/repository/PreferenciasRepositoryImplTest.kt
app/src/test/java/.../data/repository/FakePreferenciasRepository.kt
app/src/test/java/.../data/source/local/FakeAjustesLocalDataSource.kt
app/src/test/java/.../core/util/IdiomaSistemaFalso.kt
app/src/test/java/.../ui/ajustes/AjustesViewModelTest.kt
app/src/test/java/.../ui/idioma/IdiomaAppViewModelTest.kt
app/src/test/java/.../recursos/TraduccionesTest.kt       # paridad de los 5 strings.xml
app/src/androidTest/java/.../ui/idioma/ProveedorIdiomaTest.kt
app/src/androidTest/java/.../ui/ajustes/AjustesScreenTest.kt
```

**Structure Decision**: se mantiene el módulo único `:app` con las tres capas y `core/`. La feature
estrena el paquete `ui/idioma/`, que no es una pantalla sino infraestructura de presentación
(un proveedor de composición y el ViewModel que lo alimenta): es el primer paquete de `ui/` sin ruta
propia, y va separado de `ui/ajustes/` precisamente porque su alcance es toda la app y no una
pantalla. `ui/ajustes/` sigue la forma de `ui/plata/`, la pantalla más corta del proyecto.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| **Dependencia nueva: `androidx.datastore:datastore-preferences`**, contra la regla del «segundo consumidor» que `specs/007-herramientas/plan.md` dejó escrita al rechazar DataStore | La preferencia de idioma **se observa**: de que un cambio en el dato llegue como emisión a la raíz de Compose depende FR-006 (la app se repinta al instante). Además necesita lectura asíncrona en el arranque para no bloquear el hilo principal (FR-013) | `SharedPreferences` obligaría a montar a mano el flujo (`OnSharedPreferenceChangeListener` → `callbackFlow`) y a leer en el hilo principal o duplicar hilos: más código propio, sin transaccionalidad y con el mismo resultado. El argumento que faltaba en la 007 —«nadie la observa»— es justo el que aquí se cumple: la 007 guardaba una caché que nadie miraba; esto es un ajuste que mira toda la app |
| **`ui/idioma/` no tiene ruta ni pantalla**, rompiendo la costumbre de «una carpeta de `ui/` por pantalla» | El idioma no es de ninguna pantalla: es del árbol de composición entero. Su ViewModel lo posee la Activity y su composable envuelve al `NavHost` | Meterlo en `ui/ajustes/` haría que `MainActivity` dependiera del paquete de una pantalla concreta y sugeriría que el idioma se apaga al salir de Ajustes. Meterlo en `ui/components/` mezclaría un proveedor con estado inyectado entre componentes sin estado |
| **Se modifican dos componentes compartidos** (`JewelryBottomBar`, `BotonDorado`) que no son de esta pantalla | FR-021 y SC-004 exigen que ninguna etiqueta desborde en los cinco idiomas, y son los dos únicos sitios donde el texto se pinta sin límite ni autoajuste | Traducir «Ajustes» al alemán con una palabra más corta que la correcta para que quepa sería falsear la traducción. Dejarlo sin tocar rompe FR-021 en cuanto se elige alemán |
