---

description: "Task list for feature implementation"
---

# Tasks: Favoritos — guardar y reabrir cálculos

**Input**: Design documents from `/specs/009-favoritos/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md),
[data-model.md](./data-model.md), [contracts/](./contracts/), [quickstart.md](./quickstart.md)

**Tests**: obligatorios. El principio IV de `.specify/memory/constitution.md` los exige para todo
ViewModel y todo caso de uso, sin excepción. Esta feature añade además un test dorado de la firma
canónica, un test de paridad de formato y los tres primeros tests instrumentados que no son de
Compose.

**Organization**: por historia de usuario, en orden de prioridad. Cada fase deja la app compilando y
verificable por sí sola.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: se puede hacer en paralelo (ficheros distintos, sin dependencias pendientes)
- **[Story]**: a qué historia de usuario sirve (US1…US4)
- Todas las rutas son reales y relativas a la raíz del repo

## Path Conventions

Módulo único `:app`. Raíz del código:
`app/src/main/java/com/jrblanco/calculadoradejoyeros2021/` (abreviada `<pkg>/` a partir de aquí).
Tests JVM en `app/src/test/java/com/jrblanco/calculadoradejoyeros2021/` (`<test>/`) e instrumentados
en `app/src/androidTest/java/com/jrblanco/calculadoradejoyeros2021/` (`<androidTest>/`).

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: abrir la puerta a KSP en la norma del proyecto y meter Room en el build. La
compatibilidad de KSP con el Kotlin de AGP 9 ya está **verificada** (`research.md` R1), así que aquí
sólo se reproduce lo que el spike demostró.

- [x] T001 Enmendar `.specify/memory/constitution.md`: en el principio III, la viñeta pasa a «Koin con DSL manual. Nada de `koin-annotations`.» más una segunda viñeta «KSP se usa **sólo** como procesador de anotaciones de Room. Jamás para inyección de dependencias.»; añadir a «Restricciones técnicas» la línea de persistencia (preferencias en DataStore, caché derivada en SharedPreferences, datos del joyero en Room; nada de `fallbackToDestructiveMigration`; el esquema exportado se commitea); subir la cabecera a `**Version**: 1.1.0 | **Last Amended**: 2026-08-26`. En el **mismo cambio**, como exige la cláusula de Governance, añadir a `CLAUDE.md` en «Dependencias y versiones» que KSP es plugin, que entra sólo por Room y que su versión debe casar con el Kotlin que fija AGP
- [x] T002 Añadir al catálogo `gradle/libs.versions.toml`: en `[versions]`, `room = "2.8.4"` y `ksp = "2.3.11"`; en `[libraries]`, `androidx-room-runtime` y `androidx-room-compiler` con `version.ref = "room"`; en `[plugins]`, `ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }` y `androidx-room = { id = "androidx.room", version.ref = "room" }`. Ni una coordenada suelta (principio V)
- [x] T003 Modificar `build.gradle.kts` (raíz): `alias(libs.plugins.ksp) apply false` y `alias(libs.plugins.androidx.room) apply false` (depende de T002)
- [x] T004 Modificar `app/build.gradle.kts`: los dos `alias` en `plugins`; bloque `room { schemaDirectory("$projectDir/schemas") }` con el comentario de por qué el plugin y no `ksp { arg("room.schemaLocation", …) }` (deja la carpeta como salida no declarada y rompe la relocalización de la caché); `implementation(libs.androidx.room.runtime)` y `ksp(libs.androidx.room.compiler)`; y `androidTestImplementation(libs.kotlinx.coroutines.test)` + `androidTestImplementation(libs.turbine)` para los tests del DAO. **No** declarar `room-ktx`: `withTransaction`, `databaseBuilder` y `Flow` resuelven desde `room-runtime` (R6, verificado) (depende de T003)
- [x] T005 Comprobar que el build sigue en pie con los plugins aplicados y aún sin clases anotadas: `./gradlew :app:assembleDebug` y `./gradlew :app:lint`, las dos en verde (depende de T004)
- [x] T006 [P] Crear `app/src/main/res/drawable/ic_estrella_llena.xml`: el mismo `pathData` de `ic_estrella` pero de relleno (`android:fillColor="#FFFFFFFF"`, sin `strokeColor`), 24 dp, para tintarlo en tiempo de ejecución. Una estrella de trazo no comunica «quitar»
- [x] T007 [P] Comprobar que ni `.gitignore` ni `app/.gitignore` excluyen `app/schemas/`: el esquema exportado tiene que entrar en git o no habrá migración verificable el día que haya versión 2

**Checkpoint**: la app compila y `lint` pasa con Room y KSP en el build, sin una sola clase anotada
todavía.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: el dominio, la persistencia y el grafo de Koin. Guardar, leer, borrar y resumir un
favorito, todo sin una línea de interfaz.

**⚠️ CRITICAL**: ninguna tarea de US1–US4 puede empezar antes del checkpoint de esta fase.

- [x] T008 [P] (paralelo con T010 y T011; T009 depende de esta) Mudar `ModoEntradaSoldadura` de `<pkg>/ui/soldaduras/SoldadurasUiState.kt` a un fichero nuevo `<pkg>/domain/model/ModoEntradaSoldadura.kt`, conservando sus dos valores y su `analyticsId`, y reescribiendo su KDoc: ya no es «concepto de UI», porque `ResumirFavoritoUseCase` lo recibe. Actualizar el `import` en `SoldadurasUiState.kt`, `SoldadurasViewModel.kt`, `SoldadurasScreen.kt`, `SoldaduraBaseUiState.kt`, `SoldaduraBaseViewModel.kt`, `SoldaduraBaseScreen.kt`, `<test>/ui/soldaduras/SoldadurasViewModelTest.kt`, `<test>/ui/soldaduras/SoldaduraBaseViewModelTest.kt`, `<androidTest>/ui/soldaduras/SoldadurasScreenTest.kt` y `<androidTest>/ui/soldaduras/SoldaduraBaseScreenTest.kt`. Dejar `./gradlew :app:testDebugUnitTest` en verde. **No** mudar `FamiliaSoldadura`, `IngredienteSoldadura` ni `MedidaChapa`
- [x] T009 [P] Crear `<pkg>/domain/model/Favorito.kt`: `data class Favorito(id, guardadoEnEpochMillis, entradas)` y `sealed interface EntradasFavorito` con `analyticsId` y las **siete** variantes de `data-model.md` §2, cada una con los campos en el orden de los parámetros de su motor y con `init { require(... > BigDecimal.ZERO) }`. KDoc con la trampa de `BigDecimal.equals` (la escala cuenta: `Oro("30") != Oro("30.0")`) y la prohibición de `distinctBy`/`contains` sobre entradas. Kotlin puro, sin `android.*` (depende de T008)
- [x] T010 [P] Crear `<pkg>/domain/model/ResultadoGuardado.kt`: `sealed interface` con `id: Long`, `Guardado(id)` y `YaExistia(id)`. KDoc: el duplicado es un valor y no una excepción (FR-006)
- [x] T011 [P] Crear `<pkg>/domain/model/ResumenFavorito.kt`: `sealed interface` con las cinco variantes de `data-model.md` §4, con `BigDecimal` **sin formatear**, reutilizando `MetalLiga` y `ComponenteCalculado`. Cero tipos y cero enums nuevos. KDoc: por qué una variante por motor y no una lista plana (evitar el cuarto enum paralelo)
- [x] T012 Crear `<pkg>/domain/repository/FavoritosRepository.kt` con `val favoritos: Flow<List<Favorito>>` (más reciente primero), `suspend fun guardar(entradas): ResultadoGuardado`, `suspend fun borrar(id)` y `suspend fun obtener(id): Favorito?`. KDoc con el porqué de `Flow` en la lista y `suspend` en el resto, al estilo de `CotizacionesRepository` (depende de T009, T010)
- [x] T013 Crear `<pkg>/domain/usecase/ResumirFavoritoUseCase.kt`: los once motores por constructor y un `when` exhaustivo que aplica la tabla de `data-model.md` §4, incluidas las reglas de fila del modo directo (`componentes.drop(1)` en clásica y plata, `filter { it.metal != ORO_24K }` en la BASE, `oro18K = null` en ORO LEY). Recibe `EntradasFavorito`, no `Favorito`. KDoc: primer caso de uso del proyecto que depende de otros, y aviso de que los resultados cambiarán si cambia una receta o una densidad (depende de T009, T011)
- [x] T014 [P] Crear `<test>/domain/usecase/ResumirFavoritoUseCaseTest.kt`: una prueba por cada una de las once filas de la tabla, con los mismos vectores que los tests de los motores y `compareTo == 0`; que el modo directo omita la fila del metal de partida y el inverso la incluya; y que ninguna variante lance (depende de T013)
- [x] T015 [P] Crear `<test>/domain/model/FavoritosDePrueba.kt`: fábrica de las siete variantes con valores por defecto sobrescribibles, al estilo de `<test>/domain/model/CotizacionesDePrueba.kt` (depende de T009)
- [x] T016 [P] Crear `<pkg>/data/source/local/FavoritoPersistidoDto.kt`: DTO plano `@Serializable` con `version: Int = 1` y los campos de las siete variantes nulables, decimales como `String` y enums por nombre. El discriminador **no** va dentro: vive en la columna `tipo` (depende de T009)
- [x] T017 Crear `<pkg>/data/source/local/CodificadorFavorito.kt`: `data class FavoritoPersistido(tipo, firma, datosJson)`, `codificar(entradas)` y `decodificar(tipo, datosJson): EntradasFavorito?`. La **firma** la escribe un `when` explícito con el formato y las cinco reglas de `contracts/favoritos-persistidos.md`; nunca desde el DTO ni por reflexión. Decodificación tolerante: `try/catch (SerializationException, IllegalArgumentException) → null`, enums con `entries.firstOrNull { it.name == … }`, decimales con `runCatching`. Kotlin puro (depende de T016)
- [x] T018 [P] Crear `<test>/data/source/local/CodificadorFavoritoTest.kt`: ida y vuelta de las siete variantes; **las siete firmas literales** del contrato como test dorado; `firma(Oro("30")) == firma(Oro("30.0"))` junto al aserto de que `Oro("30") != Oro("30.0")` como `equals`; firma distinta al cambiar cualquier entrada, incluido el `modo`; chapa 10/2/30 para que un cruce ancho↔largo falle; y los seis casos de la tabla de tolerancia (depende de T017)
- [x] T019 [P] Crear `<pkg>/data/source/local/FavoritoEntity.kt`: `@Entity(tableName = "favoritos", indices = [Index(value = ["firma"], unique = true)])` con `id` (`@PrimaryKey(autoGenerate = true)`), `tipo`, `firma`, `datosJson` y `guardadoEnEpochMillis`. Sin `@ColumnInfo`. KDoc: por qué el índice va sobre `firma` y **no** sobre `datosJson`
- [x] T020 [P] Crear `<pkg>/data/source/local/FavoritosDao.kt`: `@Dao` con todos los métodos **abstractos** — `observar(): Flow<List<FavoritoEntity>>` con `ORDER BY id DESC` (y el KDoc de por qué por id y no por fecha), `porId`, `idPorFirma`, `insertar` con `@Insert(onConflict = IGNORE)` devolviendo `Long`, y `borrar` devolviendo `Int` (depende de T019)
- [x] T021 Crear `<pkg>/data/source/local/FavoritosDatabase.kt`: `@Database(entities = [FavoritoEntity::class], version = 1)` abstracta con `favoritosDao()`, dejando `exportSchema` en su valor por defecto. Ejecutar `./gradlew :app:kspDebugKotlin` y comprobar que genera los `_Impl` y que aparece `app/schemas/com.jrblanco.calculadoradejoyeros2021.data.source.local.FavoritosDatabase/1.json`; **commitear ese fichero** (depende de T020)
- [x] T022 [P] Crear `<pkg>/data/source/local/FavoritosLocalDataSource.kt`: interfaz que habla tipos de dominio, con `favoritos`, `guardar(entradas, guardadoEnEpochMillis)`, `borrar(id)` y `obtener(id)` (depende de T009, T010)
- [x] T023 Crear `<pkg>/data/source/local/RoomFavoritosLocalDataSource.kt`: la base nace `by lazy` aquí dentro con `Room.databaseBuilder(...)` y `.setJournalMode(TRUNCATE)`, **sin** `fallbackToDestructiveMigration`; `guardar` con `baseDatos.withTransaction { }` — `insertar`, y sólo si devuelve `-1L`, `idPorFirma` para responder `YaExistia`; `favoritos` mapea con `mapNotNull`, así que una fila ilegible se descarta sin borrarse. KDoc con los tres porqués del plan: la base fuera de Koin, TRUNCATE en vez de WAL, y por qué **no** recibe `DispatcherProvider` (depende de T017, T021, T022)
- [x] T024 [P] Crear `<test>/data/source/local/FakeFavoritosLocalDataSource.kt`: en memoria, con contadores `private set` y `puerta: CompletableDeferred<Unit>?`, al estilo de `FakeCotizacionesLocalDataSource` (depende de T022)
- [x] T025 Crear `<pkg>/data/repository/FavoritosRepositoryImpl.kt`: pasarela sobre el data source que sella `guardadoEnEpochMillis` con el `Reloj` inyectado, para que ni la pantalla ni el caso de uso puedan mentir con la hora (depende de T012, T022)
- [x] T026 [P] Crear `<test>/data/repository/FavoritosRepositoryImplTest.kt`: sello del `RelojFalso`; `guardar` → `Guardado(id)`; el mismo `guardar` otra vez → `YaExistia` con el mismo id y una sola fila; `borrar`; `obtener`; orden más reciente primero; y que una fila ilegible no aparece en la lista (depende de T024, T025)
- [x] T027 [P] Crear los cuatro casos de uso finos en `<pkg>/domain/usecase/`: `ObservarFavoritosUseCase` (devuelve el `Flow`), `GuardarFavoritoUseCase`, `BorrarFavoritoUseCase` y `ObtenerFavoritoUseCase`, al estilo de `ObservarIdiomaUseCase`/`GuardarIdiomaUseCase` (depende de T012)
- [x] T028 [P] Crear `<test>/data/repository/FakeFavoritosRepository.kt` (patrón `FakePreferenciasRepository`: flujo empujable a mano y lista de guardados) y los cuatro tests de delegación `<test>/domain/usecase/{Observar,Guardar,Borrar,Obtener}FavoritoUseCaseTest.kt` (depende de T027)
- [x] T029 Registrar en Koin **sin crear ningún módulo nuevo**: en `<pkg>/core/di/DataModule.kt`, `single { RoomFavoritosLocalDataSource(androidContext()) } bind FavoritosLocalDataSource::class` (concreto con `bind`, para que `verify()` inspeccione su constructor) y `single<FavoritosRepository> { FavoritosRepositoryImpl(get(), get()) }`; en `DomainModule.kt`, los cinco `factoryOf`. **No** registrar `FavoritosDatabase` ni `FavoritosDao`. Dejar `KoinModulesTest` en verde **sin tocar `extraTypes`** (depende de T013, T023, T025, T027)
- [x] T030 [P] Añadir el comentario de los favoritos a `app/src/main/res/xml/backup_rules.xml` y a `app/src/main/res/xml/data_extraction_rules.xml`, al estilo del que puso la 008 para el idioma: `databases/favoritos.db` **no** se excluye a propósito (FR-033), y el journal va en TRUNCATE, así que no hay `-wal`/`-shm` que restaurar a medias
- [x] T031 [P] Crear `<androidTest>/data/source/local/FavoritosDaoTest.kt` con `Room.inMemoryDatabaseBuilder(...)` y `@After close()`: que `insertar` devuelve id; que **la segunda inserción con la misma firma devuelve `-1` y deja una sola fila**; que `idPorFirma` la encuentra; que `observar()` emite en `id DESC` (con Turbine); que `borrar` de un id inexistente devuelve 0; y que dos firmas distintas conviven. Es el único sitio donde el índice único se prueba de verdad (depende de T021)
- [x] T032 [P] Crear `<androidTest>/data/source/local/RoomFavoritosLocalDataSourceTest.kt` con `ApplicationProvider` y el fichero real, tras `context.deleteDatabase("favoritos.db")`: **un solo `@Test`** que recorra guardar → observar → obtener → guardar duplicado → borrar. Un único `@Test` a propósito, para no tener dos instancias de `RoomDatabase` sobre el mismo fichero en el proceso (depende de T023)
- [x] T033 Puertas de la fase: `./gradlew :app:testDebugUnitTest`, `:app:assembleDebug`, `:app:compileDebugAndroidTestKotlin` y `:app:connectedDebugAndroidTest`, las cuatro en verde (depende de T029, T031, T032)

**Checkpoint**: se puede guardar un favorito, listarlo, obtenerlo, borrarlo y resumir sus cifras, y
está todo probado. La app aún no lo ofrece por ninguna pantalla.

---

## Phase 3: User Story 1 - Guardar el cálculo que tengo delante (Priority: P1) 🎯 MVP

**Goal**: el botón de las cinco calculadoras guarda de verdad, avisa de lo que ha pasado, no
duplica, y la pestaña Favoritos deja de ser un andamio y muestra lo guardado.

**Independent Test**: guardar un cálculo en cada una de las cinco calculadoras, cerrar la app por
completo, volver a abrirla y comprobar que la pestaña Favoritos los muestra todos, del más reciente
al más antiguo; volver a guardar uno y que avise de que ya estaba; y pulsar con el campo vacío y que
pida completar el cálculo.

- [x] T034 [P] [US1] Añadir a los cinco `strings.xml` (`values/`, `values-en`, `values-fr`, `values-de`, `values-it`), en la sección «Compartido: unidades, metales y acciones» y en el mismo orden en los cinco: `favoritos_aviso_guardado` («Guardado en favoritos»), `favoritos_aviso_repetido` («Ya estaba en favoritos») y `favoritos_aviso_sin_datos` («Completa el cálculo antes de guardar»). Dejar `TraduccionesTest` y `./gradlew :app:lint` en verde
- [x] T035 [P] [US1] Crear `<pkg>/ui/favoritos/AvisoFavorito.kt` con el enum `GUARDADO`, `REPETIDO`, `SIN_DATOS`, y en `<pkg>/ui/favoritos/PresentacionFavoritos.kt` la extensión `internal val AvisoFavorito.mensajeRes: Int` que lo mapea a las tres cadenas de T034 (depende de T034)
- [x] T036 [US1] Modificar los cinco `UiState` —`<pkg>/ui/oro/OroUiState.kt`, `<pkg>/ui/plata/PlataUiState.kt`, `<pkg>/ui/soldaduras/SoldadurasUiState.kt`, `<pkg>/ui/soldaduras/SoldaduraBaseUiState.kt`, `<pkg>/ui/herramientas/chapas/PesoChapasUiState.kt`— añadiendo `val avisoFavorito: AvisoFavorito? = null`. Con valor por defecto, para que los tests instrumentados existentes no se toquen (depende de T035)
- [x] T037 [US1] Modificar `<pkg>/ui/oro/OroViewModel.kt`: recibir `guardarFavorito: GuardarFavoritoUseCase`, `obtenerFavorito: ObtenerFavoritoUseCase` y `dispatchers: DispatcherProvider`; `onGuardarFavoritos()` construye `EntradasFavorito.Oro` desde el estado (o avisa `SIN_DATOS` si la cantidad no es válida), guarda en `viewModelScope.launch(dispatchers.main)` y pone `GUARDADO` o `REPETIDO` según el `ResultadoGuardado`; añadir `onAvisoFavoritoMostrado()`; retirar el evento `oro_favoritos_proximamente` y emitir `oro_favorito_guardado` con `resultado = nuevo | repetido`; y poner `avisoFavorito = null` en `recalcular` y en `onLimpiar` (depende de T036)
- [x] T038 [US1] Repetir T037 en `<pkg>/ui/plata/PlataViewModel.kt`, `<pkg>/ui/soldaduras/SoldadurasViewModel.kt` (en soldaduras, la variante depende de la familia: `SoldaduraLey`, `SoldaduraClasica` o `SoldaduraPlata`, y `familia == null` es `SIN_DATOS`), `<pkg>/ui/soldaduras/SoldaduraBaseViewModel.kt` y `<pkg>/ui/herramientas/chapas/PesoChapasViewModel.kt` (con las tres medidas, y `SIN_DATOS` si alguna falta o está fuera de rango), cada uno con su nombre de evento (depende de T037)
- [x] T039 [US1] Modificar las cinco pantallas —`<pkg>/ui/oro/OroScreen.kt`, `<pkg>/ui/plata/PlataScreen.kt`, `<pkg>/ui/soldaduras/SoldadurasScreen.kt`, `<pkg>/ui/soldaduras/SoldaduraBaseScreen.kt`, `<pkg>/ui/herramientas/chapas/PesoChapasSection.kt`— sustituyendo el `Toast` de `aviso_proximamente` por un `LaunchedEffect(uiState.avisoFavorito)` que lanza el `Toast` del `mensajeRes` y llama a `viewModel.onAvisoFavoritoMostrado()`. El `Toast` y el efecto viven en `XScreen`, **no** en `XContent`, para que las firmas sin estado no cambien y sus tests instrumentados sigan pasando (depende de T038)
- [x] T040 [P] [US1] Crear `<pkg>/ui/favoritos/FormatoFavoritos.kt`: `internal object` hermano de `FormatoPrecios`, con `gramosMedia` (3, `HALF_UP`), `gramosPlata` (3, `DOWN`), `pesoChapa` (2, `HALF_UP`) y `cantidadEntrada` (`stripTrailingZeros`), todas con coma decimal. KDoc: la duplicación con los cinco ViewModels es **deliberada** por mandato de `CLAUDE.md`, el truncado de plata lo exige la Ley 17/1985, y el guardián es el test de paridad
- [x] T041 [P] [US1] Crear `<test>/ui/favoritos/FormatoFavoritosTest.kt` con los valores frontera: 5,1575 → «5,158» a la media y «5,157» truncando; 1,558 → «1,56» en chapa; «30.000» → «30» como cantidad de entrada (depende de T040)
- [x] T042 [P] [US1] Añadir a los cinco `strings.xml` una sección nueva `<!-- Favoritos -->` al final del fichero, en el mismo orden en los cinco: `favoritos_vacio_titulo`, `favoritos_vacio_texto` (con `%1$s` para citar `accion_guardar_favoritos`, como hace `precios_fuente`), `favoritos_guardado_el` («Guardado el %1$s»), `favoritos_cantidad_gramos` («%1$s gr», `translatable="false"`, plantilla hermana de `chapas_dibujo_medida`; no duplica `unidad_gramos`, que es la unidad sola) y `favoritos_medidas_chapa` («%1$s × %2$s × %3$s mm», `translatable="false"`). Dejar `TraduccionesTest` y `lint` en verde
- [x] T043 [US1] Crear `<pkg>/ui/favoritos/FavoritosUiState.kt` con `FavoritosUiState(cargando = true, favoritos, pendienteDeBorrar = null)`, `FavoritoUiModel`, `EntradasFavoritoUi` (sellado, con el `tipo` **derivado**), `LineaFavoritoUi` y los dos enums de presentación `TipoFavorito` (**cinco** valores: `ORO`, `PLATA`, `SOLDADURA`, `SOLDADURA_BASE`, `CHAPA` — las tres familias de soldadura comparten pantalla y sección, así que las siete variantes de dominio colapsan a cinco secciones) y `ConceptoFavorito`. Cifras ya formateadas, lo traducible como enum y la fecha como `Long` (depende de T009, T011)
- [x] T044 [US1] Crear `<pkg>/ui/favoritos/FavoritosViewModel.kt`: `observarFavoritos`, `resumirFavorito`, `borrarFavorito`, `analytics` y `dispatchers` por constructor; `analytics.logScreenView("favoritos")` en `init` —el nombre que ya emitía el placeholder, para conservar la serie histórica—; recolecta el flujo y mapea a `FavoritoUiModel` aplicando `FormatoFavoritos` según el tipo; `onFavoritoPulsado` sólo registra `favoritos_abierto` con `tipo` y navega la vista (depende de T027, T040, T043)
- [x] T045 [US1] Registrar `viewModelOf(::FavoritosViewModel)` en `<pkg>/core/di/ViewModelModule.kt` y dejar `KoinModulesTest` en verde (depende de T043)
- [x] T046 [US1] Promover a `internal` los mapeos enum→recurso que el título de la tarjeta necesita, porque hoy son `private` de la pantalla que los estrenó y `ui/favoritos/` no puede verlos: crear `<pkg>/ui/oro/PresentacionOro.kt` con `LeyOro.etiquetaRes`, `ColorOro.etiquetaRes` y `ColorOro.acento` (salen de `OroScreen.kt`), crear `<pkg>/ui/plata/PresentacionPlata.kt` con `LeyPlata.etiquetaRes` (sale de `PlataScreen.kt`), y mover a `<pkg>/ui/soldaduras/PresentacionSoldadura.kt` —que ya existe— `FamiliaSoldadura.etiquetaRes`, `.etiquetaModoDirectoRes`, `.acento`, `TipoSoldaduraClasica.etiquetaRes`, `TipoSoldaduraPlata.etiquetaRes` y `DurezaSoldaduraLey.etiquetaRes` (salen de `SoldadurasScreen.kt`). Añadir además `ModoEntradaSoldadura.etiquetaRes` sobre las cuatro claves `soldadura_modo_*`, que hoy se usan en línea y no tienen mapeo. Es la regla del segundo consumidor del proyecto: nacieron privados y suben cuando alguien más los pide. Las tres pantallas siguen compilando sin cambiar de comportamiento (depende de T008)
- [x] T047 [US1] Completar `<pkg>/ui/favoritos/PresentacionFavoritos.kt` con `TipoFavorito.imagenRes` (`modulo_oro`, `modulo_plata`, `modulo_soldaduras`, `granalla` para la BASE, `modulo_herramientas`), `.seccionRes` y `.acento`, `ConceptoFavorito.nombreRes` (reutilizando `metal_plata_fina`, `metal_cobre`, `metal_paladio`, `metal_oro_24k`, `soldadura_fila_base`, `metal_laton`, `metal_zinc`, `metal_cadmio`, todas existentes), `tituloDe(entradas)` que ensambla etiquetas ya traducidas unidas por `" · "` (un `private const val SEPARADOR`, que es puntuación y no idioma) —en chapa hay que **anidar dos cadenas**: `chapas_material_oro` es la plantilla «Oro %1$s» y la ley sale de `oro_ley_18k`/`plata_ley_925`— y `etiquetaTotalDe(entradas)` reutilizando `oro_total`, `plata_total`, `soldadura_total`, `soldadura_base_total` y `chapas_resultado_titulo`. **Cero cadenas nuevas** para secciones, metales y totales (depende de T044)
- [x] T048 [US1] Crear `<pkg>/ui/favoritos/TarjetaFavorito.kt`: `TarjetaFavorito(favorito, onAbrir, modifier)` sobre `TarjetaAcento`, con el nombre de sección en `labelMedium` teñido, el título en `titleMedium` a 16 sp con `maxLines = 2` y `Ellipsis`, `GoldSeparator`, las líneas de resultado con `LineaPunteada` y la fila de total con `ic_balanza`; más las privadas `FilaResumenFavorito` y `FilaTotalFavorito` (filas compactas: **no** se reutilizan `FilaMetal` ni `TarjetaTotal`, cuya imagen de 44 dp y cifra de 26 sp harían un muro en una lista). La imagen de sección, el acento por sección, el recorte y la fecha llegan en US4 (depende de T047)
- [x] T049 [US1] Modificar `<pkg>/ui/components/Tarjetas.kt`: `TarjetaAcento` gana `onClick: (() -> Unit)? = null` y aplica `.clip(shape).clickable(role = Role.Button, onClick = it)` entre el borde y el `padding` **sólo cuando no es nulo**. El `clip` condicional es obligatorio: aplicarlo siempre cambiaría el pintado de los consumidores actuales, uno de los cuales lleva dentro el `Canvas` de chapas con cotas que sobresalen. Comprobar a ojo la pantalla de chapas tras el cambio (depende de T048)
- [x] T050 [US1] Crear `<pkg>/ui/favoritos/FavoritosScreen.kt` con `FavoritosScreen` (resuelve el ViewModel con `koinViewModel()`) y `FavoritosContent` (sin estado): `JewelryScaffold` con `title = stringResource(R.string.nav_favoritos)`, sin `onBack` y con `JewelryBottomBar(selected = MainTab.FAVORITOS)`; un `LazyColumn` con `contentPadding` y `spacedBy` de `JewelrySpacing.Md` e `items(key = { it.id })`; la privada `TarjetaSinFavoritos` para la lista vacía (icono `ic_favoritos` en un círculo dorado de 96 dp, al estilo de la primera visita de Herramientas); y **nada pintado mientras `cargando`**, para que la invitación no parpadee en cada visita (FR-018). Tres `@Preview`: cargando, vacío y con tres favoritos (depende de T042, T048)
- [x] T051 [US1] Modificar `<pkg>/ui/navigation/AppNavHost.kt`: `composable<Route.Favoritos>` pasa de `PlaceholderScreen` a `FavoritosScreen(onAbrirFavorito = …, onTabSelect = ::goToTab, onInfo = onInfo)`. La navegación real por tipo llega en US2; de momento el callback puede quedar sin destino (depende de T050)
- [x] T052 [P] [US1] Crear `<test>/ui/favoritos/FavoritosViewModelTest.kt`: que emite `screen_view "favoritos"` una sola vez; que `cargando` es `true` antes de la primera emisión y `false` después; el mapeo y el orden; el formateo por tipo; y `favoritos_abierto` con su `tipo`; y un **aserto negativo** de FR-036: los parámetros del evento son exactamente `{tipo}` y no llevan cantidades ni medidas. Con `FakeFavoritosRepository`, casos de uso reales y `TestDispatcherProvider` (depende de T044)
- [x] T053 [P] [US1] Crear `<test>/ui/favoritos/FavoritosParidadFormatoTest.kt`: por cada tipo, ejecutar el ViewModel real de la calculadora y el `FavoritosViewModel` real con las mismas entradas y comparar **cadena a cadena**. Casos obligatorios: oro 18 K blanco 30 g; **plata 950 con 100 g, que debe dar 5,157 y no 5,158**; soldadura clásica floja inversa; BASE inversa 10 g; y chapa 10 × 0,5 × 20, que debe dar «1,56». Es el test que legitima la duplicación de T041 (depende de T038, T044)
- [x] T054 [P] [US1] Ampliar los cinco tests de ViewModel de calculadora —`<test>/ui/oro/OroViewModelTest.kt`, `<test>/ui/plata/PlataViewModelTest.kt`, `<test>/ui/soldaduras/SoldadurasViewModelTest.kt`, `<test>/ui/soldaduras/SoldaduraBaseViewModelTest.kt`, `<test>/ui/herramientas/chapas/PesoChapasViewModelTest.kt`—: actualizar los `crearViewModel()` a la firma nueva (edición mecánica que toca todos los tests de esos ficheros) y **reescribir** el test de «guardar en favoritos sólo registra telemetría y no toca el estado», que deja de ser verdad. Casos nuevos: guardado nuevo pone `GUARDADO`, repetido pone `REPETIDO`, entrada inválida pone `SIN_DATOS` y no llama al caso de uso, `onAvisoFavoritoMostrado` lo limpia, el evento nuevo se emite con su `resultado`, y **dos `onGuardarFavoritos()` seguidos** (FR-009) dejan un solo favorito y el segundo avisa `REPETIDO` (depende de T038)
- [x] T055 [P] [US1] Crear `<androidTest>/ui/favoritos/FavoritosScreenTest.kt` montando `FavoritosContent` sin Koin ni NavHost: estado `cargando` no pinta nada; lista vacía muestra título y texto de la invitación; con dos favoritos se ven títulos, líneas y totales; y pulsar una tarjeta propaga `onAbrir` con su id (depende de T050)
- [x] T056 [US1] Puertas de la historia: `./gradlew :app:testDebugUnitTest`, `:app:lint`, `:app:assembleDebug` y `:app:connectedDebugAndroidTest` en verde, y recorrer a mano los pasos 1–9 de `quickstart.md` en el emulador (depende de T051, T052, T053, T054, T055)

**Checkpoint**: US1 completa y verificable sola. FR-013 queda a medias a propósito: la imagen de sección, el acento y la fecha son US4. El joyero guarda desde las cinco calculadoras, la
lista sobrevive al cierre de la app, el duplicado avisa y el campo vacío también.

---

## Phase 4: User Story 2 - Volver a un cálculo guardado (Priority: P2)

**Goal**: pulsar una tarjeta abre su calculadora con los datos puestos y editables, sin tocar el
favorito.

**Independent Test**: guardar un favorito de cada tipo, abrirlo desde la lista, comprobar que llegan
todas las entradas y el resultado, editar la cantidad, volver atrás y ver que la tarjeta no ha
cambiado.

- [x] T057 [US2] Modificar `<pkg>/ui/navigation/Routes.kt`: `Oro`, `Plata`, `Soldaduras`, `SoldaduraBase` y `Herramientas` pasan de `@Serializable data object` a `@Serializable data class X(val favoritoId: Long? = null)`. Sin valor centinela: el proyecto los rechaza, y `Long?` está soportado nativamente por el serializador de rutas (R10). `Welcome`, `Home`, `Favoritos`, `Ajustes` y `AcercaDe` no cambian
- [x] T058 [US2] Modificar `<pkg>/ui/navigation/AppNavHost.kt`: los mapeos privados `MainTab.route` y `HomeModule.route` pasan a `Route.Oro()`, `Route.Plata()`, `Route.Soldaduras()` y `Route.Herramientas()`; `onSoldaduraBase = { goTo(Route.SoldaduraBase()) }`; cada `composable<Route.X>` lee `entrada.toRoute<Route.X>().favoritoId` y lo pasa a su pantalla; y se añade la extensión privada `TipoFavorito.ruta(id): Route`, hermana de las dos que ya existen, para cablear `onAbrirFavorito` (depende de T057)
- [x] T059 [US2] Añadir `cargarFavorito(id: Long)` a `<pkg>/ui/oro/OroViewModel.kt`: guardián `private var favoritoAplicado = false` puesto a `true` **antes** del `launch`; obtiene el favorito, hace `as? EntradasFavorito.Oro ?: return@launch` (un id inexistente o de otro tipo se ignora en silencio, sin `recordError`) y llama a un `aplicar()` privado que construye el estado completo **en una sola asignación** y recalcula. La cantidad vuelve al campo con `FormatoFavoritos.cantidadEntrada` (depende de T027, T040)
- [x] T060 [US2] Repetir T059 en `<pkg>/ui/plata/PlataViewModel.kt`, `<pkg>/ui/soldaduras/SoldaduraBaseViewModel.kt` y `<pkg>/ui/herramientas/chapas/PesoChapasViewModel.kt` (depende de T059)
- [x] T061 [US2] Añadir `cargarFavorito` a `<pkg>/ui/soldaduras/SoldadurasViewModel.kt`, que es el caso peliagudo: `aplicar()` **no** puede usar los setters públicos, porque `onFamiliaSeleccionada` reasigna `SoldadurasUiState(familia = familia)` por FR-023 de la 006 y borraría la cantidad, además de emitir eventos `soldaduras_calculado` intermedios. Construir familia, modo, tipo o dureza, color y cantidad en una sola asignación y recalcular (depende de T059)
- [x] T062 [US2] Modificar las cinco pantallas para aceptar `favoritoId: Long? = null` y hacer `LaunchedEffect(favoritoId) { favoritoId?.let(viewModel::cargarFavorito) }` en `XScreen`. En `<pkg>/ui/herramientas/HerramientasScreen.kt`, además, pasar el id por el *slot* `chapas` a `PesoChapasSection(favoritoId = …)`, de modo que **`HerramientasContent` no cambie de firma** y su test instrumentado no se toque (depende de T060, T061)
- [x] T063 [US2] Añadir `abrirFavoritoDeChapa()` a `<pkg>/ui/herramientas/HerramientasViewModel.kt`: fija `subherramienta = CHAPAS` de forma idempotente y **sin emitir `herramientas_subherramienta`**, porque ese evento mide una elección del joyero y contaminarlo con aperturas de favorito corrompe la métrica. Llamarlo desde el `LaunchedEffect` de `HerramientasScreen` cuando llega un id (depende de T062)
- [x] T064 [US2] Cablear `onAbrirFavorito` en `FavoritosScreen`/`AppNavHost` para que navegue a `tipo.ruta(id)` con `launchSingleTop`, y que «atrás» devuelva al listado (depende de T058, T062)
- [x] T065 [P] [US2] Ampliar los cinco tests de ViewModel de calculadora —`<test>/ui/oro/OroViewModelTest.kt`, `<test>/ui/plata/PlataViewModelTest.kt`, `<test>/ui/soldaduras/SoldadurasViewModelTest.kt`, `<test>/ui/soldaduras/SoldaduraBaseViewModelTest.kt` y `<test>/ui/herramientas/chapas/PesoChapasViewModelTest.kt`— con los casos de `cargarFavorito`: aplica todas las entradas y recalcula; **es idempotente** (dos llamadas seguidas aplican una sola vez, que es lo que protege lo editado de un cambio de configuración); ignora en silencio un id inexistente y un favorito de otro tipo; y en soldaduras, que la cantidad **no** se pierde al fijar la familia (depende de T060, T061)
- [x] T066 [P] [US2] Ampliar `<test>/ui/herramientas/HerramientasViewModelTest.kt`: `abrirFavoritoDeChapa()` fija CHAPAS y **no** emite `herramientas_subherramienta` (depende de T063)
- [x] T067 [US2] Puertas de la historia: las cuatro en verde, y recorrer a mano los pasos 10–15 de `quickstart.md`, incluido el 14 (abrir un favorito de chapa **no** debe consultar la API de precios) y el 15 (editar y cambiar el tamaño de letra del sistema sin perder lo editado) (depende de T064, T065, T066)

**Checkpoint**: US1 y US2 funcionan. El joyero guarda y recupera.

---

## Phase 5: User Story 3 - Quitar de la lista lo que ya no sirve (Priority: P3)

**Goal**: la estrella de cada tarjeta quita el favorito, con confirmación previa.

**Independent Test**: con varios favoritos, quitar uno y ver que desaparece y los demás siguen;
pulsar la estrella y cancelar, y comprobar que no se pierde nada; quitar todos y ver la invitación.

- [x] T068 [P] [US3] Añadir a los cinco `strings.xml`, en la sección «Favoritos» y en el mismo orden: `favoritos_quitar` («Quitar de favoritos», que es el `contentDescription` de la estrella), `favoritos_borrar_titulo` («¿Quitar de favoritos?»), `favoritos_borrar_mensaje` («Se quitará «%1$s». Podrás volver a guardarlo cuando quieras.») y `favoritos_borrar_confirmar` («Quitar»); y `accion_cancelar` («Cancelar») en la sección «Compartido», junto a `accion_limpiar`. Dejar `TraduccionesTest` y `lint` en verde
- [x] T069 [US3] Añadir `EstrellaFavorito` privada a `<pkg>/ui/favoritos/TarjetaFavorito.kt`: `Box` de 48 dp con `clickable(role = Role.Button)`, `Icon(ic_estrella_llena)` de 22 dp tintado en `GoldPrimary` y `contentDescription = stringResource(R.string.favoritos_quitar)`, **más su propio `Modifier.semantics(mergeDescendants = true) {}`**. Ese `semantics` no es adorno: convierte la estrella en frontera de fusión y es lo único que impide que la fusión de la tarjeta se la tragase y TalkBack no pudiera borrar un favorito. Añadir `onQuitar` al parámetro de `TarjetaFavorito` y `semantics(mergeDescendants = true) {}` a la tarjeta, para que se anuncie como una frase (depende de T006, T068)
- [x] T070 [US3] Añadir a `<pkg>/ui/favoritos/FavoritosScreen.kt` las privadas `DialogoConfirmacion(titulo, mensaje, textoConfirmar, onConfirmar, onCancelar, acento = Danger)` y `BotonPlano`: `androidx.compose.ui.window.Dialog` con `DialogProperties(usePlatformDefaultWidth = false)` sobre `TarjetaAcento`, con `Modifier.semantics { paneTitle = titulo }`, icono `ic_aviso`, y dos `BotonPlano` a `weight(1f)` con `BasicText` + `TextAutoSize.StepBased(9.sp, 14.sp)` a una línea, porque «Abbrechen»/«Entfernen» es el caso que obligó al auto-ajuste en la 008. **`BotonDorado` no vale**: el dorado es el lenguaje de acción principal y un «Quitar» destructivo en dorado miente. Privadas del fichero, como `FilaIdioma` en Ajustes: suben a `ui/components/` el día que las pida un segundo consumidor (depende de T068)
- [x] T071 [US3] Modificar `<pkg>/ui/favoritos/FavoritosViewModel.kt`: `onQuitarPulsado(favorito)` abre el diálogo, `onCancelarBorrado()` lo cierra y `onConfirmarBorrado()` cierra **de inmediato** y lanza el borrado sin esperar la corrutina (si esperara, un almacenamiento lento dejaría el diálogo clavado) y emite `favoritos_borrado` con `tipo`. El id pendiente se guarda aparte y `pendienteDeBorrar` se **re-deriva** en cada emisión del flujo, descartándolo si ese favorito ya no está (depende de T044)
- [x] T072 [US3] Cablear en `FavoritosContent`, dentro de `<pkg>/ui/favoritos/FavoritosScreen.kt`, la estrella y el diálogo: `pendienteDeBorrar != null` pinta `DialogoConfirmacion` con `favoritos_borrar_mensaje` formateado con el título de la tarjeta. Añadir dos `@Preview` más: con diálogo abierto y con una sola tarjeta (depende de T069, T070, T071)
- [x] T073 [P] [US3] Ampliar `<test>/ui/favoritos/FavoritosViewModelTest.kt`: abrir, cancelar y confirmar el borrado; que confirmar llama al caso de uso una sola vez y emite `favoritos_borrado` con su `tipo`; y que `pendienteDeBorrar` se descarta si el favorito desaparece del flujo mientras el diálogo está abierto (depende de T071)
- [x] T074 [P] [US3] Ampliar `<androidTest>/ui/favoritos/FavoritosScreenTest.kt`: hay una estrella por tarjeta y ninguna con la lista vacía; **pulsar la estrella propaga `onQuitar` y NO `onAbrir`** (la prueba que protege el diseño de dos zonas pulsables); con `pendienteDeBorrar` se ven título, mensaje con el título del favorito, «Quitar» y «Cancelar», y cada botón propaga lo suyo (depende de T072)
- [x] T075 [US3] Puertas de la historia: las cuatro en verde, y recorrer a mano los pasos 16–20 de `quickstart.md`, más el 25 con TalkBack encendido para confirmar que la estrella es un nodo enfocable propio (depende de T072, T073, T074)

**Checkpoint**: las tres historias principales funcionan. El joyero guarda, recupera y limpia.

---

## Phase 6: User Story 4 - Reconocer cada favorito de un vistazo (Priority: P4)

**Goal**: la tarjeta lleva la imagen y el color de su sección, la fecha en que se guardó, y no se
convierte en un muro cuando el cálculo produce muchas cifras.

**Independent Test**: con un favorito de cada tipo en la lista, comprobar que cada tarjeta lleva su
imagen, su nombre de sección y su color; que la de una BASE inversa muestra tres cifras y dice que
quedan dos; y que la fecha sale en el formato del idioma elegido.

- [x] T076 [P] [US4] Crear `<pkg>/ui/components/Fechas.kt` con `fechaLocal(epochMillis)` y `fechaHoraLocal(epochMillis)`, moviendo aquí la lógica de `fechaHoraLocal` de `<pkg>/ui/herramientas/precios/PresentacionPrecios.kt` y conservando en el KDoc el párrafo de por qué se usa `DateFormat.getMediumDateFormat`/`getTimeFormat` y **no** `DateUtils.formatDateTime`. Actualizar la pantalla de precios para consumirla, sin cambio de comportamiento. Segundo consumidor, así que sube: la regla del proyecto
- [x] T077 [P] [US4] Añadir a los cinco `strings.xml`, en la sección «Favoritos», `favoritos_mas_lineas` («+%1$d más»). Es el único punto de la feature con riesgo de plural y la app no tiene ni un `<plurals>`: exigir en las cuatro traducciones una redacción elíptica, sin nombre que concuerde en género o número
- [x] T078 [US4] Completar `<pkg>/ui/favoritos/TarjetaFavorito.kt`: `Image` de la sección a 64 dp con `ContentScale.Fit` y `contentDescription = null` (el nombre de sección va al lado y dentro de un nodo fusionado sería ruido); acento **por sección** y no por color del oro, porque en un listado mezclado teñir de teal un oro blanco destruiría la pista «esto es ORO»; recorte a `private const val MAX_LINEAS_VISIBLES = 3` con «+N más» debajo en `TextMuted` —constante de layout, hermana del `158.dp` de `ModuleCard`, así que vive en la tarjeta y **no** en el ViewModel, que sigue emitiendo la lista completa—; y la fecha con `fechaLocal(...)` en `labelMedium` alineada a la derecha. Usar `labelMedium` y **nunca `bodySmall`**, que no está redefinido en `Typography` y saldría en la fuente por defecto de Material (depende de T076, T077)
- [x] T079 [P] [US4] Ampliar `<androidTest>/ui/favoritos/FavoritosScreenTest.kt`: un favorito con cinco cifras muestra tres y el texto de «+2 más»; una chapa sin líneas no pinta separador ni bloque de resultados; y cada tarjeta muestra su fecha (depende de T078)
- [ ] T080 [US4] Verificación de SC-009: sembrar **50 favoritos** con el bucle de `adb` del paso 29 de `quickstart.md`, abrir la pestaña y comprobar que el listado aparece completo sin espera perceptible y hace scroll sin tirones. Si no cumple, mover el mapeo de `<pkg>/ui/favoritos/FavoritosViewModel.kt` a `dispatchers.default` con `.flowOn(...)`, que es la salida ya prevista en el diseño (depende de T078)
- [ ] T081 [US4] Verificación de idiomas y accesibilidad: recorrer los pasos 21–25 de `quickstart.md` en el emulador —los cinco idiomas, la fuente del sistema al doble y TalkBack— comprobando que ninguna etiqueta desborda ni se corta, y que un favorito guardado con la app en español se lee en alemán (depende de T078, T079)

**Checkpoint**: las cuatro historias completas.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [x] T082 Borrar `<pkg>/ui/placeholder/PlaceholderScreen.kt` y `<pkg>/ui/placeholder/PlaceholderViewModel.kt`, quitar `viewModelOf(::PlaceholderViewModel)` de `<pkg>/core/di/ViewModelModule.kt` y el `import` de `PlaceholderScreen` de `AppNavHost.kt`. Favoritos era su último destino (depende de T051)
- [x] T083 Borrar de los cinco `strings.xml` la sección «Pantallas aún sin desarrollar» con `placeholder_pendiente` y la cadena `aviso_proximamente`, comprobando antes con `grep -rn "placeholder_pendiente\|aviso_proximamente" app/src` que no queda ni un consumidor. Dejar `TraduccionesTest` y `lint` en verde (depende de T081)
- [x] T084 Actualizar `CLAUDE.md`: sección nueva «Favoritos: Room, una tabla y una firma» (tabla única con el JSON, la firma canónica y sus cinco reglas, el índice único, TRUNCATE, el esquema commiteado, nada de `fallbackToDestructiveMigration`, la base fuera de Koin y por qué este data source no lleva `DispatcherProvider`); actualizar el recuento de casos de uso (19 → 24) y señalar `ResumirFavoritoUseCase` como el primero que depende de otros casos de uso y como segunda transcripción de las reglas de fila de soldaduras; documentar `ui/favoritos/` como noveno paquete de `ui/`, con su tarjeta, su primer diálogo y su `FormatoFavoritos` duplicado a propósito; anotar que `ModoEntradaSoldadura` vive ya en `domain/`; añadir `ic_estrella_llena` a la lista de iconos; actualizar el KDoc de `GoldSeparator` en `<pkg>/ui/components/JewelryTopBar.kt`, que pasa a tener un tercer consumidor, y anotar los tres `Presentacion*.kt` nuevos o ampliados de T046; y **borrar** la sección «Pantallas aún sin desarrollar» y la mención de `placeholder/` en la tabla de capas (depende de T082)
- [x] T085 [P] Revisar que ningún `Text("…")` literal nuevo se ha colado en `main` (`grep -rn 'Text("' app/src/main/java`), que no queda ningún `stringResource` huérfano, y que `git grep -n "TODO"` no señala nada de esta feature
- [x] T086 Puertas automáticas completas: `./gradlew :app:testDebugUnitTest`, `:app:lint`, `:app:assembleDebug`, `:app:compileDebugAndroidTestKotlin` y `:app:connectedDebugAndroidTest`, las cinco en verde (depende de T083)
- [ ] T087 Verificación de persistencia y release: pasos 26–28 de `quickstart.md` — reiniciar el dispositivo, comprobar que los favoritos viajan en una copia de seguridad, y **firmar un `assembleRelease` a mano, guardar un favorito y reabrirlo**, para descartar que R8 haya roto el discriminador de tipo (que es el riesgo de la regla 4 de la firma) (depende de T085)
- [x] T088 Escribir `specs/009-favoritos/verificacion.md` con el resultado de la verificación en emulador y las desviaciones respecto a este plan, al estilo del de la 008 (depende de T086)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sin dependencias. T001 va **primero de todo**: es la enmienda que autoriza KSP en la norma, y sin ella T002–T004 violarían el principio III.
- **Foundational (Phase 2)**: depende del checkpoint de la Fase 1. **Bloquea las cuatro historias.**
- **US1 (Phase 3)**: depende de la Fase 2. Es el MVP.
- **US2 (Phase 4)**: depende de la Fase 2. Necesita de US1 sólo la pantalla de la que se pulsa (T050) para verificarse de punta a punta; sus cambios de ViewModel y de rutas son independientes.
- **US3 (Phase 5)**: depende de la Fase 2 y de la tarjeta de US1 (T048), que es donde va la estrella.
- **US4 (Phase 6)**: depende de la tarjeta de US1 (T048). Es refinamiento visual sobre algo que ya sirve.
- **Polish (Phase 7)**: T081 y T082 dependen de que Favoritos ya no use el placeholder (T051), o sea de US1.

### Dentro de cada historia

Cadenas → estado → ViewModel → composables → tests. Los tests de un fichero que no existe todavía
no se pueden escribir, así que van marcados `[P]` justo detrás de lo que prueban.

### Parallel Opportunities

- **Fase 1**: T006 y T007 en paralelo con T002–T005.
- **Fase 2**: T009, T010 y T011 son tres ficheros de dominio independientes; T015, T016, T019, T020, T022, T024, T028, T030, T031 y T032 tocan ficheros distintos. La cadena larga es T017 → T023 → T029.
- **Fase 3**: T034, T035, T040, T041 y T042 no se pisan; T052, T053, T054 y T055 son cuatro ficheros de test distintos.
- **Fases 4, 5 y 6** pueden solaparse entre sí una vez cerrada la Fase 3, porque tocan zonas distintas: US2 las rutas y los ViewModels de calculadora, US3 el diálogo y el ViewModel de Favoritos, US4 la tarjeta.

## Implementation Strategy

### MVP primero (sólo US1)

1. Fase 1 (T001–T007): la norma y el build.
2. Fase 2 (T008–T033): dominio, persistencia y Koin, todo probado. **Bloqueante.**
3. Fase 3 (T034–T056): guardar de verdad y ver la lista.
4. **PARAR Y VALIDAR**: los pasos 1–9 de `quickstart.md`. En este punto el botón que llevaba cinco
   versiones diciendo «Próximamente» ya cumple, y la app es demostrable.

### Entrega incremental

- Fase 2 → hay dónde guardar.
- \+ US1 → **MVP**: el joyero guarda y ve lo guardado.
- \+ US2 → recupera un cálculo en dos toques. Es donde la feature empieza a ahorrar trabajo.
- \+ US3 → puede limpiar la lista.
- \+ US4 → la reconoce de un vistazo.
- \+ Polish → se va el andamio del placeholder y se documenta.

## Notes

- 88 tareas. Las cinco puertas (`testDebugUnitTest`, `lint`, `assembleDebug`,
  `compileDebugAndroidTestKotlin`, `connectedDebugAndroidTest`) se pasan al cerrar cada fase, no sólo
  al final.
- **T001 no es papeleo**: mientras la constitución diga «nada de KSP», T002 la incumple. La enmienda
  va antes que el build, y `CLAUDE.md` en el mismo cambio porque lo exige la cláusula de Governance.
- **T053 es el test que no se puede saltar.** `FormatoFavoritos` duplica a propósito cuatro políticas
  de redondeo, y copiar `HALF_UP` en la rama de plata es el fallo más caro de la feature: la tarjeta
  mostraría 5,158 g de cobre y la ley real caería por debajo de la objetivo.
- **T069 tampoco.** Sin el `semantics(mergeDescendants = true)` de la estrella, TalkBack no puede
  borrar un favorito, y eso no se ve mirando la pantalla.
- Cada cadena nueva son **cinco ficheros** en la misma sección y el mismo orden, con
  `TraduccionesTest` y `lint` vigilando. Y cada cadena borrada, también cinco.
- Los cinco `XScreenTest` y `HerramientasScreenTest` existentes **no deben tocarse**: si hay que
  cambiarlos, algo se ha puesto en `XContent` que pertenece a `XScreen`.
- Commits en Conventional Commits y en español, `feat(009):`, tras cada tarea o grupo lógico.

## Estado

**85 de las 88 tareas están cerradas.** Las tres que quedan son verificación manual, no
implementación: T080 (volumen con 50 favoritos), T081 (los cinco idiomas, la fuente al doble y
TalkBack) y T087 (copia de seguridad y `assembleRelease` firmado). Necesitan un humano o un emulador
en otro idioma.

Las cuatro puertas automáticas están en verde: **440 tests JVM sin fallos**, `lint`,
`assembleDebug` y `compileDebugAndroidTestKotlin`. En instrumentados, 116 de 124, con **8 fallos
preexistentes** que se reprodujeron en un *worktree* del commit anterior a la feature.

El resultado de la verificación y las siete desviaciones respecto a este plan están en
[verificacion.md](./verificacion.md).
