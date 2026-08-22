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
| `ui/` | `navigation/`, una carpeta por pantalla, `theme/` | `data.*` |
| `core/` | `di/` (módulos Koin), `ui/UiState.kt`, `util/DispatcherProvider.kt` | — |

Los SDK externos están confinados: **`FirebaseAnalyticsDataSource` es el único
fichero del proyecto que importa `com.google.firebase.*`** (junto a `core/di/FirebaseModule.kt`,
que los registra). Todo lo demás pasa por `domain/repository/AnalyticsRepository`.

`ui/home/` es la pantalla de referencia: copia su forma al crear una nueva.

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

## Commits

Conventional Commits (`feat:`, `fix:`, `build:`, `test:`, `docs:`, `chore:`,
`refactor:`), en español. Es también lo que genera `/speckit-git-commit`.
