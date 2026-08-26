# Data Model: Favoritos

**Feature**: `009-favoritos` | **Fecha**: 2026-08-26 | **Spec**: [spec.md](./spec.md)

Cuatro conceptos de dominio (`Favorito`, `EntradasFavorito`, `ResumenFavorito`,
`ResultadoGuardado`), una tabla y un enum que se muda de capa. Todo lo de `domain/` es Kotlin puro:
no importa `android.*`, `androidx.*` ni `data.*`.

---

## 1. `Favorito` — el agregado

`domain/model/Favorito.kt`

| Campo | Tipo | Origen | Notas |
|---|---|---|---|
| `id` | `Long` | la base | Clave primaria autogenerada. Monótona: es también el orden real de inserción |
| `guardadoEnEpochMillis` | `Long` | `Reloj` | Se sella en el repositorio, no lo aporta la pantalla |
| `entradas` | `EntradasFavorito` | el joyero | Lo único que se persiste del cálculo |

No lleva resultado (R3) ni título (FR-008 y FR-014: lo compone la vista con sus recursos).

`id` y hora **fuera** de `EntradasFavorito` a propósito: al guardar no existen todavía, así que
`guardar(entradas)` no tiene que inventarse un `id = 0` ni una fecha falsa.

---

## 2. `EntradasFavorito` — la identidad

`sealed interface` con **siete** variantes. Los campos de cada una son **exactamente** los
parámetros de su motor, en el mismo orden, para que el `when` de `ResumirFavoritoUseCase` no pueda
equivocarse de argumento.

| Variante | Campos | Motor(es) |
|---|---|---|
| `Oro` | `masaOrigen: BigDecimal`, `color: ColorOro`, `ley: LeyOro` | `CalcularAleacionOroUseCase` |
| `Plata` | `masaOrigen: BigDecimal`, `ley: LeyPlata` | `CalcularAleacionPlataUseCase` |
| `SoldaduraLey` | `cantidad: BigDecimal`, `dureza: DurezaSoldaduraLey`, `color: ColorOroSoldadura`, `modo: ModoEntradaSoldadura` | `CalcularSoldaduraLeyDesdeOroUseCase` / `…LeyInversaUseCase` |
| `SoldaduraClasica` | `cantidad: BigDecimal`, `tipo: TipoSoldaduraClasica`, `modo: ModoEntradaSoldadura` | `CalcularSoldaduraClasicaUseCase` / `…InversaUseCase` |
| `SoldaduraPlata` | `cantidad: BigDecimal`, `tipo: TipoSoldaduraPlata`, `modo: ModoEntradaSoldadura` | `CalcularSoldaduraPlataUseCase` / `…InversaUseCase` |
| `SoldaduraBase` | `cantidad: BigDecimal`, `modo: ModoEntradaSoldadura` | `CalcularSoldaduraBaseUseCase` / `…InversaUseCase` |
| `Chapa` | `ancho: BigDecimal`, `largo: BigDecimal`, `espesor: BigDecimal`, `material: MaterialChapa` | `CalcularPesoChapaUseCase` |

Cada variante expone `analyticsId` (`"oro"`, `"plata"`, `"soldadura_ley"`, `"soldadura_clasica"`,
`"soldadura_plata"`, `"soldadura_base"`, `"chapa"`), que es también el discriminador `tipo` de la
tabla y el valor de telemetría (FR-036: sólo el tipo, nunca la cantidad).

### Por qué siete variantes y no una con el tipo dentro

Porque `CalcularSoldaduraClasicaUseCase` **no acepta color**, y su KDoc lo razona: «§8.1 prohíbe
elegir color en las recetas clásicas… la prohibición va en el diseño de tipos, no en una
validación». Una variante única obligaría a un `color` nulable y a un `!!` o un `require` al
resumir, reintroduciendo justo lo que ese diseño impide. Con siete variantes el `when` es
exhaustivo, cada rama tiene los campos justos y el discriminador es 1:1 con la variante.

### Reglas de validación

- `init { require(cantidad > BigDecimal.ZERO) }` en cada variante (las tres medidas en `Chapa`). Un
  favorito con cantidad ≤ 0 es **inconstruible**, y eso es lo que garantiza que
  `ResumirFavoritoUseCase` nunca dispare el `require` de un motor.
- El límite operativo de las chapas (10 000 mm de ancho y largo, 1 000 mm de espesor) **no** se
  repite aquí: es control de interfaz y vive en `PesoChapasViewModel`, igual que hoy. Un favorito
  sólo puede nacer de un formulario que ya lo respetó.
- Las cantidades llegan **ya normalizadas**: `parsearDecimalPositivo` (`core/util/Decimales.kt`)
  convierte la coma en punto y rechaza lo inválido antes de que se construya la variante.

### Trampa de `equals`, documentada en el KDoc

`BigDecimal.equals` compara también la escala, así que `Oro(BigDecimal("30"), …)` y
`Oro(BigDecimal("30.0"), …)` **no** son iguales aunque sean el mismo favorito. La identidad la
define la firma canónica (§5), nunca `equals`: prohibido `distinctBy { it.entradas }`, `contains` o
`indexOf` sobre entradas.

---

## 3. `ModoEntradaSoldadura` — el enum que cambia de capa

Hoy vive en `ui/soldaduras/SoldadurasUiState.kt` con el KDoc «Concepto de UI, como `HomeModule`:
ningún caso de uso lo recibe». **Se muda a `domain/model/`** porque a partir de esta feature
`ResumirFavoritoUseCase` sí lo recibe: es la misma regla del segundo consumidor que promovió
`parsearDecimalPositivo` a `core/util/`. Además, directo/inverso es §2.3 de su documento técnico,
no una decisión de pantalla. Cambio mecánico de `import` en los ficheros de `ui/soldaduras/` y sus
tests; el enum no cambia (conserva sus dos valores y su `analyticsId`).

**No se mudan** `FamiliaSoldadura`, `IngredienteSoldadura` ni `MedidaChapa`: la familia va implícita
en la variante, y las medidas de chapa viajan como tres campos con nombre propio. Siguen siendo
conceptos de UI y ningún caso de uso los recibe.

Alternativa descartada: crear un `ModoCalculoSoldadura` en dominio y mapearlo en
`PresentacionSoldadura.kt`, como se hizo con `ColorOro`/`ColorOroSoldadura`. Allí los conjuntos de
valores **difieren** (ROJO); aquí serían dos enums idénticos, o sea duplicación sin motivo.

---

## 4. `ResumenFavorito` — las cifras derivadas

`sealed interface`, **una variante por motor**, con `BigDecimal` **sin formatear**: el redondeo de
vista es exclusivo del ViewModel y no es el mismo en las cinco calculadoras.

| Variante | Campos | Se construye de |
|---|---|---|
| `Oro` | `metales: Map<MetalLiga, BigDecimal>`, `masaFinal` | `CalculoAleacion.metales`, `.masaFinal` |
| `Plata` | `cobre`, `masaFinal` | `CalculoPlata.cobre`, `.masaFinal` |
| `SoldaduraLey` | `base`, `oro18K: BigDecimal?`, `total` | `CalculoSoldaduraLey` |
| `Soldadura` | `componentes: List<ComponenteCalculado>`, `total` | `CalculoSoldadura` |
| `Chapa` | `peso`, `volumenCm3`, `metalFino` | `CalculoChapa` |

**Cero tipos y cero enums nuevos en dominio**: reutiliza `MetalLiga` y `ComponenteCalculado`. Un
enum unificado de ingredientes sería el **cuarto** enum paralelo del proyecto (`MetalLiga`,
`MetalSoldadura`, `IngredienteSoldadura` y el nuevo), y el aplanado que la tarjeta necesita se hace
en `ui/favoritos/` con un enum de presentación, que es donde el proyecto pone los mapeos.

`Plata` lleva un campo `cobre` y no un mapa porque el cobre es el único metal de liga de la plata
(§2, §33 de su documento) y no hay `RecetasPlata` que tabular. `SoldaduraLey.oro18K` es nulable
porque en modo directo el oro es lo que el joyero ya tiene y no se pinta como fila.

### Reglas de resumen que `ResumirFavoritoUseCase` hereda de los ViewModels

Transcritas de `SoldadurasViewModel` (líneas 130, 136-137, 162, 193) y `SoldaduraBaseViewModel`
(líneas 78-79, 85). En modo directo la fila del metal de partida **no** se pinta:

| Variante | Modo | Motor | Filas |
|---|---|---|---|
| `Oro` | — | `calcularOro(masaOrigen, color, ley)` | `metales` + `masaFinal` |
| `Plata` | — | `calcularPlata(masaOrigen, ley)` | `cobre` + `masaFinal` |
| `SoldaduraLey` | `DESDE_METAL` | `calcularLeyDesdeOro` | `base`; `oro18K = null` |
| `SoldaduraLey` | `PESO_FINAL` | `calcularLeyInversa` | `base` y `oro18K` |
| `SoldaduraClasica` | `DESDE_METAL` | `calcularClasica` | `componentes.drop(1)` |
| `SoldaduraClasica` | `PESO_FINAL` | `calcularClasicaInversa` | `componentes` |
| `SoldaduraPlata` | `DESDE_METAL` | `calcularSoldaduraPlata` | `componentes.drop(1)` |
| `SoldaduraPlata` | `PESO_FINAL` | `calcularSoldaduraPlataInversa` | `componentes` |
| `SoldaduraBase` | `DESDE_METAL` | `calcularBase` | `componentes.filter { it.metal != ORO_24K }` |
| `SoldaduraBase` | `PESO_FINAL` | `calcularBaseInversa` | `componentes` |
| `Chapa` | — | `calcularChapa(ancho, largo, espesor, material)` | `peso`, `volumenCm3`, `metalFino` |

Queda una **segunda transcripción** de esa regla (la primera está en los ViewModels). Los tests de
`ResumirFavoritoUseCase` usan los mismos vectores que los tests de los motores, y merece una línea
en `CLAUDE.md`.

---

## 5. `ResultadoGuardado` — qué pasó al guardar

`sealed interface` con `id: Long`: `Guardado(id)` y `YaExistia(id)`.

El duplicado **no es una excepción**: es el resultado normal de pedir dos veces lo mismo, así que
viaja como valor (FR-006). En los dos casos hay un `id`, que es lo que la pantalla necesitaría para
señalar el favorito.

---

## 6. La tabla

Una sola: `favoritos`.

| Columna | Tipo | Notas |
|---|---|---|
| `id` | `INTEGER` PK autogenerada | Monótona. Es también el criterio de orden (FR-012) |
| `tipo` | `TEXT` | El `analyticsId` de la variante. Discriminador al decodificar |
| `firma` | `TEXT` | **Índice único.** La identidad del favorito (FR-006, FR-007) |
| `datosJson` | `TEXT` | Las entradas serializadas, con su propio número de versión |
| `guardadoEnEpochMillis` | `INTEGER` | Sólo para mostrar la fecha (FR-013) |

**Tabla única con las entradas en JSON, y no una columna por entrada**: las siete variantes no
comparten campos, y con JSON el esquema de SQLite **no se toca** cuando una calculadora gane una
entrada nueva — no habría migración, sólo una firma nueva. Con columnas tipadas, cada entrada nueva
sería una migración.

**`ORDER BY id DESC`, no por fecha**: el id autoincremental es el orden real de inserción y
sobrevive a que el joyero cambie la hora del móvil o a dos guardados en el mismo milisegundo
(FR-012). La fecha se guarda para mostrarla, no para ordenar.

**Sin índice sobre `guardadoEnEpochMillis`**: no se consulta por fecha, y un joyero tendrá decenas
de favoritos.

El detalle de la firma, del JSON y de la tolerancia está en
[contracts/favoritos-persistidos.md](./contracts/favoritos-persistidos.md).

---

## 7. Estado de presentación

`ui/favoritos/FavoritosUiState.kt`. Cifras ya formateadas, lo traducible como enum y las fechas
como `Long`, según el contrato de ViewModel del proyecto.

- `FavoritosUiState(cargando: Boolean = true, favoritos: List<FavoritoUiModel>, pendienteDeBorrar: FavoritoUiModel?)`.
  `cargando` cumple FR-018: sin él la tarjeta de «Aún no hay favoritos» parpadea en cada visita,
  porque la primera emisión del flujo llega un fotograma después de componer. Es el mismo criterio
  por el que `MainActivity` no compone el `NavHost` hasta saber el idioma.
- `pendienteDeBorrar` vive en el `UiState` y no en un `rememberSaveable` porque los tests
  instrumentados del proyecto montan `XContent(uiState, …)` sin ViewModel. El ViewModel guarda el
  id aparte y **re-deriva** el campo en cada emisión, descartándolo si ese favorito ya no está: así
  una emisión concurrente no deja un diálogo colgado sobre algo borrado.
- `FavoritoUiModel(id, entradas: EntradasFavoritoUi, lineas: List<LineaFavoritoUi>, totalFormateado, guardadoEnEpochMillis)`.
  La lista de líneas viaja **completa**; el recorte a tres (FR-015) es constante de layout y vive en
  la tarjeta, hermana del `158.dp` de `ModuleCard`.
- **`TipoFavorito`** es un enum de esta capa con **cinco** valores —`ORO`, `PLATA`, `SOLDADURA`,
  `SOLDADURA_BASE`, `CHAPA`—, y es la **sección**: le da a la tarjeta su imagen, su nombre y su
  acento, y decide a qué ruta lleva al pulsarla. Las **siete** variantes de `EntradasFavorito`
  colapsan a estos cinco porque las tres familias de soldadura comparten pantalla y sección
  (`SoldaduraLey`, `SoldaduraClasica` y `SoldaduraPlata` → `SOLDADURA`), mientras que la BASE
  tiene ruta propia y por eso es sección propia. Esa asimetría 7→5 es deliberada: el dominio
  distingue por motor y la interfaz por pantalla.
- El **tipo se deriva** de las entradas, no se duplica: la misma regla que ya documenta
  `PesoChapasUiState` («la familia no se duplica: es `material.familia`»). Ojo con no confundirlo
  con `EntradasFavorito.analyticsId`, que tiene siete valores y es el discriminador de la **tabla**.
- `ConceptoFavorito` es un enum **de esta capa**: aplana `MetalLiga`, `ComponenteCalculado` y la
  base en una sola lista para la tarjeta, y `PresentacionFavoritos.kt` lo mapea a `R.string.metal_*`,
  todas existentes.

### Transiciones de estado

```
cargando ──primera emisión──> lista (con favoritos)  ──se quita el último──> lista vacía
                           └─> lista vacía (invitación)

lista ──pulsar estrella──> confirmación pendiente ──confirmar──> lista sin ese favorito
                                                 └─cancelar──> lista (sin cambios)
```

El borrado cierra el diálogo **de inmediato** y no espera a la corrutina: si esperara, un
almacenamiento lento dejaría el diálogo clavado. La fila desaparece cuando el flujo reemite.
