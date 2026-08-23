# Tasks: Pantalla de Información («Acerca de»)

**Feature**: `003-info-acerca-de` | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

## Formato: `[ID] [P?] [Story] Descripción`

- **[P]**: paralelizable — ficheros distintos, sin dependencia con tareas pendientes.
- **[Story]**: historia de usuario a la que sirve (US1, US2, US3). Las fases de Setup,
  Fundacional y Pulido no llevan etiqueta.
- Rutas de fichero relativas a la raíz del repositorio.

**Tests**: sí. La constitución (principio IV) los exige para todo ViewModel, y la feature 002
dejó precedente de test instrumentado por pantalla.

---

## Fase 1 — Setup: recursos compartidos

**Propósito**: dejar en `res/` todo lo que la pantalla necesita pintar.

- [X] T001 [P] Redimensionar a 512 px las tres imágenes de `UI_Plantillas/Feature_Info/` hacia `app/src/main/res/drawable-nodpi/` con `sips -Z 512 <origen> --out <destino>`: `jr.png` → `foto_jrblanco.png`, `logoblanco.png` → `logo_blanco_joyeros.png`, `imagen_joya.png` → `joya_lupa.png`. Los originales se quedan donde están, como plantilla de diseño.
- [X] T002 [P] Dibujar tres vector drawables monocromos en `app/src/main/res/drawable/`: `ic_linkedin.xml`, `ic_instagram.xml` y `ic_enlace_externo.xml`. Viewport 24×24, grosor de trazo 1.5–1.8 coherente con `ic_info` e `ic_chevron`, sin color propio: se tiñen con `Icon(tint = …)`.
- [X] T003 [P] Añadir el bloque `<!-- Pantalla de información -->` en `app/src/main/res/values/strings.xml`: `info_titulo`, `info_perfil_nombre`, `info_perfil_descripcion`, `info_perfil_etiquetas`, `info_perfil_foto`, `info_linkedin_titulo`, `info_instagram_titulo`, `info_enlace_abrir`, `info_blanco_joyeros_titulo`, `info_blanco_joyeros_descripcion`, `info_blanco_joyeros_logo`, `info_blanco_joyeros_imagen`, `info_version` (FR-002 a FR-007, FR-010, FR-015, FR-016). `pantalla_acerca_de` **no** se borra aquí: sigue en uso hasta T009.

---

## Fase 2 — Fundacional: piezas que bloquean a todas las historias

**⚠️ CRÍTICO**: ninguna historia puede empezar hasta que esta fase esté completa.

- [X] T004 Mover `DiamondDivider` y su ayudante `GoldHairline` desde `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/welcome/WelcomeScreen.kt` a un fichero nuevo `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/components/Ornamentos.kt`, públicos y con un parámetro nuevo `widthFraction: Float = 0.7f` para que dentro de una tarjeta ocupe todo el ancho. `WelcomeScreen.kt` pasa a importarlos; la portada debe verse idéntica.
- [X] T005 [P] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/info/InfoEnlace.kt`: `enum class InfoEnlace(val url: String)` con `LINKEDIN("https://www.linkedin.com/in/jr-blanco/")` e `INSTAGRAM("https://www.instagram.com/blancojoyeros/")` y `val analyticsId: String get() = name.lowercase()`. Kotlin puro, sin `R`, sin `Color`, sin `android.*`.
- [X] T006 [P] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/info/InfoUiState.kt`: `data class InfoUiState(val enlaces: List<InfoEnlace> = emptyList(), val abriendoEnlace: Boolean = false)`. El segundo campo es la guarda de doble pulsación de FR-017; en US1 nace y se queda a `false`.

**Checkpoint**: el ornamento es compartido y el modelo de la pantalla existe.

---

## Fase 3 — User Story 1: saber quién está detrás de la app (P1) 🎯 MVP

**Objetivo**: el control de información deja de llevar a andamiaje y lleva a una pantalla
que presenta al autor, el propósito de la app y el bloque de Blanco Joyeros.

**Test independiente**: abrir la pantalla desde el icono de info de cualquier pantalla y
comprobar que se ve el perfil completo y que la flecha devuelve al origen. No necesita
ninguno de los accesos externos.

- [X] T007 [US1] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/info/InfoViewModel.kt`: `class InfoViewModel(private val analytics: AnalyticsRepository) : ViewModel()` con un único `StateFlow<InfoUiState>` inicializado con `InfoEnlace.entries` y `analytics.logScreenView("acerca_de")` en el `init` (FR-013). Sin importar `androidx.compose.*` ni `R`.
- [X] T008 [US1] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/info/InfoScreen.kt` con los dos composables: `InfoScreen(onInfo, onBack, modifier, viewModel = koinViewModel())` que resuelve el estado con `collectAsStateWithLifecycle()`, e `InfoContent(...)` sin estado y con `@Preview(widthDp = 411, heightDp = 891)` privado. Dentro de `JewelryScaffold(onInfo, title = null, onBack = onBack)`: `Box` con `R.drawable.fondo_taller` a `alpha = 0.30f` y `ContentScale.Crop`, y encima `Column` con `verticalScroll(rememberScrollState())` —el estado recordado es lo que devuelve al joyero al mismo punto al volver de la red social— (FR-011, FR-012) que pinta el título `info_titulo` en `displayLarge` con degradado dorado y `semantics { heading() }`, la `PerfilCard` privada (foto circular con aro dorado, nombre, `DiamondDivider`, descripción y fila de etiquetas) y la `BlancoJoyerosCard` privada (logotipo circular, título, `DiamondDivider`, texto e imagen de joyería), **no accionable** (FR-003, FR-004, FR-007). Todas las imágenes con `contentDescription` (FR-015). **Depende de T007**: el composable con estado resuelve `InfoViewModel` con `koinViewModel()`.
- [X] T009 [US1] En `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/navigation/AppNavHost.kt`, sustituir el cuerpo de `composable<Route.AcercaDe>` por `InfoScreen(onInfo = onInfo, onBack = onBack)` (FR-001) y borrar de `app/src/main/res/values/strings.xml` la clave `pantalla_acerca_de`, que queda sin uso. `Routes.kt` no se toca.
- [X] T010 [US1] Registrar `viewModelOf(::InfoViewModel)` en `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/core/di/ViewModelModule.kt`. No hace falta módulo nuevo: `KoinModulesTest` lo cubre solo.
- [X] T011 [P] [US1] Crear `app/src/test/java/com/jrblanco/calculadoradejoyeros2021/ui/info/InfoViewModelTest.kt` con MockK + Turbine + `runTest`, nombres en backticks y en español: el estado inicial trae los dos enlaces en orden, `logScreenView("acerca_de")` se registra exactamente una vez al construirse, y los `analyticsId` son únicos.
- [X] T012 [P] [US1] Crear `app/src/androidTest/java/com/jrblanco/calculadoradejoyeros2021/ui/info/InfoScreenTest.kt` montando `InfoContent` directo dentro de `Calculadoradejoyeros2021Theme`, sin Koin ni NavHost: comprueba que se muestran el título, el nombre del autor, el texto de presentación y el título de Blanco Joyeros, y que la tarjeta de Blanco Joyeros **no** tiene acción de click.

**Checkpoint**: US1 funciona sola. Ningún destino de la barra superior lleva ya a andamiaje (SC-007).

---

## Fase 4 — User Story 2: llegar a LinkedIn e Instagram (P2)

**Objetivo**: dos accesos externos que abren la app nativa de la red o, en su defecto, el
navegador, sin tumbar la app si no hay ninguno.

**Test independiente**: tocar cada acceso y comprobar el perfil que se abre; repetirlo con
esas apps desinstaladas y comprobar que se abre en el navegador.

- [X] T013 [US2] Añadir a `ui/info/InfoScreen.kt` el composable privado `EnlaceCard(iconRes, acento, titulo, urlVisible, onClick)` —icono en cuadrado redondeado, título en el acento, dirección visible en `TextSecondary` y `ic_enlace_externo` dentro del círculo de 44 dp, al estilo del chevron de `ModuleCard`— y el mapeo privado `InfoEnlace.presentation()` con icono, acento y textos. Los dos acentos de marca van como `private val` del fichero, aclarados para contrastar sobre `Background`. Tarjeta entera `clickable(role = Role.Button, onClickLabel = info_enlace_abrir)`, icono con `contentDescription = null` (FR-005, FR-006, FR-014).
- [X] T014 [US2] Añadir a `ui/info/InfoViewModel.kt` tres métodos (FR-009, FR-013, FR-017):
  - `onEnlacePulsado(enlace: InfoEnlace): Boolean` — si `abriendoEnlace` ya está a `true`, devuelve `false` y no hace nada; si no, levanta la guarda, registra `logEvent("acerca_de_enlace_abierto", mapOf("enlace" to enlace.analyticsId))` y devuelve `true`.
  - `onEnlaceFallido(error: Throwable)` — baja la guarda y llama a `analytics.recordError(error)`.
  - `onPantallaVisible()` — baja la guarda. Es lo que rehabilita los accesos al volver de la red social.
- [X] T015 [US2] En `InfoScreen` (el composable con estado), obtener `LocalUriHandler.current` y, al pulsar una tarjeta, consultar primero `viewModel.onEnlacePulsado(enlace)`: si devuelve `false` la pulsación se ignora (FR-017, SC-010); si devuelve `true`, envolver `openUri(enlace.url)` en `runCatching` y, en caso de fallo, llamar a `onEnlaceFallido(error)`. `AndroidUriHandler` lanza `IllegalArgumentException` cuando no hay actividad capaz de atender el enlace y sin capturarla la app se cerraría (FR-008, FR-009, SC-004). Añadir además `LifecycleResumeEffect(Unit) { viewModel.onPantallaVisible(); onPauseOrDispose { } }` de `lifecycle-runtime-compose` —ya en el classpath— para bajar la guarda al regresar de la aplicación externa.
- [X] T016 [P] [US2] Ampliar `app/src/test/java/com/jrblanco/calculadoradejoyeros2021/ui/info/InfoViewModelTest.kt`: cada enlace registra el evento con su `analyticsId`; un fallo de apertura llega a `recordError` con la excepción recibida; una segunda pulsación con la guarda levantada devuelve `false` y **no** registra un segundo evento (FR-017, SC-010); y tras `onPantallaVisible()` la siguiente pulsación vuelve a registrar.
- [X] T017 [P] [US2] Ampliar `app/src/androidTest/java/com/jrblanco/calculadoradejoyeros2021/ui/info/InfoScreenTest.kt`: las dos tarjetas de enlace se muestran con su nombre de red y su dirección, tienen acción de click, y pulsarlas propaga el `InfoEnlace` esperado al callback.

**Checkpoint**: US1 y US2 funcionan de forma independiente.

---

## Fase 5 — User Story 3: identificar la versión instalada (P3)

**Objetivo**: la versión de la app visible al pie, leída de la propia compilación.

**Test independiente**: comparar la versión del pie con la de la app instalada en el dispositivo.

- [X] T018 [US3] Añadir a `InfoContent` en `ui/info/InfoScreen.kt` el parámetro `versionName: String = BuildConfig.VERSION_NAME` y pintar al pie `stringResource(R.string.info_version, versionName)` en `labelMedium` / `TextMuted`, centrado (FR-010). El valor por defecto evita meter un `String` en el grafo de Koin.
- [X] T019 [US3] Ampliar `app/src/androidTest/java/com/jrblanco/calculadoradejoyeros2021/ui/info/InfoScreenTest.kt`: montando `InfoContent` con un `versionName` fijo, el pie muestra ese valor.

**Checkpoint**: las tres historias funcionan de forma independiente.

---

## Fase 6 — Pulido y verificación

- [X] T020 [P] Actualizar `CLAUDE.md`: la sección «Pantallas aún sin desarrollar» pasa de siete destinos a seis, `ui/info/` se documenta como segunda pantalla de referencia y `Ornamentos.kt` se añade a la lista de componentes compartidos. Lo exige la constitución al cambiar la estructura.
- [X] T021 `./gradlew :app:testDebugUnitTest` y `./gradlew :app:assembleDebug` en verde, con `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`. `KoinModulesTest`, `HomeViewModelTest` y `WelcomeViewModelTest` deben seguir pasando: las features 001 y 002 no cambian de comportamiento.
- [X] T022 `./gradlew :app:lint` sin `UnusedResources` tras borrar `pantalla_acerca_de` en T009.
- [X] T023 Comprobación visual en emulador contra `UI_Plantillas/Feature_Info/ejemplo_info.png` (SC-009): orden de los bloques, acentos de cada tarjeta, foto recortada en círculo con aro dorado y fondo del taller apenas insinuado. Recorrido de navegación: info desde Home → atrás vuelve a Home; info desde una sección → atrás vuelve a esa sección; pulsar el icono de info **estando ya en Info** no apila historial, y una sola pulsación de atrás sigue devolviendo al origen (SC-001, SC-006).
- [X] T024 Comprobación de accesibilidad: con TalkBack, las dos tarjetas se anuncian como botón con su red y la etiqueta de acción, y las tres imágenes tienen descripción; con la fuente del sistema al doble, ningún texto se recorta y todo el contenido se alcanza con desplazamiento; y las dos tarjetas de enlace superan los 48 dp de alto que fija `JewelrySize.MinTouchTarget` (FR-014, SC-003, SC-008).
- [X] T025 Comprobación de los enlaces en dispositivo: con las apps de LinkedIn e Instagram instaladas se abren en ellas; desinstaladas, en el navegador. Verificar en Firebase DebugView que llegan `screen_view` con `acerca_de` y `acerca_de_enlace_abierto` con el parámetro `enlace`.
- [X] T026 `./gradlew :app:connectedDebugAndroidTest` en verde.
- [X] T027 `./gradlew :app:assembleRelease`: R8 no rompe la pantalla nueva. Anotar el crecimiento del APK por las tres imágenes.

---

## Dependencias y orden de ejecución

### Dependencias entre fases

- **Fase 1 (Setup)**: sin dependencias, puede empezar ya. Las tres tareas son paralelas.
- **Fase 2 (Fundacional)**: T005 y T006 no dependen de nada; T004 toca `WelcomeScreen`. Bloquea todas las historias.
- **Fase 3 (US1)**: depende de las fases 1 y 2 completas. Dentro de ella, T008 depende de T007 (resuelve el ViewModel), así que no es paralelizable con él.
- **Fase 4 (US2)** y **Fase 5 (US3)**: dependen de US1, porque amplían los mismos ficheros.
- **Fase 6 (Pulido)**: depende de las historias que se quieran entregar.

### Diagrama

```
T001 [P] ─┐
T002 [P] ─┼───────────────────────────┐
T003 [P] ─┘                           │
T004 ─────────────────────────────────┤
                                      ├──> T008 ──> T009 ──> T013 ──> T014 ──> T015 ──> T018 ──> T020..T027
T005 [P] ──┬──> T007 ─────────────────┘       │                                 │          │
T006 [P] ──┘      │                           └──> T010                         │          │
                  └──> T011 [P]      T012 [P]              T016 [P]  T017 [P] ──┘          └──> T019 [P]
```

### Oportunidades de paralelismo

- Fase 1 entera: T001, T002 y T003 tocan ficheros distintos.
- Fase 2: T005 y T006 en paralelo; T004 va aparte porque toca `WelcomeScreen`.
- Dentro de cada historia, los tests (T011/T012, T016/T017, T019) van en paralelo con el
  resto una vez existe el código que prueban.
- **Entre historias no hay paralelismo real**: US1, US2 y US3 amplían los mismos tres
  ficheros de `ui/info/`. Es una pantalla, no tres. Se ejecutan en orden de prioridad.

---

## Estrategia de implementación

### MVP primero (solo US1)

1. Fase 1 (Setup) → 2. Fase 2 (Fundacional) → 3. Fase 3 (US1).
4. **PARAR Y VALIDAR**: el icono de info ya no lleva a andamiaje y la pantalla presenta al
   autor. Es entregable por sí solo.

### Entrega incremental

1. Setup + Fundacional → base lista.
2. US1 → validar → la pantalla ya cuenta quién hizo la app (MVP).
3. US2 → validar → los dos accesos externos funcionan, también sin navegador.
4. US3 → validar → la versión aparece al pie.
5. Fase 6 → puertas de calidad, accesibilidad y documentación.

---

## Notas

- `[P]` = ficheros distintos, sin dependencias pendientes.
- Ningún fichero de `domain/` ni de `data/` se toca: la telemetría ya tiene su interfaz.
- `Routes.kt` no se toca: `Route.AcercaDe` existe desde la feature 002.
- No se añade ninguna dependencia; `gradle/libs.versions.toml` no cambia.
- Commit por tarea o por grupo lógico, con Conventional Commits en español y scope `003`.
- Un `tasks.md` no se cierra con tests en rojo (constitución, principio IV).

---

## Resultado de la verificación (2026-08-23, emulador Pixel_10 API 17)

- **T021** `:app:testDebugUnitTest` 16/16 en verde (7 de `InfoViewModelTest`); `:app:assembleDebug` correcto.
- **T022** `:app:lint`: 11 avisos, **todos previos a esta feature** (versiones nuevas disponibles, orientación bloqueada, iconos de launcher). Ningún `UnusedResources` sobre los recursos nuevos ni sobre `pantalla_acerca_de`, ya retirado.
- **T023** La pantalla coincide con el mockup salvo las dos divergencias acordadas. Pulsar el icono de info tres veces estando en Info y luego atrás **una sola vez** devuelve a Home: `launchSingleTop` no apila.
- **T024** Objetivos táctiles medidos en el árbol de accesibilidad: tarjetas de enlace **80 × 387 dp**, flecha e icono de info **48 × 48 dp**. Las tres imágenes con significado exponen descripción; el fondo del taller va sin ella, por decorativo. Con la fuente del sistema al doble no se recorta ningún texto y el pie sigue alcanzable con desplazamiento.
- **T025** LinkedIn e Instagram abren Chrome (única app capaz de atender `https` en el emulador). En Firebase (logging verboso de FA) llegan `screen_view` con `acerca_de` y un `acerca_de_enlace_abierto` por red con el parámetro `enlace`. **Dos pulsaciones rápidas sobre LinkedIn produjeron un único evento** (FR-017, SC-010) y, tras volver de Chrome, el segundo acceso volvió a abrirse: la guarda baja sola. *Pendiente de comprobar en un dispositivo con las apps de LinkedIn e Instagram instaladas*: el emulador no las tiene, así que la rama «app nativa» de FR-008 no se ha podido observar, solo la del navegador.
- **T026** `:app:connectedDebugAndroidTest` 12/12 en verde (5 de `InfoScreenTest`).
- **T027** `:app:assembleRelease` correcto. APK de 6,35 MB; las tres imágenes nuevas pesan ~0,6 MB dentro del paquete. Firmado con la clave de debug e instalado: la pantalla se pinta igual que en debug y no hay `FATAL EXCEPTION`, así que R8 no se lleva nada por delante.

---

## Fase 7 — Enmienda: sin acceso a Información dentro de Información (FR-018)

Petición del propietario posterior a la implementación. La spec la recogía como suposición
en sentido contrario, así que se actualizó primero (FR-018) y después el código, como exige
el principio I de la constitución.

- [X] T028 En `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/components/JewelryTopBar.kt`, hacer `onInfo` nulable (`onInfo: (() -> Unit)?`): cuando es `null` se pinta un `Box` de 48 dp en lugar del `IconButton`, para que el hueco siga siendo simétrico al de la izquierda y el logo no se descentre (FR-018).
- [X] T029 Propagar el `onInfo` nulable en `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/components/JewelryScaffold.kt`. El resto de pantallas no cambia: una lambda no nula sigue encajando en el parámetro.
- [X] T030 Quitar el parámetro `onInfo` de `InfoScreen` e `InfoContent` en `ui/info/InfoScreen.kt` —la pantalla no tiene a dónde mandar ese acceso— y pasar `onInfo = null` al `JewelryScaffold`. Ajustar el `@Preview` y el cableado de `composable<Route.AcercaDe>` en `ui/navigation/AppNavHost.kt`.
- [X] T031 Ampliar `app/src/androidTest/.../ui/info/InfoScreenTest.kt`: el nodo con la descripción `topbar_info` **no existe** en la pantalla de información, y el de `topbar_atras` sí. Repasar `CLAUDE.md` (descripción de `JewelryTopBar`) y volver a pasar las puertas de calidad y la comprobación en emulador.

### Verificación de la enmienda (2026-08-23)

`:app:testDebugUnitTest` 16/16 · `:app:connectedDebugAndroidTest` **13/13** (uno nuevo:
`la_barra_superior_no_ofrece_acceso_a_la_propia_informacion`) · `:app:assembleDebug` y
`:app:lint` correctos, sin avisos nuevos.

En emulador: la barra de Información queda con flecha a la izquierda, logo centrado —el
hueco de 48 dp lo mantiene en su sitio— y nada a la derecha. Home y las secciones de módulo
siguen exponiendo su acceso a la información, comprobado en el árbol de accesibilidad.

