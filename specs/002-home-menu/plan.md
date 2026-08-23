# Implementation Plan: Home (menú de inicio)

**Branch**: `002-home-menu` | **Date**: 2026-08-23 | **Spec**: [spec.md](./spec.md)

## Summary

Menú principal con cuatro tarjetas de módulo, barra superior compartida y barra inferior de tres destinos. Siete pantallas placeholder cierran todas las rutas de navegación para que la app sea transitable de punta a punta antes de escribir ninguna calculadora.

## Technical Context

Sin cambios respecto a la feature 001: Kotlin 2.2.10, Compose BOM 2026.08.00, Material 3 1.4.0, Navigation Compose 2.9.8 con rutas `@Serializable`, Koin 4.2.2, minSdk 24.

**Esta feature no añade ninguna dependencia.**

### Decisiones y sus porqués

| Decisión | Motivo |
|---|---|
| Iconos como vector drawables propios | `material-icons-core` **no está en el classpath**: Material3 1.4.0 dejó de arrastrarlo y `Icons.Default.*` no compila. La librería está deprecada y la design spec pide iconografía de identidad propia, así que se dibujan en `res/drawable` en vez de añadir dependencia |
| `HomeModule` como enum sin `R` | Mantiene el ViewModel libre de Android y su test en JVM. El mapeo a imagen, textos y color vive en la capa Compose |
| Un `PlaceholderScreen` parametrizado | Siete ficheros casi idénticos serían deuda desde el minuto uno. Cuando una sección reciba su feature real, cambia solo su cableado en el grafo |
| Cada pantalla declara su propio *chrome* | La alternativa —un `Scaffold` sobre el `NavHost` que deduzca las barras husmeando la ruta actual— es implícita y haría parpadear las barras al entrar en la portada, que no lleva ninguna |
| `ContentScale.Fit` en la imagen de tarjeta | `herramientas` es apaisada y las otras tres cuadradas; con `Crop` se perderían el calibre y la lupa (FR-002, SC-003) |
| `LazyColumn` para los módulos | Cuatro tarjetas más las dos barras no caben en pantallas pequeñas (FR-005) |

## Constitution Check

| Principio | Cumplimiento |
|---|---|
| I. SDD obligatorio | spec → plan → tasks → implement |
| II. MVVM con capas estancas | `HomeViewModel` expone un `StateFlow<HomeUiState>` y no importa Compose ni `R`. La telemetría entra por `AnalyticsRepository` |
| III. DI solo por Koin | `viewModelOf(::HomeViewModel)`, ya registrado. Los placeholders no llevan ViewModel: no tienen estado ni comportamiento |
| IV. Test obligatorio | `HomeViewModelTest` (JVM) y `HomeScreenTest` (instrumentado) |
| V. Versiones en `libs.versions.toml` | No se añaden dependencias |

Sin desviaciones que declarar. La de la feature 001 (`WelcomeViewModel` sin `StateFlow`) sigue vigente y documentada allí.

## Project Structure

```
app/src/main/
├── java/com/jrblanco/calculadoradejoyeros2021/ui/
│   ├── components/                  (N) primeros componentes reutilizables
│   │   ├── JewelryTopBar.kt             logo centrado | atrás + título; info a la derecha
│   │   ├── JewelryBottomBar.kt          3 destinos, 88dp, activo en dorado
│   │   ├── JewelryScaffold.kt           combina ambas; cada pantalla declara su chrome
│   │   └── ModuleCard.kt                imagen + textos + chevron, con color de acento
│   ├── home/
│   │   ├── HomeModule.kt            (N) enum puro, sin R
│   │   ├── HomeUiState.kt           (M) reescrito: lista de módulos
│   │   ├── HomeViewModel.kt         (M) reescrito
│   │   └── HomeScreen.kt            (M) reescrito: LazyColumn de ModuleCard
│   ├── placeholder/
│   │   └── PlaceholderScreen.kt     (N) un composable para los 7 destinos
│   └── navigation/
│       ├── Routes.kt                (M) + 7 destinos
│       └── AppNavHost.kt            (M) cableado completo
└── res/
    ├── drawable-nodpi/              (N) modulo_oro/plata/soldaduras/herramientas.png
    ├── drawable/                    (N) ic_home, ic_favoritos, ic_ajustes,
    │                                    ic_chevron, ic_info, ic_atras
    └── values/strings.xml           (M) módulos, barra inferior y placeholders

app/src/test/.../ui/home/HomeViewModelTest.kt       (M) reescrito
app/src/androidTest/.../ui/home/HomeScreenTest.kt   (N)
```

## Complexity Tracking

Sin complejidad que justificar. No hay dependencias nuevas, capas nuevas ni datos
persistidos. `ui/components/` y `ui/placeholder/` amplían la estructura descrita en
`CLAUDE.md`, que se actualiza en el mismo cambio.
