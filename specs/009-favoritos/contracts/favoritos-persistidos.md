# Contrato: los favoritos persistidos

**Feature**: `009-favoritos` | **Consumidor único**: `data/source/local/RoomFavoritosLocalDataSource`

Nadie fuera de ese fichero conoce la base, la tabla ni el JSON. El resto de la app habla con
`domain/repository/FavoritosRepository`.

## Almacén

| Aspecto | Valor |
|---|---|
| Tecnología | Room 2.8.4 sobre SQLite, procesado con KSP 2.3.11 (verificado: `research.md` R1) |
| Nombre del fichero | `favoritos.db` |
| Ruta real | `/data/data/com.jrblanco.calculadoradejoyeros2021/databases/favoritos.db` |
| Journal | `TRUNCATE`, no WAL: un único fichero autocontenido, sin `-wal`/`-shm` que una restauración pueda dejar a medias |
| Instancia | Una sola, creada `by lazy` dentro del data source y garantizada por su `single` de Koin. La base **no** se registra en Koin |
| Versión del esquema | 1. `exportSchema` en su valor por defecto (`true`); `app/schemas/…FavoritosDatabase/1.json` se commitea |
| Migraciones | Ninguna todavía. **Prohibido `fallbackToDestructiveMigration`**: son datos del joyero |
| Copia de seguridad | **Incluida**, en la nube y en la transferencia entre dispositivos (FR-033). No aparece en ninguna exclusión de `res/xml/`, y eso se documenta con un comentario en los dos ficheros |

## Tabla `favoritos`

| Columna | Tipo | Restricción | Significado |
|---|---|---|---|
| `id` | `INTEGER` | `PRIMARY KEY AUTOINCREMENT` | Identidad y **orden** (monótona) |
| `tipo` | `TEXT` | no nula | Discriminador de variante. Uno de los siete valores de abajo |
| `firma` | `TEXT` | no nula, **`UNIQUE`** | Identidad del cálculo. El índice es lo que impide el duplicado |
| `datosJson` | `TEXT` | no nula | Las entradas serializadas, con su propia `version` dentro |
| `guardadoEnEpochMillis` | `INTEGER` | no nula | Sólo para mostrar la fecha |

Valores válidos de `tipo`: `oro`, `plata`, `soldadura_ley`, `soldadura_clasica`, `soldadura_plata`,
`soldadura_base`, `chapa`.

## La firma

Es la identidad del favorito. La escribe un `when` explícito **sobre las entradas de dominio**,
nunca sobre el DTO ni sobre el texto JSON: si colgara del JSON, reordenar un campo del DTO cambiaría
la forma del texto y el índice dejaría de detectar duplicados **en silencio**.

Formato `tipo|vN|campo=valor|…`, con la versión **por tipo**:

```
oro|v1|masa=30|color=AMARILLO|ley=LEY_18K
plata|v1|masa=100|ley=LEY_925
soldadura_ley|v1|cant=2|dureza=MUY_FLOJA|color=AMARILLO|modo=DESDE_METAL
soldadura_clasica|v1|cant=5|tipo=FLOJA|modo=PESO_FINAL
soldadura_plata|v1|cant=10|tipo=NORMAL|modo=DESDE_METAL
soldadura_base|v1|cant=10|modo=DESDE_METAL
chapa|v1|ancho=10|largo=30|espesor=2|material=ORO_18K
```

Reglas de canonización, y son contrato:

| # | Regla | Consecuencia |
|---|---|---|
| 1 | Las cantidades llegan normalizadas por `parsearDecimalPositivo`: la coma ya es punto y lo ≤ 0 está rechazado | El dominio nunca ve `«30,0»` ni un blanco |
| 2 | Decimales con `stripTrailingZeros().toPlainString()` | `30`, `30.0`, `030`, `+30` y `3e1` → `30`; `0.50` → `0.5` |
| 3 | Enums por `name`, **nunca** por `analyticsId` | `analyticsId` es otro contrato y colisiona: `LeyOro.LEY_12K` y `MaterialChapa.ORO_12K` valen los dos `"12k"` |
| 4 | Orden de campos escrito a mano, copiado del orden de parámetros del motor. Sin reflexión y sin `::class.simpleName` | R8 ofusca los nombres de clase: con reflexión, en release **ningún** favorito se leería |
| 5 | Separadores `\|` y `=`, imposibles en un `name` de enum y en un decimal canónico | Sin escapes y sin hash; la firma se lee tal cual al depurar el `.db` |

Subir la versión de un tipo (`v1` → `v2`) es el protocolo para cuando una variante gane o pierda un
campo: las filas `v1` conviven, se deduplican entre ellas y no colisionan con las `v2`.

## `datosJson`

DTO plano, `@Serializable`, con los campos de las siete variantes nulables, los decimales como
`String` **ya canónico** (regla 2) y los enums por `name`. El discriminador **no** va dentro: vive
en la columna `tipo`, que es la que se consulta. Mismo patrón que `InstantaneaPersistidaDto` de
la 007, incluido el `version: Int = 1` y el `Json { ignoreUnknownKeys = true; encodeDefaults = true }`.

Al reabrir un favorito, el campo de la calculadora se rellena desde el decimal canónico, no desde
el literal tecleado: quien escribió `30,000` verá `30`.

## Operaciones

```kotlin
interface FavoritosLocalDataSource {
    val favoritos: Flow<List<Favorito>>                       // más reciente primero
    suspend fun guardar(entradas: EntradasFavorito, guardadoEnEpochMillis: Long): ResultadoGuardado
    suspend fun borrar(id: Long)
    suspend fun obtener(id: Long): Favorito?
}
```

`guardar` es **idempotente**, y lo es por el índice único y no por una comprobación previa:

1. `@Insert(onConflict = IGNORE)` → devuelve el `id` nuevo, o `-1` si la firma ya estaba.
2. Si devolvió `-1`, y **sólo entonces**, `SELECT id FROM favoritos WHERE firma = :firma` para poder
   responder `YaExistia(id)`.
3. Los dos pasos van dentro de `withTransaction { }`: entre el rechazo y la consulta la fila no
   puede desaparecer.

No se usa `INSERT … ON CONFLICT DO NOTHING RETURNING id`, que lo haría en un viaje: necesita SQLite
3.35, o sea **API 34**, y el `minSdk` es 24.

`borrar` con un id que ya no existe **no es un error**: devuelve 0 filas afectadas y se ignora.

## Tolerancia a lo que no se entiende

| Situación | Comportamiento | Requisito |
|---|---|---|
| `tipo` desconocido (versión futura de la app) | La fila se **descarta de la lista** y **no se borra** | FR-034 |
| Valor de enum desconocido (`ley=LEY_22K`) | Ídem | FR-034 |
| Firma `v2` que esta versión no genera | Ídem, y **no puede** producir un `YaExistia` fantasma, porque ninguna firma que esta versión escriba coincidirá con ella | FR-034, FR-006 |
| `datosJson` truncado o ilegible | Ídem: `SerializationException` → la fila no se convierte | FR-034 |
| Cantidad ≤ 0 en el JSON | Ídem: el `require` de la variante lanza `IllegalArgumentException` y se captura | FR-034 |
| Claves de más en el JSON | Se **ignoran** y la fila se lee: `ignoreUnknownKeys = true` | FR-034 |

Al contrario que la caché de cotizaciones de la 007, que **sí** borra lo que no entiende: aquello
era dato derivado y esto es una decisión del joyero. Es el criterio de la preferencia de idioma
de la 008.

**Residuo asumido**: una fila que esta versión no entiende es invisible **e imborrable** desde la
app. Sólo puede aparecer tras volver de una versión más nueva, y le espera intacta si el joyero
vuelve a ella. Mostrarla como «guardado por una versión más reciente» con su botón de borrar es una
mejora futura, no v1.

## Compatibilidad hacia delante

- Una calculadora que gane una entrada: campo nuevo en el DTO y **firma `v2` de ese tipo**. Sin
  migración de esquema.
- Una calculadora nueva: variante nueva, `tipo` nuevo, `v1`. Sin migración de esquema.
- Una versión anterior de la app leyendo lo nuevo: descarta lo que no entiende y conserva la fila.
- El esquema sólo cambiaría si hiciera falta una columna nueva, y ese día se estrena
  `room-testing` con `MigrationTestHelper` y el `1.json` commiteado como punto de partida.

## Qué se prueba y cómo

**En JVM**, porque es Kotlin puro:

- `CodificadorFavoritoTest`: ida y vuelta de las siete variantes; las **siete firmas literales** de
  arriba como test dorado; `firma(Oro("30")) == firma(Oro("30.0"))`; firma distinta al cambiar
  cualquier entrada, incluido el `modo`; chapa 10/2/30 para que un cruce ancho↔largo falle;
  `version` presente; y los seis casos de la tabla de tolerancia.
- `FavoritosRepositoryImplTest`, con un `FakeFavoritosLocalDataSource`: el sello del `Reloj`, el
  orden, y que una fila ilegible no aparece en la lista.

**En el emulador**, porque es lo único que Room aporta de verdad y no se puede simular:

- `FavoritosDaoTest`, con base en memoria: que la segunda inserción con la misma firma devuelve
  `-1` y deja **una sola** fila; que `idPorFirma` la encuentra; que `observar()` emite en
  `id DESC`; que `borrar` de un id inexistente devuelve 0; que dos firmas distintas conviven.
- `RoomFavoritosLocalDataSourceTest`, con el fichero real y **un único `@Test`** (guardar → observar
  → obtener → guardar duplicado → borrar), para no tener dos instancias de `RoomDatabase` sobre el
  mismo fichero en el proceso — que es la razón por la que en producción es un `single`.

**Lo que no se prueba**: el data source como pegamento sobre el SDK, igual que
`SharedPreferencesCotizacionesLocalDataSource` y `DataStoreAjustesLocalDataSource`. Y una verificación
manual que ningún test cubre: guardar y reabrir un favorito en un **`assembleRelease`** firmado a
mano, para descartar que R8 rompa el discriminador (regla 4).
