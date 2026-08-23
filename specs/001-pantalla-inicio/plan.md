# Implementation Plan: Pantalla de inicio

**Branch**: `001-pantalla-inicio` | **Date**: 2026-08-23 | **Spec**: [spec.md](./spec.md)

## Summary

Portada de la app con logo, título, ornamento, subtítulo, botón "Comenzar" y crédito de autoría, sobre el fondo de taller entregado por el propietario. Se convierte en el destino inicial del grafo de navegación. Arrastra la sustitución del tema de plantilla de Android Studio por los fundamentos del sistema de diseño dark luxury.

## Technical Context

**Lenguaje**: Kotlin 2.2.10 (lo fija el Kotlin integrado de AGP 9.3.1)
**UI**: Jetpack Compose, Material 3, Compose BOM 2026.08.00
**Navegación**: Navigation Compose 2.9.8 con rutas type-safe `@Serializable`
**DI**: Koin 4.2.2, DSL manual
**Telemetría**: Firebase Analytics vía `domain/repository/AnalyticsRepository`
**Tipografía**: TTF estáticos de Google Fonts empaquetados en `res/font`
**Testing**: JUnit4 + mockk + Turbine (JVM); Compose UI test (instrumentado)
**Plataforma**: Android, minSdk 24, targetSdk 36, compileSdk 37

### Decisiones y sus porqués

| Decisión | Motivo |
|---|---|
| Playfair Display para el título, Manrope para el resto | El mockup usa un serif de alto contraste; la design spec pide Manrope. Se resuelve por ámbito: el serif es excepción de portada, Manrope manda en el sistema |
| TTF estáticos empaquetados, no variables | El repo de Google Fonts ya solo publica fuentes variables y `FontVariation` requiere API 26. Con `minSdk` 24, en Android 7 los pesos se ignorarían y los títulos saldrían en regular |
| TTF empaquetados, no Downloadable Fonts | Evita el salto de fuente en el primer arranque, justo en la pantalla que es la portada |
| Se elimina `dynamicColor` | El color dinámico de Android 12+ repintaría la marca con el fondo de pantalla del usuario (FR-007) |
| Logo redimensionado a 640px; fondo intacto | El fondo (941×1672) ya es más pequeño que una pantalla actual: reducirlo solo lo degradaría. El logo (1254×1254) se pinta a ~190dp, así que sobra tamaño |
| `popUpTo(Welcome) { inclusive = true }` | FR-005: el retroceso desde la home cierra la app |

## Constitution Check

| Principio | Cumplimiento |
|---|---|
| I. SDD obligatorio | Esta feature estrena el ciclo: spec → plan → tasks → implement |
| II. MVVM con capas estancas | La pantalla no toca `data/`; la telemetría entra por la interfaz de dominio `AnalyticsRepository`. Composable partido en resolutor de ViewModel + contenido sin estado |
| III. DI solo por Koin | `viewModelOf(::WelcomeViewModel)` en `viewModelModule`, ya dentro de `featureModules` |
| IV. Test obligatorio | `WelcomeViewModelTest` (JVM) + `WelcomeScreenTest` (instrumentado). `KoinModulesTest` cubre el grafo sin tocarlo |
| V. Versiones en `libs.versions.toml` | La feature **no añade dependencias**: Compose, Navigation y Koin ya están |

### Desviación declarada

**`WelcomeViewModel` no expone ningún `StateFlow`.** El principio II dice que todo ViewModel expone uno. Esta pantalla es completamente estática: no hay nada que observar. Inventar un `WelcomeUiState` con un campo artificial cumpliría la letra de la norma y empeoraría el código. El ViewModel existe porque hay una responsabilidad real —la telemetría— que un Composable no puede asumir sin saltarse la separación de capas. La norma se mantiene para ViewModels **con estado**.

## Project Structure

### Documentation (this feature)

```
specs/001-pantalla-inicio/
├── spec.md
├── plan.md
├── tasks.md
└── checklists/requirements.md
```

### Source Code (repository root)

```
app/src/main/
├── java/com/jrblanco/calculadoradejoyeros2021/
│   ├── MainActivity.kt              (M) enableEdgeToEdge en estilo oscuro
│   ├── core/di/ViewModelModule.kt   (M) registrar WelcomeViewModel
│   └── ui/
│       ├── theme/
│       │   ├── Color.kt             (M) tokens dark luxury, fuera el morado
│       │   ├── Type.kt              (M) escala sobre Manrope + TitleSerif
│       │   ├── Theme.kt             (M) darkColorScheme fijo, sin dynamicColor
│       │   └── Dimens.kt            (N) radios y espaciados
│       ├── navigation/
│       │   ├── Routes.kt            (M) + Route.Welcome
│       │   └── AppNavHost.kt        (M) startDestination = Welcome
│       └── welcome/
│           ├── WelcomeScreen.kt     (N) WelcomeScreen + WelcomeContent + Preview
│           └── WelcomeViewModel.kt  (N)
└── res/
    ├── font/                        (N) manrope 400/600/700, playfair 700
    ├── drawable-nodpi/              (N) fondo_taller.png, logo_calculadora.png
    ├── values/strings.xml           (M) app_name + textos de la portada
    ├── values/themes.xml            (M) padre oscuro + windowBackground
    └── values-v31/themes.xml        (N) windowSplashScreenBackground

app/src/test/.../ui/welcome/WelcomeViewModelTest.kt        (N)
app/src/androidTest/.../ui/welcome/WelcomeScreenTest.kt    (N)
```

`UI_Plantillas/` permanece como carpeta de referencia de diseño, fuera de `res/`.

## Complexity Tracking

Sin complejidad que justificar. La feature no añade dependencias, no introduce
capas nuevas y no persiste datos. La única desviación de la constitución está
declarada arriba.
