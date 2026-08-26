# Research: Favoritos — guardar y reabrir cálculos

**Feature**: `009-favoritos` | **Fecha**: 2026-08-26 | **Spec**: [spec.md](./spec.md)

Trece decisiones. La R1 se resolvió **compilando**, no leyendo: era la única capaz de tumbar la
feature y de cambiar la arquitectura entera, así que se hizo un spike de build desechable antes
de escribir el plan.

---

## R1 — ¿Room y KSP funcionan con el Kotlin integrado de AGP 9? **VERIFICADO**

**Decisión**: Sí. Room **2.8.4** con KSP **2.3.11** y el plugin `androidx.room` funcionan en este
proyecto tal cual está.

**Por qué era una incógnita y no una consulta de documentación**: aquí la versión de Kotlin la
fija el Kotlin integrado de AGP 9.3.1 (2.2.10) y **no existe el plugin `kotlin-android`**,
mientras que el plugin de KSP ha dependido históricamente del Kotlin Gradle Plugin y sus
versiones eran del tipo `2.2.10-2.0.2`, atadas a una versión exacta de Kotlin. La línea 2.3.x de
KSP está desacoplada (la guía oficial empareja Kotlin 2.4.10 con KSP 2.3.10, y la nota de
2.3.10 menciona expresamente «R-class resolution in KSP with AGP 9»), pero eso no demuestra que
la combinación exacta de este repo funcione.

**Cómo se verificó**: spike desechable con el catálogo, los dos plugins, `room { schemaDirectory }`,
`implementation(room-runtime)`, `ksp(room-compiler)` y tres tipos mínimos (entidad con índice
único, DAO con `Flow` + `@Insert(IGNORE)` + consultas, y `@Database`). Resultados, todos en verde:

| Comprobación | Resultado |
|---|---|
| `./gradlew :app:kspDebugKotlin` | **BUILD SUCCESSFUL**, con `:app:copyRoomSchemas` ejecutándose |
| Fuentes generadas | `build/generated/ksp/debug/kotlin/…/SpikeDatabase_Impl.kt` y `SpikeDao_Impl.kt` |
| Esquema exportado | `app/schemas/…SpikeDatabase/1.json` (el plugin `androidx.room` hace el trabajo) |
| `./gradlew :app:assembleDebug` | **BUILD SUCCESSFUL** |
| `./gradlew :app:lint` | **BUILD SUCCESSFUL** — la puerta de calidad de la 008 sigue en verde |
| Caché de configuración | «Configuration cache entry stored» en las cuatro ejecuciones |

El spike se deshizo por completo (`git status` limpio salvo `specs/009-favoritos/`) y el proyecto
vuelve a compilar sin Room. La implementación real es tarea de `/speckit-implement`.

**Alternativas descartadas** (ya no hacen falta, pero quedan documentadas por si una subida futura
de AGP rompe esto):
- `ksp = "2.2.10-2.0.2"`, la release cuyo prefijo casa exactamente con el Kotlin de AGP. Era el
  primer escape; innecesario.
- **SQLite a mano** (`SQLiteOpenHelper` + SQL propio), sin procesador de anotaciones. Era el
  segundo escape y conservaba la tabla, el índice único y la firma canónica, perdiendo sólo el DAO
  generado. Innecesario.
- `fallbackToDestructiveMigration`: descartado siempre. Los favoritos son del joyero.

**Consecuencia de gobierno**: el principio III de la constitución dice «Nada de `koin-annotations`
ni de KSP». La frase está escrita dentro del principio de inyección de dependencias y su intención
es prohibir Koin por anotaciones, pero tal como está redactada veta KSP en todo el proyecto. Se
enmienda (ver R13).

---

## R2 — ¿Room, o el patrón de persistencia que ya existe en el repo?

**Decisión**: Room, con **una sola tabla**.

**Rationale**: el repo ya tiene dos almacenes y ninguno encaja. `SharedPreferences` con una clave
y un JSON (caché de cotizaciones, 007) obliga a reescribir la lista entera en cada borrado y no se
observa. Preferences DataStore (idioma, 008) se observa, pero guarda pares clave-valor: una lista
de objetos compuestos acabaría siendo un JSON gigante en una clave, con el mismo problema de
reescritura. Room da lo que esta feature necesita y ellos no: **un índice único que hace el
deduplicado idempotente sin releer nada** (FR-006, FR-009) y un `Flow` que reemite al borrar
(FR-030).

**Alternativas consideradas**:
- **JSON en DataStore**: cero dependencias nuevas y todo testeable en JVM puro. Rechazado por el
  deduplicado y el borrado: los dos exigen leer la lista completa, compararla y reescribirla, y con
  dos pulsaciones rápidas hay carrera. El índice único de SQL es la solución honesta.
- **Una columna por entrada** (11 columnas nulables + conversores de `BigDecimal`): rechazado
  porque obligaría a una **migración de esquema cada vez que una calculadora gane una entrada**.
  Con JSON en una columna, el esquema no se toca nunca.
- Room fue descartado una vez en `specs/007-herramientas/research.md` (R4) con el argumento «una
  tabla para cinco filas». Ese argumento era correcto **para una caché derivada de cinco
  cotizaciones** y no aplica aquí: esto es dato del joyero, de tamaño abierto, que se observa y
  que necesita identidad única.

---

## R3 — ¿Se guardan los resultados o sólo las entradas?

**Decisión**: **sólo las entradas** (FR-002, FR-003). El resumen se rehace con los motores de
siempre en un `ResumirFavoritoUseCase`.

**Rationale**: tres razones, en orden de peso. (1) La app habla cinco idiomas: un texto formateado
guardado en español se vería en español con la app en alemán, y eso rompe FR-014 y SC-007. (2) Los
cuatro motores son Kotlin puro sobre `BigDecimal`, sin red ni disco: rehacer el cálculo de unas
decenas de favoritos es aritmética, no un coste. (3) Un resultado guardado es dato derivado con
fecha, exactamente lo que la 007 decidió no arrastrar en su caché.

**Alternativas consideradas**:
- **Guardar el resumen ya formateado**: tarjeta trivial de pintar, pero congelada en el idioma y en
  el redondeo del día en que se guardó. Rechazado.
- **Guardar el resumen sin formatear** (`BigDecimal`): salva el idioma pero no el redondeo, duplica
  en la base algo que la aritmética da gratis, y obliga a versionar el formato de los resultados
  además del de las entradas. Rechazado.

**Coste asumido y declarado en la spec**: si una versión futura corrige una receta de
`RecetasSoldadura` o una densidad de `MaterialChapa`, los favoritos viejos mostrarán las cifras
nuevas sin avisar. Un favorito es una receta, no un recibo. Va al KDoc de `ResumirFavoritoUseCase`.

---

## R4 — Identidad de un favorito: ¿cómo se detecta el duplicado?

**Decisión**: una columna **`firma`** con índice único, calculada por un `when` explícito sobre las
entradas. **No** se indexa el JSON.

**Rationale**: el índice tiene que colgar de algo que signifique «este cálculo es el mismo». El
texto que produce `kotlinx.serialization` **no** lo es: depende del orden de declaración del DTO y
de `encodeDefaults`, así que el día que alguien reordene un campo, el JSON de una entrada idéntica
cambia de forma y el índice deja de detectar duplicados **en silencio**. La firma la escribe una
función a mano, así que reordenar el DTO no puede cambiar la identidad de nada, y `datosJson` queda
libre para evolucionar.

Formato, con la versión **por tipo** delante: `tipo|v1|campo=valor|…`

```
oro|v1|masa=30|color=AMARILLO|ley=LEY_18K
plata|v1|masa=100|ley=LEY_925
soldadura_ley|v1|cant=2|dureza=MUY_FLOJA|color=AMARILLO|modo=DESDE_METAL
soldadura_clasica|v1|cant=5|tipo=FLOJA|modo=PESO_FINAL
soldadura_plata|v1|cant=10|tipo=NORMAL|modo=DESDE_METAL
soldadura_base|v1|cant=10|modo=DESDE_METAL
chapa|v1|ancho=10|largo=30|espesor=2|material=ORO_18K
```

Cinco reglas de normalización, que son lo que hace cumplir FR-007:

1. **La coma no llega al dominio.** `parsearDecimalPositivo` (`core/util/Decimales.kt`) hace
   `trim()`, `replace(',', '.')` y rechaza lo no numérico y lo ≤ 0 antes de construir las entradas.
2. **Decimales con `stripTrailingZeros().toPlainString()`**: da una representación única por valor
   numérico. `30`, `30.0`, `030`, `+30` y `3e1` → `30`; `0.50` → `0.5`. Un `setScale` fijo sería
   peor: truncaría un espesor de `0.15` y haría colisionar dos chapas distintas.
3. **Enums por `name`, nunca por `analyticsId`**. `analyticsId` es otro contrato, puede cambiar, y
   además **colisiona**: `LeyOro.LEY_12K.analyticsId == MaterialChapa.ORO_12K.analyticsId == "12k"`.
   Es el mismo criterio de `InstantaneaPersistidaDto`, que persiste `metal.name`.
4. **Orden de campos escrito a mano**, copiado del orden de parámetros del motor. Nunca reflexión
   y nunca `::class.simpleName`: R8 lo ofusca y en release **todos** los favoritos dejarían de
   leerse.
5. **Separadores `|` y `=`**, imposibles en un `name` de enum y en un decimal canónico: sin escapes
   y sin hash. Un SHA-256 sólo añadiría un `MessageDigest` y un paso irreversible; la firma legible
   se lee tal cual al depurar el `.db`.

**Alternativas consideradas**: índice único sobre `(tipo, datosJson)` — rechazado por el fallo
silencioso descrito; comparación en Kotlin al guardar (`favoritos.any { it.entradas == nuevas }`) —
rechazado dos veces: hay carrera con la doble pulsación, y `BigDecimal.equals` **mira la escala**,
así que `Oro(BigDecimal("30"))` y `Oro(BigDecimal("30.0"))` no son iguales aunque sean el mismo
favorito.

**Test dorado obligatorio**: las siete cadenas de arriba, literales, en `CodificadorFavoritoTest`.

---

## R5 — El duplicado tiene que devolver el id del que ya estaba

**Decisión**: `@Insert(onConflict = IGNORE)` + un `SELECT id WHERE firma = :firma` **sólo en la
rama del duplicado**, todo dentro de `baseDatos.withTransaction { }`.

**Rationale**: `IGNORE` devuelve `-1` cuando el índice único rechaza la fila, y eso dice *que*
existía pero no *cuál*. La pantalla necesita el id. `INSERT … ON CONFLICT DO NOTHING RETURNING id`
lo resolvería en un viaje, pero **necesita SQLite 3.35, o sea API 34**, y el `minSdk` es 24: no es
una opción. La transacción garantiza que entre el rechazo y la consulta la fila no puede
desaparecer. La consulta extra sólo ocurre cuando el joyero guarda algo repetido.

**Verificado en el spike**: `@Insert(onConflict = IGNORE) suspend fun insertar(...): Long` compila y
genera su `_Impl`.

**Alternativas**: `REPLACE` (borraría y recrearía la fila, cambiando el id y la fecha, justo lo
contrario de FR-006); `@Transaction` con cuerpo en la interfaz del DAO (los `DefaultImpls` de una
interfaz Kotlin con `suspend` son un campo de minas del compilador de Room; `withTransaction` sobre
la instancia es la misma transacción exclusiva y se lee mejor).

---

## R6 — `withTransaction` y `Flow`: ¿hace falta `room-ktx`? **VERIFICADO**

**Decisión**: **no**. `room-runtime` basta.

**Cómo se verificó**: en el spike, un `suspend fun` con `baseDatos.withTransaction { }`,
`Room.databaseBuilder(...)`, `.setJournalMode(TRUNCATE)` y un DAO que devuelve
`Flow<List<Entity>>` compilaron con **sólo** `implementation(room-runtime)` declarado.
`:app:compileDebugKotlin` en verde. `room-ktx` no se declara.

---

## R7 — WAL o TRUNCATE

**Decisión**: `setJournalMode(RoomDatabase.JournalMode.TRUNCATE)`.

**Rationale**: FR-033 exige que los favoritos viajen en la copia de seguridad y en la
transferencia a un móvil nuevo. Con WAL —el modo por defecto— el `.db` puede quedarse atrás
respecto a su `-wal`, y una restauración que se lleve uno y no el otro puede perder los últimos
favoritos o dejar la base inconsistente. Con TRUNCATE el favorito vive en un `favoritos.db`
autocontenido. Se escribe una vez por pulsación de botón: el rendimiento que compra WAL aquí no
vale nada.

---

## R8 — Dónde vive la base y por qué no entra en Koin

**Decisión**: la base nace `by lazy` **dentro** de `RoomFavoritosLocalDataSource`, que se registra
como `single`. `FavoritosDatabase` y `FavoritosDao` **no** se registran en Koin.

**Rationale**: es exactamente el precedente de `DataStoreAjustesLocalDataSource` (008, R7).
`KoinModulesTest.verify()` sólo inspecciona los constructores del **tipo primario** de cada
definición, así que una base nacida de `Room.databaseBuilder` habría que meterla en `extraTypes`,
debilitando el test para todo el proyecto. El `single` da la instancia única por fichero que Room
exige. El data source se registra **concreto con `bind`** a su interfaz, como los de cotizaciones,
para que `verify()` sí compruebe su constructor.

---

## R9 — ¿`DispatcherProvider` en el data source de Room?

**Decisión**: **no**, y es la única excepción en `data/`.

**Rationale**: los `suspend` de un DAO y los `Flow` de Room ya se ejecutan en el executor de Room y
son seguros desde el hilo principal; un `withContext(dispatchers.io)` encima sería un salto de hilo
de adorno. Y pasar el dispatcher inyectado como `setQueryExecutor` es el camino corto al bloqueo
cuando ese dispatcher es de un solo hilo y hay una transacción por medio. La regla del proyecto
(«nunca `Dispatchers.IO` directo») se respeta: no se nombra ningún dispatcher. Va en el KDoc del
data source, porque contradice la lectura rápida de la constitución.

---

## R10 — Cómo llega el favorito a la calculadora

**Decisión**: las cinco rutas afectadas pasan a `@Serializable data class X(val favoritoId: Long? = null)`,
la pantalla recibe el id como parámetro y llama a `viewModel.cargarFavorito(id)` desde un
`LaunchedEffect`. Sin `SavedStateHandle`.

**Rationale**: los valores por defecto en rutas type-safe funcionan en navigation 2.9.8 —
`RouteSerializer.generateNavArguments()` acepta elementos opcionales, `RouteBuilder` los emite como
query param y `RouteDecoder` los salta para que el default de Kotlin se aplique—, y `Long?` está
soportado nativamente: `NavTypeConverter` lo mapea a `InternalNavType.LongNullableType`, un
`NavType<Long?>(true)` completo. Lo que está prohibido es un nullable **primitivo** sobre
`NavType.LongType`, que declara `isNullableAllowed = false`; el serializador no pasa por ahí.

**Sin valor centinela** (`favoritoId: Long = 0L`): el proyecto los rechaza por escrito («la
ausencia de clave es Automático, **no hay valor centinela**»; `familia = null` para la primera
visita; `subherramienta = null`). Un centinela obligaría además a documentar en la capa de datos
que el id 0 no existe nunca, una restricción invisible que se paga en la primera migración.

**`SavedStateHandle` + `toRoute()` rechazado** por dos motivos: obligaría a `extraTypes` en
`KoinModulesTest`, y sobre todo `toRoute()` acaba en `NavType` sobre `Bundle`, que no existe en un
test JVM pelado — habría que meter Robolectric para probar los cinco ViewModels.

**Buzón compartido rechazado**: un `single` que Favoritos rellena y el ViewModel consume sería
estado global mutable y un canal invisible.

---

## R11 — `cargarFavorito` tiene que ser idempotente

**Decisión**: guardián `private var favoritoAplicado = false`, puesto a `true` **antes** del
`launch`.

**Rationale**: no es estilo, es pérdida de datos (FR-025). El ViewModel sobrevive al cambio de
configuración; la composición **no**. Un cambio de tamaño de letra, de tema o de idioma del sistema
recrea la Activity, relanza el `LaunchedEffect` y **machaca lo que el joyero llevara editado**.
`android:screenOrientation="portrait"` tapa el caso más frecuente pero no lo elimina. El precedente
exacto es `PlaceholderViewModel.registrada`. Ponerlo a `true` antes del `launch` y no dentro evita
que dos recomposiciones seguidas encolen dos cargas.

Y `aplicar()` construye el estado completo **en una sola asignación**, sin pasar por los setters
públicos: en soldaduras es obligatorio, porque `onFamiliaSeleccionada` hace
`_uiState.value = SoldadurasUiState(familia = familia)` por FR-023 de la 006 —o sea, borra todo lo
demás—, así que una secuencia encadenada perdería la cantidad y emitiría eventos
`soldaduras_calculado` intermedios.

---

## R12 — El primer diálogo de la app

**Decisión**: `androidx.compose.ui.window.Dialog` + una tarjeta propia sobre `TarjetaAcento`.

**Rationale**: la app no tiene ni un `AlertDialog`, `BasicAlertDialog`, `ModalBottomSheet` o
`Snackbar`; su único aviso efímero es `Toast`. Y rechaza a propósito los componentes de Material
que imponen su geometría: `NavigationBar`, `Button` y `SegmentedButton` están escritos a mano.
`Dialog` aporta lo único que hay que delegar a la plataforma —ventana, botón atrás y toque fuera— y
no impone un píxel. De Material 3 se copia a mano lo único valioso,
`Modifier.semantics { paneTitle = … }`, que es API pública y estable.

**Alternativas**: `AlertDialog` de M3 impone padding de 24 dp, ancho máximo, forma, elevación
tonal y **`TextButton`** para las acciones, que sería el primer botón de Material de la app —
justo lo que `BotonDorado` existe para evitar. `BasicAlertDialog` es `Dialog` más un `paneTitle`
sacado de las traducciones de Material, a cambio de un `@OptIn(ExperimentalMaterial3Api::class)`:
paga un opt-in experimental y una segunda fuente de traducción por dos líneas.

**`BotonDorado` no vale para los botones del diálogo**: está documentado que el dorado es el
lenguaje de acción principal de la app, y un «Quitar» destructivo en dorado miente. Nace un
`BotonPlano` privado, con `BasicText` + `TextAutoSize` como `BotonDorado`, porque dos botones a
`weight(1f)` con «Abbrechen» y «Entfernen» es el escenario que obligó a introducir el auto-ajuste
en la 008.

---

## R13 — La enmienda de la constitución

**Decisión**: enmendar el principio III y subir la constitución a **1.1.0**, en el mismo commit que
`CLAUDE.md`.

**Rationale**: el principio III dice «Koin con DSL manual. Nada de `koin-annotations` ni de KSP».
La frase vive dentro del principio de inyección de dependencias y su intención es prohibir Koin por
anotaciones, pero tal como está redactada veta KSP en todo el proyecto, y Room lo necesita. Se acota
a lo que quería decir y se añade la autorización explícita y limitada:

```
- Koin con DSL manual. Nada de `koin-annotations`.
- KSP se usa **sólo** como procesador de anotaciones de Room. Jamás para inyección de dependencias.
```

Más una línea en «Restricciones técnicas» sobre persistencia (preferencias en DataStore, caché
derivada en SharedPreferences, datos del joyero en Room; nada de `fallbackToDestructiveMigration`;
el esquema exportado se commitea). Es un cambio **MINOR**: relaja una prohibición sin retirar un
principio. La cláusula de Governance obliga a actualizar `CLAUDE.md` en el mismo cambio.

**Alternativa descartada**: dejar la norma como está y justificar sólo en Complexity Tracking.
Cuesta menos hoy y deja una regla escrita que la propia app incumple, que la próxima revisión
volverá a levantar.
