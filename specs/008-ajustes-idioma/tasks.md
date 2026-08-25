---

description: "Task list for feature implementation"
---

# Tasks: Ajustes — idioma de la aplicación

**Input**: Design documents from `/specs/008-ajustes-idioma/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md),
[data-model.md](./data-model.md), [contracts/](./contracts/), [quickstart.md](./quickstart.md)

**Tests**: obligatorios. El principio IV de `.specify/memory/constitution.md` los exige para todo
ViewModel y todo caso de uso, sin excepción, y esta feature añade además un test de paridad de
recursos y dos instrumentados.

**Organization**: por historia de usuario, en orden de prioridad. Cada fase deja la app compilando y
verificable por sí sola.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: se puede hacer en paralelo (ficheros distintos, sin dependencias pendientes)
- **[Story]**: a qué historia de usuario sirve (US1…US4)
- Todas las rutas son reales y relativas a la raíz del repo

## Path Conventions

Módulo único `:app`. Raíz del código:
`app/src/main/java/com/jrblanco/calculadoradejoyeros2021/` (abreviada como `<pkg>/` a partir de aquí).
Tests JVM en `app/src/test/java/com/jrblanco/calculadoradejoyeros2021/` (`<test>/`) e instrumentados en
`app/src/androidTest/java/com/jrblanco/calculadoradejoyeros2021/` (`<androidTest>/`).

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: la dependencia nueva y los recursos que no dependen de nada.

- [x] T001 Añadir DataStore al catálogo: `datastorePreferences = "1.2.1"` en `[versions]` y `androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastorePreferences" }` en `[libraries]` de `gradle/libs.versions.toml`, más `implementation(libs.androidx.datastore.preferences)` en el bloque AndroidX de `app/build.gradle.kts`. Comprobar con `./gradlew :app:assembleDebug`
- [x] T002 [P] Crear las cinco banderas como VectorDrawable de 24×16 dp (proporción 3:2) en `app/src/main/res/drawable/`: `ic_bandera_de.xml`, `ic_bandera_fr.xml` e `ic_bandera_it.xml` calcadas de los SVG de `UI_Plantillas/Feature_Ajustes/` (tres rectángulos cada una); `ic_bandera_es.xml` con las tres franjas `#c60b1e`/`#ffc400` **sin escudo**; `ic_bandera_en.xml` redibujada con paths de relleno —sin `stroke` ni `clip-path`— para que el contracambio de las diagonales salga bien (plan.md, Recursos)
- [x] T003 [P] Crear `app/src/main/res/drawable/ic_idioma.xml`: globo de 24 dp, trazo 1.5–1.8, al estilo de los 21 iconos propios del proyecto; se usa en la cabecera de sección y en la fila «Automático»
- [x] T004 En `app/src/main/res/values/strings.xml`: marcar con `translatable="false"` las 21 cadenas de marca y símbolo que lista `contracts/traducciones.md`, partir `precios_fuente` en `precios_fuente` («Fuente: %1$s») y `precios_fuente_nombre` («Metal Sentinel», no traducible), y añadir el bloque nuevo «Ajustes: idioma» con sus 10 cadenas
- [x] T005 Ajustar `<pkg>/ui/herramientas/precios/PreciosMetalesContent.kt:170` al nuevo formato: `stringResource(R.string.precios_fuente, stringResource(R.string.precios_fuente_nombre))` (depende de T004)

**Checkpoint**: la app compila igual que antes, con la dependencia dentro y los recursos listos.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: el dominio, la persistencia y el grafo. Sin esto ninguna historia puede empezar.

**⚠️ CRITICAL**: ninguna tarea de US1–US4 puede empezar antes del checkpoint de esta fase.

- [x] T006 [P] Crear `<pkg>/domain/model/IdiomaApp.kt`: enum de cinco valores con `etiquetaBcp47`, `analyticsId`, `PREDETERMINADO = ESPANOL` y `desdeEtiqueta(String?)` según las cuatro reglas de `data-model.md`. Kotlin puro, sin `android.*`
- [x] T007 [P] Crear `<test>/domain/model/IdiomaAppTest.kt`: un caso por regla — `es`, `es-ES`, `es_MX`, `ES`, `es-419` → `ESPANOL`; `en`, `fr`, `de`, `it`; `pt`, `xx`, `""`, `"  "`, `null` → `null`; y que `entries` mantiene el orden de la lista de Ajustes
- [x] T008 Crear `<pkg>/domain/model/SeleccionIdioma.kt` con `elegido`, `sistema`, `efectivo` y `esAutomatico` (depende de T006)
- [x] T009 [P] Crear `<test>/domain/model/SeleccionIdiomaTest.kt` con las cuatro filas de la tabla de precedencia de `data-model.md` (FR-008, FR-011)
- [x] T010 Crear `<pkg>/core/util/IdiomaSistema.kt`: interfaz `IdiomaSistema` e implementación `IdiomaSistemaJvm` sobre `Locale.getDefault().language`, con `PREDETERMINADO` como salida cuando no hay coincidencia. JVM puro, mismo patrón que `Reloj.kt` (depende de T006)
- [x] T011 [P] Crear `<test>/core/util/IdiomaSistemaFalso.kt`: `class IdiomaSistemaFalso(var idioma: IdiomaApp = IdiomaApp.ESPANOL)`, al estilo de `RelojFalso`
- [x] T012 Crear `<pkg>/domain/repository/PreferenciasRepository.kt`: `val idioma: Flow<IdiomaApp?>` y `suspend fun guardarIdioma(idioma: IdiomaApp?)`, documentando por qué es `Flow` y no `suspend` al contrario que `CotizacionesRepository` (depende de T006)
- [x] T013 Crear `<pkg>/data/source/local/AjustesLocalDataSource.kt` (interfaz) y `<pkg>/data/source/local/DataStoreAjustesLocalDataSource.kt`: `DataStore<Preferences>` propio `by lazy` con `PreferenceDataStoreFactory.create` sobre `preferencesDataStoreFile("ajustes")` y scope con `dispatchers.io`; clave única `idioma`; `null` elimina la clave; `catch` de `IOException` → `emptyPreferences()`; valor desconocido → `null` **sin borrarlo**. Todo según `contracts/preferencia-idioma.md` (depende de T001, T006)
- [x] T014 Crear `<pkg>/data/repository/PreferenciasRepositoryImpl.kt`, más `<test>/data/source/local/FakeAjustesLocalDataSource.kt` (en memoria con contadores, al estilo de `FakeCotizacionesLocalDataSource`) y `<test>/data/repository/PreferenciasRepositoryImplTest.kt` (depende de T012, T013)
- [x] T015 Crear `<pkg>/domain/usecase/ObservarIdiomaUseCase.kt` y `<pkg>/domain/usecase/GuardarIdiomaUseCase.kt`, más `<test>/data/repository/FakePreferenciasRepository.kt` (con `MutableStateFlow`) y sus tests `<test>/domain/usecase/ObservarIdiomaUseCaseTest.kt` y `GuardarIdiomaUseCaseTest.kt`. El idioma del sistema se lee **en cada emisión** (depende de T008, T010, T012)
- [x] T016 Registrar en Koin sin crear ningún módulo nuevo: `single<IdiomaSistema> { IdiomaSistemaJvm() }` en `<pkg>/core/di/CoreModule.kt`; `single { DataStoreAjustesLocalDataSource(androidContext(), get()) } bind AjustesLocalDataSource::class` y `single<PreferenciasRepository> { PreferenciasRepositoryImpl(get()) }` en `DataModule.kt`; los dos `factoryOf` en `DomainModule.kt`. Dejar `KoinModulesTest` en verde **sin tocar `extraTypes`** (depende de T010, T013, T014, T015)

**Checkpoint**: `./gradlew :app:testDebugUnitTest` en verde. El dominio decide el idioma y la
preferencia se guarda y se observa, aunque todavía nada lo muestre.

---

## Phase 3: User Story 1 - Elegir el idioma y ver la app cambiar al instante (Priority: P1) 🎯 MVP

**Goal**: la pantalla de Ajustes con sus seis filas, y el mecanismo que repinta la app entera al
tocar una bandera. El alemán entra completo en esta fase: es el idioma de los escenarios de la
historia y el más largo, así que es el que prueba de verdad tanto la traducción como la maquetación.

**Independent Test**: con el móvil en español, abrir Ajustes, tocar la bandera alemana y comprobar
que la propia pantalla, su título y la barra inferior pasan a alemán sin navegar ni reiniciar; volver
a Home y ver el menú en alemán.

- [x] T017 [US1] Crear `<pkg>/ui/idioma/ProveedorIdioma.kt`: composable que construye el `Context` localizado con `Configuration(base.resources.configuration).apply { setLocales(LocaleList(Locale.forLanguageTag(idioma.etiquetaBcp47))) }` y `createConfigurationContext`, y provee `LocalContext` y `LocalConfiguration`. `remember(base, idioma)` para no reconstruirlo en cada recomposición. KDoc con el porqué de R2 (no proveer `LocalResources`: se recalcula solo)
- [x] T018 [US1] Crear `<pkg>/ui/idioma/IdiomaAppUiState.kt` (`idioma: IdiomaApp? = null`) y `<pkg>/ui/idioma/IdiomaAppViewModel.kt`, que colecta `ObservarIdiomaUseCase` con `viewModelScope.launch(dispatchers.main)` y publica `seleccion.efectivo`; sin telemetría propia. Registrar con `viewModelOf(::IdiomaAppViewModel)` en `<pkg>/core/di/ViewModelModule.kt`
- [x] T019 [P] [US1] Crear `<test>/ui/idioma/IdiomaAppViewModelTest.kt` con `TestDispatcherProvider` y turbine: estado inicial con `idioma` nulo, primera emisión con el idioma efectivo, y que una emisión con el mismo idioma no produce un estado nuevo
- [x] T020 [US1] Modificar `<pkg>/MainActivity.kt`: resolver `IdiomaAppViewModel` con `koinViewModel()` dentro de `setContent`, colectar con `collectAsStateWithLifecycle()` y componer `ProveedorIdioma(idioma) { AppNavHost() }` solo cuando el idioma no es nulo (FR-013). No tocar `enableEdgeToEdge` ni el tema
- [x] T021 [US1] Crear `<pkg>/ui/ajustes/AjustesUiState.kt` y `<pkg>/ui/ajustes/AjustesViewModel.kt`: colecta `ObservarIdiomaUseCase`, expone `elegido` y `sistema`, y `onIdiomaSeleccionado(IdiomaApp)` que ignora la elección repetida y, si cambia, llama a `GuardarIdiomaUseCase` y emite `ajustes_idioma` con `analyticsId`. En `init`, `logScreenView("ajustes")` —el mismo nombre que emitía el placeholder, para conservar la serie histórica—. Registrar con `viewModelOf(::AjustesViewModel)`
- [x] T022 [P] [US1] Crear `<test>/ui/ajustes/AjustesViewModelTest.kt`: `screen_view` al crearse, elegir un idioma guarda y registra el evento, elegir el mismo idioma no hace nada, y el estado sale del flujo y no de la escritura
- [x] T023 [US1] Crear `<pkg>/ui/ajustes/PresentacionAjustes.kt` con `IdiomaApp.banderaRes` e `IdiomaApp.nombreRes` según la tabla de `data-model.md`, `internal` al paquete
- [x] T024 [US1] Crear `<pkg>/ui/ajustes/AjustesScreen.kt` con `AjustesScreen` (resuelve el ViewModel) y `AjustesContent` (sin estado): `JewelryScaffold` con `title = stringResource(R.string.nav_ajustes)`, sin `onBack` y con `JewelryBottomBar(selected = MainTab.AJUSTES)`; `CabeceraSeccion(ic_idioma, ajustes_seccion_idioma)`; una `TarjetaAcento` dorada con la fila «Automático» y las cinco banderas, cada una como `FilaIdioma` privada con `Role.RadioButton` dentro de un `selectableGroup()`, bandera de 32 dp con esquinas redondeadas, nombre del idioma y `ic_check` dorado en la activa. Tres `@Preview` (sin elección, con italiano elegido, con alemán elegido) sobre `AjustesContent`
- [x] T025 [US1] Modificar `<pkg>/ui/navigation/AppNavHost.kt`: `composable<Route.Ajustes>` pasa de `PlaceholderScreen` a `AjustesScreen(onInfo = onInfo, onTabSelect = ::goToTab)`. `Route.Favoritos` se queda con el placeholder; no se toca `PlaceholderScreen` ni su ViewModel
- [x] T026 [US1] Crear `app/src/main/res/values-de/strings.xml` con las ~195 cadenas traducibles al alemán, respetando las 8 reglas de `contracts/traducciones.md`: mismo orden y mismos comentarios de sección, sin las 26 no traducibles, con los `%n$s` intactos, el `%%`, los `\n` y los símbolos `‰ ³ € …`. Los tres avisos de ley siguen diciendo que se refieren a España; el aviso de humos se traduce literal
- [x] T027 [US1] Endurecer los dos componentes que hoy pintan texto sin límite, ahora que existe «Einstellungen» y «In Favoriten speichern»: `TabItem` en `<pkg>/ui/components/JewelryBottomBar.kt` y `BotonDorado` en `<pkg>/ui/components/Botones.kt` pasan a `BasicText` con `TextAutoSize.StepBased` y una sola línea, igual que `SelectorSegmentado` (R10, FR-021)
- [x] T028 [P] [US1] Crear `<androidTest>/ui/idioma/ProveedorIdiomaTest.kt`: un `Text(stringResource(R.string.nav_ajustes))` envuelto en `ProveedorIdioma(IdiomaApp.ALEMAN)` se lee «Einstellungen» aunque el dispositivo esté en español; y sin envolver, «Ajustes». Es la prueba del mecanismo
- [x] T029 [P] [US1] Crear `<androidTest>/ui/ajustes/AjustesScreenTest.kt`: monta `AjustesContent` sin Koin ni NavHost, comprueba las seis filas, que solo una está seleccionada y que tocar una bandera invoca el callback con el idioma correcto

**Checkpoint**: MVP. El joyero cambia a alemán y la app entera responde. `lint` avisará de
`MissingTranslation` hasta que la fase de US4 complete los otros tres idiomas: es esperado.

---

## Phase 4: User Story 2 - Que la app hable el idioma del móvil sin configurar nada (Priority: P2)

**Goal**: el arranque acierta sin que el joyero toque nada, y Ajustes le dice qué ha detectado.

**Independent Test**: `adb shell pm clear` y arrancar el emulador en cada idioma; la portada aparece
traducida y Ajustes muestra «Automático» marcado con el idioma detectado.

**Nota**: la mayor parte del comportamiento de esta historia sale ya de la Fase 2
(`SeleccionIdioma.efectivo`) y de T020. Lo que queda es lo que el joyero **ve** de esa detección y
la verificación de los bordes.

- [x] T030 [US2] En `<pkg>/ui/ajustes/AjustesScreen.kt`, la fila «Automático» muestra el idioma detectado con `stringResource(R.string.ajustes_idioma_automatico_detalle, stringResource(uiState.sistema.nombreRes))` y queda marcada cuando `elegido == null` (FR-005)
- [x] T031 [P] [US2] Ampliar `<test>/ui/ajustes/AjustesViewModelTest.kt`: con `IdiomaSistemaFalso` en francés y sin elección guardada, el estado sale con `elegido = null` y `sistema = FRANCES`; con el falso en un idioma no soportado, `sistema = ESPANOL` (FR-009)
- [x] T032 [P] [US2] Ampliar `<androidTest>/ui/ajustes/AjustesScreenTest.kt`: primera visita (`elegido = null`) → la fila «Automático» aparece seleccionada, muestra el nombre del idioma detectado y ninguna bandera está marcada
- [x] T033 [US2] Verificar en emulador los pasos 1, 2 y 12 del quickstart: arranque limpio en los cinco idiomas y con el dispositivo en portugués (→ español). Anotar el resultado

**Checkpoint**: US1 y US2 funcionan por separado. Quien no abra Ajustes ya tiene la app en su idioma.

---

## Phase 5: User Story 3 - Que la elección mande y se pueda deshacer (Priority: P3)

**Goal**: la elección sobrevive a todo y «Automático» es un camino de vuelta.

**Independent Test**: elegir un idioma, forzar el cierre y reabrir; cambiar el idioma del sistema y
comprobar que la app no se mueve; elegir «Automático» y comprobar que vuelve a seguirlo, también
tras reiniciar.

- [x] T034 [US3] Añadir `onAutomaticoSeleccionado()` a `<pkg>/ui/ajustes/AjustesViewModel.kt`: si ya está en automático no hace nada; si no, `GuardarIdiomaUseCase(null)` y `ajustes_idioma` con `idioma = "automatico"` (FR-012, FR-024)
- [x] T035 [US3] Cablear la fila «Automático» de `AjustesContent` a `onAutomaticoSeleccionado` (`<pkg>/ui/ajustes/AjustesScreen.kt`)
- [x] T036 [P] [US3] Ampliar `<test>/ui/ajustes/AjustesViewModelTest.kt`: volver a automático guarda `null` y emite el evento con `"automatico"`; repetir automático no guarda ni registra
- [x] T037 [US3] Comprobar que `files/datastore/ajustes.preferences_pb` **no** cae en ninguna exclusión de `app/src/main/res/xml/backup_rules.xml` ni de `app/src/main/res/xml/data_extraction_rules.xml`, y dejar en ambos ficheros un comentario que diga que el idioma elegido sí viaja a un móvil nuevo, para que nadie lo excluya por inercia (FR-025)
- [x] T038 [US3] Verificar en emulador los pasos 8, 9, 10 y 11 del quickstart, y el apartado 3 (copia de seguridad con `bmgr`). Anotar el resultado

**Checkpoint**: las tres historias de comportamiento funcionan. Falta el volumen de traducción.

---

## Phase 6: User Story 4 - Leer toda la app en el idioma elegido, sin islas en español (Priority: P4)

**Goal**: los otros tres idiomas completos y las dos redes automáticas que impiden que se
desincronicen.

**Independent Test**: recorrer las nueve pantallas en cada uno de los cinco idiomas sin encontrar
texto en otro idioma ni etiquetas cortadas; `TraduccionesTest` y `lint` en verde.

- [x] T039 [P] [US4] Crear `app/src/main/res/values-en/strings.xml` completo, con las mismas reglas que T026
- [x] T040 [P] [US4] Crear `app/src/main/res/values-fr/strings.xml` completo
- [x] T041 [P] [US4] Crear `app/src/main/res/values-it/strings.xml` completo
- [x] T042 [US4] Crear `<test>/recursos/TraduccionesTest.kt`: parsea los cinco `strings.xml` desde `src/main/res/` y comprueba las reglas 1–6 de `contracts/traducciones.md` (falta ninguna clave, no sobra ninguna, no se traduce lo no traducible, mismo juego de `%n$s`, `%%` y `\n` conservados), con mensajes de fallo que nombren clave e idioma (depende de T039–T041)
- [x] T043 [US4] Dejar `./gradlew :app:lint` sin `MissingTranslation` ni `ExtraTranslation`, que es la segunda red de FR-015 y FR-018
- [x] T044 [US4] Barrido de desbordes: pasos 4, 5, 13 y 14 del quickstart en los cinco idiomas, con el tamaño de letra por defecto y con el máximo. `ModuleCard` (altura fija de 158 dp) solo se toca si algo se corta de verdad (SC-004)
- [x] T045 [US4] Verificar con TalkBack el paso 15: la fila activa se anuncia como seleccionada y las descripciones de imagen suenan en el idioma elegido (FR-016)
- [x] T046 [US4] Verificar el paso 7: cambiar de idioma con precios ya cargados no los recarga y la fecha «Actualizado …» sale con el mes en el idioma nuevo (FR-019, FR-023)

**Checkpoint**: la feature está completa según la spec.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [x] T047 Puertas automáticas completas: `./gradlew :app:testDebugUnitTest`, `:app:lint`, `:app:assembleDebug` y `:app:compileDebugAndroidTestKotlin`, todas en verde
- [x] T048 Ejecutar `./gradlew :app:assembleRelease` y abrir la app firmada con la clave de debug: comprobar que R8 no se lleva nada de DataStore y que Ajustes funciona en el APK optimizado
- [x] T049 [P] Actualizar `CLAUDE.md` en el mismo cambio (lo exige la constitución): idioma de la app y las cinco carpetas de recursos, `ui/idioma/` como primer paquete de `ui/` sin ruta, `ui/ajustes/`, DataStore como segundo almacén local y por qué, los seis drawables nuevos, `translatable="false"` como contrato, y `:app:lint` como puerta de calidad
- [x] T050 [P] Anotar en `specs/008-ajustes-idioma/` las desviaciones observadas durante la implementación y el resultado de la verificación en emulador, como se hizo en la 007
- [x] T051 Comprobar en DebugView de Firebase el paso 16: `screen_view` con `"ajustes"` y `ajustes_idioma` con `de`, `en` y `automatico`
- [x] T052 Repaso final: ningún `Text("…")` literal nuevo en `main` (`grep -rn 'Text("' app/src/main/java`), ningún `stringResource` huérfano y ningún `TODO` suelto

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Fase 1)**: sin dependencias. T002, T003 y T004 son independientes entre sí; T005 depende de T004
- **Foundational (Fase 2)**: T013 depende de T001 (la dependencia de DataStore); el resto de la fase no depende del Setup. **Bloquea US1–US4**
- **US1 (Fase 3)**: depende del checkpoint de la Fase 2 y de T002/T003 (banderas e icono)
- **US2 (Fase 4)**: depende de US1 (la pantalla y el proveedor ya existen)
- **US3 (Fase 5)**: depende de US1. Independiente de US2
- **US4 (Fase 6)**: depende de US1 (T026 fija el patrón de traducción que copian T039–T041) y de T027
- **Polish (Fase 7)**: depende de todo lo anterior

### Dentro de cada historia

- El modelo antes del repositorio, el repositorio antes del caso de uso, el caso de uso antes del
  ViewModel, el ViewModel antes de la pantalla
- El test de cada pieza se escribe con la pieza, no al final
- Los ficheros de recursos antes de los tests que los leen (T042 después de T039–T041)

### Parallel Opportunities

- Fase 1: T002 y T003 (recursos distintos), y T004 en paralelo con los dos
- Fase 2: T006 con T007; T009 y T011 con lo que haya alrededor; el resto es una cadena de
  dependencias corta y clara
- Fase 3: T019, T022, T028 y T029 (tests, ficheros distintos); T026 y T027 se pueden llevar en
  paralelo con la pantalla
- Fase 6: **T039, T040 y T041 son tres ficheros independientes**: es el mayor paralelismo de la
  feature, y donde más se nota

```bash
# Fase 6, los tres idiomas restantes a la vez (ficheros distintos, cero solape):
values-en/strings.xml · values-fr/strings.xml · values-it/strings.xml
```

---

## Implementation Strategy

### MVP primero (solo US1)

1. Fase 1 (Setup) → 2. Fase 2 (Foundational) → 3. Fase 3 (US1)
4. **PARAR Y VALIDAR**: `testDebugUnitTest` en verde y el paso 3 del quickstart en el emulador
5. En este punto la feature ya se puede demostrar: Ajustes cambia la app entera a alemán al instante

### Entrega incremental

1. Setup + Foundational → el idioma se decide y se guarda, aunque no se vea
2. US1 → **MVP**: pantalla, mecanismo y alemán completo
3. US2 → el arranque acierta solo; validar con `pm clear` en varios idiomas
4. US3 → la elección manda y se puede deshacer; validar con `force-stop` y con `bmgr`
5. US4 → inglés, francés e italiano, más las dos redes automáticas
6. Polish → puertas, `CLAUDE.md`, release y telemetría

---

## Notes

- Las tareas de traducción (T026, T039–T041) son las más largas de la feature: ~195 cadenas cada
  una. El patrón lo fija T026 y las otras tres lo copian, así que conviene revisar T026 con calma
  antes de seguir
- Ninguna tarea toca los cuatro motores de cálculo, ni el conversor de precios, ni la política de
  caché, ni los formateadores de los ViewModels: si una tarea lleva ahí, algo se ha entendido mal
- `KoinModulesTest` no se modifica: si falla, es que un registro está mal, no que el test se haya
  quedado corto
- Commit por tarea o por grupo lógico, con Conventional Commits en español
- Se puede parar en cualquier checkpoint con la app compilando y en verde

---

## Estado

**Las 52 tareas están cerradas.** El resultado de las puertas automáticas, la verificación en el
emulador y las siete desviaciones respecto a este plan están en
[verificacion.md](./verificacion.md). Las tres que más se apartaron: `welcome_developer` había que
partirla (una isla en español en la portada en inglés), `lint` obligó a corregir la lectura de la
configuración en `ProveedorIdioma` y a desactivar los splits de idioma del App Bundle, y la fecha de
precios dejó de usar `DateUtils` porque no seguía al idioma elegido.
