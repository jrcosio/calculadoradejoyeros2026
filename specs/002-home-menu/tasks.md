# Tasks: Home (menú de inicio)

**Feature**: `002-home-menu` | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

`[P]` = paralelizable (ficheros distintos, sin dependencia entre ellas).

---

## Fase 1 — Recursos

- [x] **T001 [P]** Redimensionar a 512px las cuatro imágenes de `UI_Plantillas/Feature_home/` hacia `res/drawable-nodpi/`: `modulo_oro`, `modulo_plata`, `modulo_soldaduras`, `modulo_herramientas`. La de herramientas es apaisada (1672×941) y queda en 512×288.
- [x] **T002 [P]** Dibujar seis vector drawables en `res/drawable/`: `ic_home`, `ic_favoritos`, `ic_ajustes`, `ic_chevron`, `ic_info`, `ic_atras`. Trazo fino y grosor coherente entre ellos; se tintan en tiempo de ejecución.
- [x] **T003 [P]** `strings.xml`: títulos y descripciones de los 4 módulos según el mockup, etiquetas de la barra inferior, nombres de las 7 pantallas placeholder y descripciones de contenido de imágenes e iconos.

## Fase 2 — Componentes compartidos (`ui/components/`)

- [x] **T004** `JewelryTopBar.kt`: sin `title` → logo centrado; con `title` y `onBack` → flecha y nombre de sección. Info siempre a la derecha (FR-009, FR-010). Depende de T002 y T003.
- [x] **T005** `JewelryBottomBar.kt`: Home, Favoritos y Ajustes; 88dp; activo en `GoldPrimary` con subrayado, inactivos en `TextMuted` (FR-006). Depende de T002 y T003.
- [x] **T006** `JewelryScaffold.kt`: combina barra superior e inferior; la inferior es opcional (FR-007). Depende de T004 y T005.
- [x] **T007** `ModuleCard.kt`: imagen a la izquierda con `ContentScale.Fit`, título y descripción, chevron en círculo. Color de acento que tiñe borde, título y chevron (FR-002, FR-003). Depende de T002.

## Fase 3 — Home (`ui/home/`)

- [x] **T008** `HomeModule.kt`: `enum class HomeModule { ORO, PLATA, SOLDADURAS, HERRAMIENTAS }`. Kotlin puro, sin `R`.
- [x] **T009** `HomeUiState.kt` y `HomeViewModel.kt` reescritos: `StateFlow` con los cuatro módulos en el orden del mockup, `screen_view` al construirse y evento por módulo pulsado (FR-001, FR-012). Depende de T008.
- [x] **T010** `HomeScreen.kt` reescrito: `HomeScreen(onModuleClick)` resuelve el ViewModel; `HomeContent` sin estado pinta un `LazyColumn` de `ModuleCard` dentro de `JewelryScaffold`, con `@Preview` (FR-005). Depende de T006, T007 y T009.

## Fase 4 — Placeholders y navegación

- [x] **T011** `ui/placeholder/PlaceholderScreen.kt`: composable parametrizado con título y si lleva barra inferior (FR-011). Depende de T006.
- [x] **T012** `Routes.kt`: añadir `Favoritos`, `Ajustes`, `AcercaDe`, `Oro`, `Plata`, `Soldaduras`, `Herramientas`.
- [x] **T013** `AppNavHost.kt`: cablear los 7 destinos. Las 3 pestañas con `popUpTo(Route.Home)` y `launchSingleTop` para no apilar historial (FR-008); las secciones se apilan normal. `launchSingleTop` también blinda la doble pulsación sobre un módulo. Depende de T010, T011 y T012.

## Fase 5 — Tests

- [x] **T014** `HomeViewModelTest` reescrito: los 4 módulos en orden, `screen_view` al construirse y el evento correcto por módulo. Depende de T009.
- [x] **T015** `HomeScreenTest` (instrumentado): las 4 tarjetas se muestran con su título y pulsar una invoca el callback con el módulo correcto. Depende de T010.

## Fase 6 — Verificación

- [x] **T016** `:app:testDebugUnitTest` y `:app:assembleDebug` en verde. `WelcomeViewModelTest` debe seguir pasando: la 001 no se toca.
- [x] **T017** Emulador, contra `UI_Plantillas/Feature_home/ejemplo_homescreen.png`: las 4 tarjetas con su acento, **la imagen de herramientas entera** (SC-003), barra inferior solo en las 3 zonas principales (FR-007), los 6 iconos legibles y coherentes.
- [x] **T018** Recorrido completo: Home → cada sección → atrás; saltos entre las 3 pestañas sin apilar historial; la portada de la 001 sigue sin barras y "Comenzar" sigue funcionando.
- [x] **T019** `:app:connectedDebugAndroidTest` en verde.
- [x] **T020** `:app:assembleRelease`: R8 no rompe las rutas `@Serializable` nuevas. Anotar tamaño del APK.
- [x] **T021** Actualizar la tabla de estructura de `CLAUDE.md` con `ui/components/` y `ui/placeholder/`, como exige la constitución.

---

## Orden de ejecución

```
T001 [P] ─────────────────────────────┐
T002 [P] ──┬──> T004 ──┐              │
T003 [P] ──┘           ├──> T006 ──┬──┼──> T010 ──> T013 ──> T016..T021
           └──> T005 ──┘           │  │      │
           └──> T007 ──────────────┼──┘      │
T008 ──> T009 ─────────────────────┘         │
T012 ────────────────────────────────────────┘
T006 ──> T011 ───────────────────────────────┘
T009 ──> T014                      T010 ──> T015
```
