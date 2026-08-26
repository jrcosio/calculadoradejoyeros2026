# Implementation Plan: Favoritos — guardar y reabrir cálculos

**Branch**: `009-favoritos` | **Date**: 2026-08-26 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/009-favoritos/spec.md`

## Summary

El botón «Guardar en favoritos» existe en cinco pantallas desde cinco versiones y lo único que hace
es decir «Próximamente». Esta feature lo convierte en un guardado real, transforma el último
`PlaceholderScreen` de la app en el listado de favoritos y cierra el círculo permitiendo reabrir
cualquier cálculo guardado en su calculadora.

La pieza técnica central es **qué se guarda**: sólo las entradas del cálculo, nunca los resultados.
Los cuatro motores de dominio son Kotlin puro sobre `BigDecimal`, así que rehacer el cálculo es
aritmética; guardar texto formateado, en cambio, lo congelaría en el idioma y el redondeo del día en
que se guardó, y la app habla cinco idiomas. De esa decisión sale un `ResumirFavoritoUseCase` que
despacha a los once motores existentes, y la contrapartida de que la pantalla de Favoritos tiene que
reproducir el redondeo de vista de cada calculadora —incluido el truncado de la plata— con un test
de paridad como guardián.

La segunda pieza es **cómo se identifica un favorito**. El deduplicado (FR-006) y la idempotencia
frente a la doble pulsación (FR-009) cuelgan de una columna `firma` canónica con índice único en
SQLite, no de una comparación en Kotlin: `BigDecimal.equals` mira la escala y una comparación previa
tiene carrera. Esa necesidad de identidad única es lo que trae **Room** al proyecto, y con Room
**KSP**, que el principio III de la constitución prohíbe. La incógnita de si KSP funciona con el
Kotlin que AGP 9 lleva dentro se resolvió compilando un spike antes de escribir este plan: funciona
(`research.md` R1), y la constitución se enmienda a 1.1.0 para acotar el veto a lo que quería decir.

## Technical Context

**Language/Version**: Kotlin 2.2.10, el que trae integrado AGP 9.3.1. No se sube por libre y KSP no
lo altera.

**Primary Dependencies**: las de siempre (Compose BOM 2026.08.00, Material 3, Navigation Compose
2.9.8, Koin 4.2.2, corrutinas 1.11.0, kotlinx-serialization 1.9.0, Firebase BoM 34.18.0) más dos
nuevas: **Room 2.8.4** (`room-runtime` + `room-compiler`) y **KSP 2.3.11** como plugin. `room-ktx`
**no** se declara: `withTransaction`, `databaseBuilder` y el soporte de `Flow` resuelven desde
`room-runtime` (verificado, R6).

**Storage**: Room sobre SQLite, fichero `favoritos.db`, **una sola tabla** con las entradas en JSON y
una columna `firma` con índice único. Journal en `TRUNCATE`. Esquema versión 1, exportado y
commiteado en `app/schemas/`. Contrato completo en
[contracts/favoritos-persistidos.md](./contracts/favoritos-persistidos.md).

**Testing**: JUnit 4 + MockK 1.14.11 + Turbine 1.2.1 + `kotlinx-coroutines-test` en JVM;
`createComposeRule()` en instrumentado. Esta feature estrena **tests instrumentados que no son de
Compose** (los del DAO), así que `kotlinx-coroutines-test` y `turbine` pasan también a
`androidTestImplementation`.

**Target Platform**: Android, `minSdk 24`, `targetSdk 36`, `compileSdk 37`. Room 2.8.x exige
`minSdk 23`: entra sin condicionales de versión.

**Project Type**: aplicación Android de un solo módulo (`:app`), MVVM con `ui → domain ← data`.

**Performance Goals**: el listado aparece completo en menos de un segundo con 50 favoritos
guardados y se desplaza sin tirones (SC-009). Resumir un favorito es aritmética `BigDecimal` sin
red ni disco.

**Constraints**: sin conexión (todo local); el redondeo de vista de cada calculadora es
inviolable (SC-006); las cifras no se localizan, como en el resto de la app; toda cadena nueva son
cinco ficheros con `TraduccionesTest` y `lint` vigilando; ningún favorito se pierde sin
confirmación explícita (SC-005).

**Scale/Scope**: 7 formas de favorito, 5 calculadoras afectadas, 1 pantalla nueva, 1 tabla, 5 casos
de uso nuevos, 5 rutas que ganan argumento, 14 cadenas nuevas y 2 borradas, y un paquete
(`ui/placeholder/`) que desaparece.

### Decisiones y sus porqués

1. **Room, y una sola tabla** (R1, R2). Lo que ni `SharedPreferences` ni DataStore dan es un índice
   único que haga el deduplicado idempotente sin releer la lista, y un `Flow` que reemita al borrar.
   Room fue descartado en la 007 con el argumento «una tabla para cinco filas»: era correcto para
   una caché derivada y no aplica a datos del joyero de tamaño abierto.
2. **KSP verificado antes de planificar** (R1). Era la única incógnita capaz de cambiar la
   arquitectura entera, así que se hizo un spike de build desechable: `kspDebugKotlin`,
   `assembleDebug`, `lint` y el esquema exportado, los cuatro en verde, con la caché de
   configuración funcionando. El spike se deshizo por completo.
3. **Sólo se guardan las entradas** (R3). Los resultados son dato derivado; guardarlos formateados
   rompería FR-014 y SC-007, y guardarlos sin formatear no salvaría el redondeo. Coste asumido: si
   una versión futura corrige una receta o una densidad, los favoritos viejos mostrarán las cifras
   nuevas.
4. **Siete variantes de entradas, no una con el tipo dentro** (data-model §2). El mismo criterio que
   `CalcularSoldaduraClasicaUseCase`: «la prohibición va en el diseño de tipos, no en una
   validación». Una variante única obligaría a un color nulable en las clásicas.
5. **La identidad es una firma canónica, no el JSON** (R4). El texto que produce
   `kotlinx.serialization` depende del orden de declaración del DTO: indexarlo dejaría de detectar
   duplicados en silencio el día que alguien reordene un campo. Cinco reglas de canonización, y
   enums por `name` porque `analyticsId` colisiona (`LEY_12K` y `ORO_12K` valen los dos `"12k"`).
6. **`IGNORE` más una consulta sólo en la rama del duplicado** (R5). `INSERT … RETURNING` necesita
   API 34 y el `minSdk` es 24.
7. **`TRUNCATE` y no WAL** (R7). FR-033 exige que los favoritos viajen en la copia de seguridad, y
   con WAL una restauración puede llevarse el `.db` sin su `-wal`.
8. **La base nace `by lazy` dentro del data source y no entra en Koin** (R8), como el `DataStore` de
   la 008: `verify()` sólo inspecciona constructores del tipo primario y meterla en `extraTypes`
   debilitaría el test para todo el proyecto.
9. **Sin `DispatcherProvider` en el data source de Room** (R9), única excepción en `data/`: los
   `suspend` de un DAO ya corren en el executor de Room.
10. **`ModoEntradaSoldadura` se muda a `domain/model/`** (data-model §3), porque a partir de aquí un
    caso de uso lo recibe — el criterio literal por el que su KDoc justificaba quedarse en `ui/`.
    `FamiliaSoldadura`, `IngredienteSoldadura` y `MedidaChapa` **no** se mudan.
11. **Rutas con `favoritoId: Long? = null` y sin `SavedStateHandle`** (R10). Verificado en las
    fuentes de navigation 2.9.8; sin valor centinela, que el proyecto rechaza por escrito; y sin
    `toRoute()` en el ViewModel, que arrastraría `Bundle` y Robolectric a los tests JVM.
12. **`cargarFavorito` idempotente** (R11). No es estilo: sin el guardián, un cambio de tamaño de
    letra machaca lo que el joyero llevara editado (FR-025).
13. **El primer diálogo de la app con `Dialog` de compose-ui** (R12), no con `AlertDialog`, que
    impondría geometría y `TextButton`. Es la cuarta vez que el proyecto coge el comportamiento de
    plataforma y dibuja los píxeles.
14. **El redondeo se duplica a propósito y lo vigila un test de paridad**. `CLAUDE.md` prohíbe
    unificar las cuatro políticas; el patrón de duplicación con test de paridad ya existe para las
    milésimas de `MaterialChapa`.
15. **La constitución se enmienda a 1.1.0** (R13), con `CLAUDE.md` en el mismo cambio, como exige su
    cláusula de Governance.
16. **Telemetría: los cinco eventos `*_favoritos_proximamente` se retiran, no se renombran.** Medían
    la intención de usar algo que no existía; reutilizar el nombre mezclaría en el mismo informe de
    Firebase dos hechos incomparables. Los sustituyen `<screen_name>_favorito_guardado` con
    `resultado = nuevo | repetido` en cada calculadora, y en la pantalla nueva `favoritos_abierto` y
    `favoritos_borrado`. Sin evento de cancelación: un «he cambiado de idea» no es un hecho de
    negocio. El `screen_view` de Favoritos sigue siendo **`"favoritos"`**, el nombre que ya emitía el
    placeholder, para no romper la serie histórica — igual que hizo la 008 con `"ajustes"`.
17. **El vocabulario del parámetro `tipo` es el de las cinco secciones**, no el de las siete
    variantes: `oro`, `plata`, `soldadura`, `soldadura_base`, `chapa`. Es lo que mide la pregunta
    que interesa —desde qué zona de la app se recupera trabajo—, y no hay que confundirlo con
    `EntradasFavorito.analyticsId`, que tiene siete valores y es el discriminador de la tabla. Por
    FR-036, el evento **no lleva** cantidades ni medidas: sólo el tipo, y hay un aserto negativo que
    lo vigila.

## Constitution Check

*GATE: comprobado antes de la Fase 0 y vuelto a comprobar tras la Fase 1.*

| Principio | Estado | Cómo se cumple |
|---|---|---|
| I — Spec-Driven Development | ✅ | Ciclo completo: `spec.md` validado con su checklist 16/16, este `plan.md`, y `tasks.md` antes de una línea de producto. La spec no nombra ni una librería. |
| II — MVVM con capas estancas | ✅ | `domain/` no importa `android.*`, `androidx.*` ni `data.*`: `Favorito`, `EntradasFavorito`, `ResumenFavorito`, `ResultadoGuardado` y los cinco casos de uso son Kotlin puro. Room vive confinado en `data/source/local/`. `ui/favoritos/` sólo habla con casos de uso. Un único `StateFlow` inmutable por ViewModel, pantalla partida en dos composables con `@Preview`. La mudanza de `ModoEntradaSoldadura` a `domain/` **refuerza** el principio en vez de tensarlo. |
| III — Inyección sólo por Koin | ⚠️→✅ | Koin sigue con DSL manual y **cero `koin-annotations`**. KSP entra **sólo** como procesador de Room. La letra del principio veta KSP en todo el proyecto, así que se enmienda para acotarlo a su intención (R13, y fila 1 de Complexity Tracking). Todo por constructor; repositorio registrado por su interfaz de dominio; data source concreto con `bind` para que `verify()` lo inspeccione. |
| IV — Test obligatorio | ✅ | Los seis ViewModels tocados y los cinco casos de uso nuevos llevan test. `KoinModulesTest` cubre el grafo nuevo sin tocarse, incluidas las once dependencias de `ResumirFavoritoUseCase`. Corrutinas con `DispatcherProvider` inyectado y `TestDispatcherProvider`. Dos redes extra: el test dorado de la firma y el test de paridad del formato. |
| V — Una sola fuente para las versiones | ✅ | Room y KSP entran por `gradle/libs.versions.toml`, plugins incluidos. Ni una coordenada suelta en un `build.gradle.kts`. La versión de Kotlin la sigue fijando AGP: KSP 2.3.11 conviven con ella sin subirla (verificado). |

**Restricciones técnicas**: `minSdk 24` intacto (Room 2.8.x pide 23, sin condicionales de versión).
Ningún producto nuevo de Firebase. Nombres y comentarios en español. `google-services.json` sigue
fuera de git. Navigation con rutas type-safe `@Serializable`: las cinco que ganan argumento pasan de
`data object` a `data class`, que es la otra mitad del contrato que `Routes.kt` ya documentaba.

**Puerta especial de esta feature**: `./gradlew :app:connectedDebugAndroidTest`. Los tests del DAO
son los primeros del proyecto que necesitan un dispositivo para algo que no es Compose, y el índice
único —la pieza de la que cuelgan FR-006 y FR-009— sólo se prueba de verdad ahí. Se suma a las
cuatro puertas de la 008.

**Re-evaluación tras el diseño de la Fase 1**: sin violaciones nuevas. El diseño no añadió capas ni
módulos; añadió una tabla, un paquete de `ui/` y cinco casos de uso, y **quitó** un paquete
(`ui/placeholder/`). Las ocho desviaciones están en Complexity Tracking, y siete de las ocho son
decisiones locales sin efecto en el resto de la app.

## Project Structure

### Documentation (this feature)

```text
specs/009-favoritos/
├── spec.md                              # 4 historias, 36 FR, 9 SC
├── plan.md                              # este fichero
├── research.md                          # R1…R13; R1 verificada compilando
├── data-model.md                        # los 4 conceptos, la tabla y el estado de presentación
├── quickstart.md                        # puertas automáticas y 28 pasos de verificación manual
├── contracts/
│   └── favoritos-persistidos.md         # almacén, tabla, firma, JSON, tolerancia
├── checklists/
│   └── requirements.md                  # 16/16
└── tasks.md                             # lo genera /speckit-tasks
```

### Source Code (repository root)

Raíz del código: `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/`, abreviada `<pkg>/`.

**Ficheros nuevos — dominio** (Kotlin puro)

```text
<pkg>/domain/model/Favorito.kt                    Favorito + sealed EntradasFavorito (7 variantes)
<pkg>/domain/model/ModoEntradaSoldadura.kt        mudado desde ui/soldaduras/SoldadurasUiState.kt
<pkg>/domain/model/ResultadoGuardado.kt           Guardado(id) / YaExistia(id)
<pkg>/domain/model/ResumenFavorito.kt             sealed, 5 variantes de cifras sin formatear
<pkg>/domain/repository/FavoritosRepository.kt
<pkg>/domain/usecase/ObservarFavoritosUseCase.kt
<pkg>/domain/usecase/GuardarFavoritoUseCase.kt
<pkg>/domain/usecase/BorrarFavoritoUseCase.kt
<pkg>/domain/usecase/ObtenerFavoritoUseCase.kt
<pkg>/domain/usecase/ResumirFavoritoUseCase.kt    el primero que depende de otros casos de uso
```

**Ficheros nuevos — datos**

```text
<pkg>/data/source/local/FavoritosLocalDataSource.kt      interfaz, habla tipos de dominio
<pkg>/data/source/local/RoomFavoritosLocalDataSource.kt  la base nace by lazy aquí dentro
<pkg>/data/source/local/FavoritoEntity.kt                @Entity, índice único sobre firma
<pkg>/data/source/local/FavoritosDao.kt                  @Dao, todos los métodos abstractos
<pkg>/data/source/local/FavoritosDatabase.kt             @Database(version = 1)
<pkg>/data/source/local/FavoritoPersistidoDto.kt         @Serializable, version = 1
<pkg>/data/source/local/CodificadorFavorito.kt           JSON + FIRMA, Kotlin puro
<pkg>/data/repository/FavoritosRepositoryImpl.kt         pasarela + sello del Reloj
```

Planos en `data/source/local/`, sin subpaquete: es la forma que ya tiene `data/source/remote/`, y el
prefijo `Room…` marca la frontera tecnológica.

**Ficheros nuevos — interfaz**

```text
<pkg>/ui/favoritos/FavoritosScreen.kt        pantalla + DialogoConfirmacion + BotonPlano
                                             + TarjetaSinFavoritos + @Preview
<pkg>/ui/favoritos/TarjetaFavorito.kt        tarjeta + 2 filas compactas + EstrellaFavorito
<pkg>/ui/favoritos/FavoritosUiState.kt       estado, FavoritoUiModel, EntradasFavoritoUi,
                                             LineaFavoritoUi, ConceptoFavorito
<pkg>/ui/favoritos/FavoritosViewModel.kt
<pkg>/ui/favoritos/PresentacionFavoritos.kt  enums → recursos y acentos, título, etiqueta de total
<pkg>/ui/favoritos/FormatoFavoritos.kt       las 4 políticas de redondeo, duplicadas a propósito
<pkg>/ui/favoritos/AvisoFavorito.kt          GUARDADO / REPETIDO / SIN_DATOS
<pkg>/ui/components/Fechas.kt                fechaLocal + fechaHoraLocal, promovido de precios
```

**Recursos nuevos**

```text
app/src/main/res/drawable/ic_estrella_llena.xml      el pathData de ic_estrella, con relleno
app/schemas/…FavoritosDatabase/1.json                generado y commiteado
```

Cadenas: **12 traducibles + 2 no traducibles**, en los cinco `strings.xml`. En «Compartido:
unidades, metales y acciones»: `accion_cancelar`, `favoritos_aviso_guardado`,
`favoritos_aviso_repetido`, `favoritos_aviso_sin_datos`. En una sección nueva `<!-- Favoritos -->` al
final: `favoritos_vacio_titulo`, `favoritos_vacio_texto` (con `%1$s` para citar
`accion_guardar_favoritos`, como `precios_fuente`), `favoritos_quitar`, `favoritos_guardado_el`,
`favoritos_mas_lineas`, `favoritos_borrar_titulo`, `favoritos_borrar_mensaje`,
`favoritos_borrar_confirmar`, más `favoritos_cantidad_gramos` y `favoritos_medidas_chapa` con
`translatable="false"`. **Se borran** `placeholder_pendiente` y `aviso_proximamente` de los cinco
ficheros. `favoritos_mas_lineas` («+%1$d más») es el único punto con riesgo de plural: la app no
tiene ni un `<plurals>` y `TraduccionesTest` sólo parsea `<string>`, así que exige redacción
elíptica sin nombre que concuerde. Contrato vigente:
`specs/008-ajustes-idioma/contracts/traducciones.md`.

**Ficheros modificados**

```text
gradle/libs.versions.toml                    room, ksp, 2 librerías, 2 plugins
build.gradle.kts                             2 alias apply false
app/build.gradle.kts                         2 plugins, room { schemaDirectory }, 2 deps,
                                             2 androidTestImplementation
<pkg>/core/di/DataModule.kt                  data source + repositorio
<pkg>/core/di/DomainModule.kt                5 factoryOf
<pkg>/core/di/ViewModelModule.kt             + FavoritosViewModel, − PlaceholderViewModel
<pkg>/ui/navigation/Routes.kt                5 data object → data class con favoritoId
<pkg>/ui/navigation/AppNavHost.kt            Favoritos deja de ser placeholder; TipoFavorito.ruta();
                                             los 2 mapeos privados pasan a Route.Oro() etc.
<pkg>/ui/components/Tarjetas.kt              TarjetaAcento gana onClick, con clip CONDICIONAL
<pkg>/ui/components/JewelryTopBar.kt         KDoc de GoldSeparator: tercer consumidor
<pkg>/ui/herramientas/precios/PresentacionPrecios.kt   fechaHoraLocal sube a components/Fechas.kt
<pkg>/ui/{oro,plata,soldaduras,herramientas/chapas}/   los 5 UiState (+ avisoFavorito), los 5
                                                       ViewModel (guardar, cargarFavorito, aviso) y
                                                       las 5 pantallas (favoritoId + Toast)
<pkg>/ui/herramientas/HerramientasViewModel.kt         abrirFavoritoDeChapa(), sin telemetría
<pkg>/ui/soldaduras/*.kt (6 ficheros)                  sólo import de ModoEntradaSoldadura
app/src/main/res/values{,-en,-fr,-de,-it}/strings.xml  14 altas y 2 bajas
app/src/main/res/xml/backup_rules.xml                  comentario: los favoritos NO se excluyen
app/src/main/res/xml/data_extraction_rules.xml         ídem
.specify/memory/constitution.md                        principio III, persistencia, 1.1.0
CLAUDE.md                                              sección de Favoritos, KSP, casos de uso,
                                                       fuera la sección de placeholder
```

**Ficheros borrados**

```text
<pkg>/ui/placeholder/PlaceholderScreen.kt
<pkg>/ui/placeholder/PlaceholderViewModel.kt
```

**Tests nuevos**

```text
<test>/domain/model/FavoritosDePrueba.kt                  fixtures, patrón CotizacionesDePrueba
<test>/domain/usecase/ResumirFavoritoUseCaseTest.kt       11 filas: variante × modo
<test>/domain/usecase/{Observar,Guardar,Borrar,Obtener}FavoritoUseCaseTest.kt
<test>/data/source/local/CodificadorFavoritoTest.kt       ida y vuelta + 7 firmas doradas + tolerancia
<test>/data/source/local/FakeFavoritosLocalDataSource.kt
<test>/data/repository/FavoritosRepositoryImplTest.kt
<test>/data/repository/FakeFavoritosRepository.kt
<test>/ui/favoritos/FavoritosViewModelTest.kt
<test>/ui/favoritos/FormatoFavoritosTest.kt
<test>/ui/favoritos/FavoritosParidadFormatoTest.kt        el que legitima la duplicación
<androidTest>/data/source/local/FavoritosDaoTest.kt       índice único, orden, borrado
<androidTest>/data/source/local/RoomFavoritosLocalDataSourceTest.kt   un solo @Test, fichero real
<androidTest>/ui/favoritos/FavoritosScreenTest.kt         vacío, lista, las 2 zonas, recorte, diálogo
```

Ampliados: los cinco `X ViewModelTest` de las calculadoras (los `crearViewModel()` cambian de
firma, y el test de «favoritos sólo registra telemetría» deja de ser verdad),
`HerramientasViewModelTest` y `TraduccionesTest` (que no cambia de código pero exige las 14 altas y
las 2 bajas). Los cinco `XScreenTest` y `HerramientasScreenTest` **deben seguir pasando sin
tocarse**: el `Toast` y el `LaunchedEffect` viven en `XScreen`, las firmas de `XContent` no cambian
y el campo nuevo de cada `UiState` tiene valor por defecto.

**Structure Decision**: módulo único `:app` con las tres capas de siempre. Favoritos no justifica un
módulo aparte: comparte los cuatro motores, los componentes de `ui/components/` y el grafo de Koin.
La única novedad estructural es que `data/source/local/` pasa de dos tecnologías (DataStore y
SharedPreferences) a tres, y el prefijo tecnológico de cada implementación sigue siendo lo que las
distingue.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| **KSP en el proyecto**, contra la letra del principio III | Room genera sus `_Impl` con KSP; no hay Room sin procesador de anotaciones para fuentes Kotlin | Se enmienda el principio a su intención («nada de `koin-annotations`») en vez de ignorarlo. KSP queda autorizado **sólo** para Room y jamás para inyección. Verificado que funciona con el Kotlin de AGP 9 (R1) |
| **Room como dependencia nueva**, contra la regla del segundo consumidor de la 007 | Un solo consumidor, pero es el único que da identidad única en la base: sin índice único, FR-006 y FR-009 se resuelven con una lectura-comparación-escritura que tiene carrera con la doble pulsación | JSON en DataStore reescribe la lista entera en cada borrado y no resuelve la carrera; SQLite a mano conserva tabla e índice pero cambia el DAO generado por SQL escrito a mano y su propio mapeo |
| **Dos plugins nuevos**, no uno | El segundo (`androidx.room`) es sólo para `schemaDirectory` | `ksp { arg("room.schemaLocation", …) }` deja la carpeta como salida no declarada de la tarea y rompe la relocalización de la caché de Gradle, que este proyecto tiene activada |
| **`ResumirFavoritoUseCase` con once casos de uso por constructor**, el primero del proyecto que depende de otros casos de uso | La alternativa es que `FavoritosViewModel` inyecte los once y repita el despacho de las cinco calculadoras más las reglas FR-022 de la 006 | Eso pondría lógica de dominio en presentación y la duplicaría por sexta vez. `KoinModulesTest` verifica el constructor entero gratis |
| **`FormatoFavoritos` duplica las cuatro políticas de redondeo** | `CLAUDE.md` prohíbe unificarlas por escrito: son documentos técnicos distintos y en plata el truncado lo exige la Ley 17/1985 | Un helper compartido parametrizado por política se leería como la unificación que la norma prohíbe. La duplicación va protegida por `FavoritosParidadFormatoTest`, el mismo patrón que ya vigila las milésimas de `MaterialChapa` |
| **Primer diálogo de la app** | FR-028 exige confirmar antes de borrar, y ni `Toast` ni `AvisoTecnico` pueden preguntar | `AlertDialog` de M3 impone padding, ancho, forma y `TextButton`, que sería el primer botón de Material de la app; `BasicAlertDialog` paga un opt-in experimental y una segunda fuente de traducciones. Se usa `Dialog` de compose-ui, que sólo aporta la ventana |
| **Primeras rutas con argumento** (`data class` en `Routes.kt`) | FR-020 no tiene otra forma honesta: el favorito tiene que viajar del listado a la calculadora | Un buzón compartido en Koin sería estado global mutable y un canal invisible; `SavedStateHandle` arrastraría `Bundle` y Robolectric a los tests JVM de los cinco ViewModels |
| **El data source de Room no recibe `DispatcherProvider`**, única excepción en `data/` | Los `suspend` de un DAO y los `Flow` de Room ya corren en el executor de Room | Un `withContext(dispatchers.io)` encima sería un salto de hilo de adorno, y `setQueryExecutor` con un dispatcher de un solo hilo se bloquea en una transacción. Se documenta en su KDoc, porque contradice la lectura rápida de la norma |
