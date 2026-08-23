# Tasks: Pantalla de inicio

**Feature**: `001-pantalla-inicio` | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

`[P]` = paralelizable (ficheros distintos, sin dependencia entre ellas).

---

## Fase 1 — Fundamentos del sistema de diseño

- [x] **T001** Descargar los TTF estáticos de Google Fonts a `app/src/main/res/font/`:
  `manrope_regular` (400), `manrope_semibold` (600), `manrope_bold` (700), `playfair_display_bold` (700).
  Usar el endpoint css2 con User-Agent de Android 4.3, que es el que devuelve TrueType estático por peso.
  **Verificar** que cada fichero es `TrueType Font data`, no EOT ni woff.
- [x] **T002 [P]** `ui/theme/Color.kt`: sustituir la paleta morada de plantilla por los tokens de la design spec.
- [x] **T003 [P]** `ui/theme/Dimens.kt` (nuevo): radios (12/18/28/34/pill) y espaciados (4/8/12/16/24/32).
- [x] **T004** `ui/theme/Type.kt`: `Typography` de Material 3 sobre Manrope siguiendo la escala de la spec, más `TitleSerif` con Playfair para la portada. Depende de T001.
- [x] **T005** `ui/theme/Theme.kt`: `darkColorScheme` con los tokens de T002. **Eliminar `dynamicColor` y el parámetro `darkTheme`** (FR-007: tema oscuro fijo). Depende de T002 y T004.

## Fase 2 — Recursos

- [x] **T006 [P]** `sips -Z 640` sobre `UI_Plantillas/logo.png` → `res/drawable-nodpi/logo_calculadora.png`.
- [x] **T007 [P]** Copiar `UI_Plantillas/fondo.png` → `res/drawable-nodpi/fondo_taller.png` **sin redimensionar** (ya es menor que una pantalla actual).
- [x] **T008 [P]** `res/values/strings.xml`: `app_name` → "Calculadora de Joyeros" (FR-010) y añadir `welcome_title`, `welcome_subtitle`, `welcome_start`, `welcome_developer`, más las descripciones de contenido de logo y fondo.
- [x] **T009 [P]** `res/values/themes.xml`: padre `android:Theme.Material.NoActionBar` (hoy es `.Light`, que provoca el destello blanco del FR-008) y `windowBackground` oscuro. Añadir `res/values-v31/themes.xml` con `windowSplashScreenBackground`.

## Fase 3 — Pantalla (US1 + US3)

- [x] **T010** `ui/welcome/WelcomeViewModel.kt`: registra `screen_view` al construirse y un evento al pulsar Comenzar, vía `AnalyticsRepository` (FR-011). Sin `StateFlow`: ver desviación declarada en el plan.
- [x] **T011** `core/di/ViewModelModule.kt`: `viewModelOf(::WelcomeViewModel)`. Depende de T010.
- [x] **T012** `ui/welcome/WelcomeScreen.kt`: `WelcomeScreen(onStart)` resuelve el ViewModel con `koinViewModel()`; `WelcomeContent(onStart)` sin estado, con `@Preview`.
  Composición: fondo a `ContentScale.Crop` → logo ~190dp → título con degradado dorado → ornamento → subtítulo → botón pill con borde dorado → crédito anclado abajo con `navigationBarsPadding()`.
  Depende de T004–T009.
- [x] **T013** `ui/navigation/Routes.kt`: añadir `@Serializable data object Welcome : Route`.
- [x] **T014** `ui/navigation/AppNavHost.kt`: `startDestination = Route.Welcome` (FR-001) y navegación a Home con `popUpTo(Welcome) { inclusive = true }` (FR-005). Depende de T012 y T013.
- [x] **T015** `MainActivity.kt`: `enableEdgeToEdge` en estilo oscuro para que los iconos de la barra de estado se lean sobre el fondo.

## Fase 4 — Tests

- [x] **T016** `WelcomeViewModelTest` (JVM): verifica el registro de `screen_view` al construirse y el evento al invocar la acción de Comenzar. Depende de T010.
- [x] **T017** `WelcomeScreenTest` (instrumentado): sobre `WelcomeContent`, comprueba que el botón existe y que su pulsación dispara el callback. Depende de T012.
- [x] **T018** Borrar `HomeViewModelTest` **no**: se mantiene. Solo verificar que `KoinModulesTest` sigue en verde con el ViewModel nuevo.

## Fase 5 — Verificación

- [x] **T019** `./gradlew :app:testDebugUnitTest` y `:app:assembleDebug` en verde.
- [x] **T020** Instalar en el emulador y comprobar contra `UI_Plantillas/screen_inicio_ejemplo.png`:
  sin destello blanco al arrancar (SC-005), **las tildes se ven** en "Precisión", "cálculo" y "José Ramón" (SC-004),
  el degradado dorado se aprecia, "Comenzar" navega y atrás cierra la app (SC-007), el lanzador dice "Calculadora de Joyeros".
- [x] **T021** `./gradlew :app:connectedDebugAndroidTest` con el emulador arrancado.
- [x] **T022** `./gradlew :app:assembleRelease`: confirmar que R8 no rompe las rutas `@Serializable` ni los recursos, y anotar el tamaño final del APK.

---

## Orden de ejecución

```
T001 ──> T004 ──┐
T002 ──────────┼──> T005 ──┐
T003 ──────────┘           │
T006 T007 T008 T009 [P] ───┼──> T012 ──> T014 ──> T019 ──> T020 ──> T021 ──> T022
T010 ──> T011 ─────────────┘      │
T013 ─────────────────────────────┘
T010 ──> T016                  T012 ──> T017
```
