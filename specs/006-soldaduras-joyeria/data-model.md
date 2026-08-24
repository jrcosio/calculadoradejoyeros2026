# Data Model: Calculadora de soldaduras de joyería

**Feature**: `006-soldaduras-joyeria` · **Fecha**: 2026-08-24
**Fuente de verdad numérica**: `UI_Plantillas/Feature_Soldadura/ESPECIFICACION_CALCULADORA_SOLDADURAS_JOYERIA.md` (§ citados)

Todo el modelo es Kotlin puro: sin `android.*`, sin `androidx.*`, sin `R`. Las cantidades
son `BigDecimal` y las constantes decimales se construyen desde literales `String`, nunca
desde `Double` (§2.1). Es el **tercer motor** del proyecto y no depende de ningún tipo de
los otros dos: `ColorOro` tiene ROJO (§5.1 solo admite tres colores) y `MetalLiga` es
vocabulario del documento de oro. La duplicación de constantes de precisión es deliberada
y va comentada, como en la 005.

A diferencia de oro y plata, aquí conviven **dos formas de cálculo**: recetas escalables
(clásicas y base, §2.2) y factores de mezcla (plata §4.1 y ley §5.4). Por eso hay dos
tipos de resultado.

## Entidades

### MetalSoldadura *(enum)*

| Valor | analyticsId | Interviene en |
|---|---|---|
| `ORO_24K` | `"oro_24k"` | clásica muy floja de ley, base |
| `ORO_18K` | `"oro_18k"` | clásicas floja y fuerte, mezcla de ley |
| `PLATA_FINA` | `"plata_fina"` | clásicas, base, plata |
| `LATON` | `"laton"` | clásicas, plata |
| `COBRE` | `"cobre"` | clásica fuerte, base |
| `ZINC` | `"zinc"` | base |
| `CADMIO` | `"cadmio"` | clásica muy floja de ley, base |

- El **orden del enum no es el orden de pintado**: cada receta fija el suyo (las tablas de
  §3.2–§3.4 y §5.2 ordenan distinto). El orden viaja en `RecetaSoldadura`.
- El color del oro 18 K no vive aquí: es un atributo del cálculo de ley
  (`CalculoSoldaduraLey.color`), porque no cambia el metal ni su peso (TEST 9).

### ColorOroSoldadura *(enum, §5.1)*

| Valor | analyticsId |
|---|---|
| `AMARILLO` | `"amarillo"` |
| `BLANCO` | `"blanco"` |
| `ROSA` | `"rosa"` |

Enum propio de tres valores: reutilizar `ColorOro` permitiría ROJO, que §5.1 no admite, y
§8.1 exige poder exigir color solo en el método de ley (aquí, por parámetro no nulo).

### TipoSoldaduraClasica *(enum, §3.1)*

| Valor | analyticsId | llevaCadmio | Oro de entrada (modo directo) |
|---|---|---|---|
| `FLOJA` | `"floja"` | `false` | `ORO_18K` (5 g patrón) |
| `FUERTE` | `"fuerte"` | `false` | `ORO_18K` (5 g patrón) |
| `MUY_FLOJA_LEY` | `"muy_floja_ley"` | `true` | `ORO_24K` (1 g patrón) |

`llevaCadmio` es propiedad derivada (la receta contiene `CADMIO`), patrón
`LeyPlata.esSoloTecnica`: la pantalla decide la advertencia §9 sin campo de UI.

### TipoSoldaduraPlata *(enum, §4.1)*

| Valor | analyticsId | Factor `p` (latón / plata fina) |
|---|---|---:|
| `MUY_FLOJA` | `"muy_floja"` | 0,75 |
| `FLOJA` | `"floja"` | 0,50 |
| `NORMAL` | `"normal"` | 0,40 |
| `FUERTE` | `"fuerte"` | 0,30 |

El factor **no** vive en el enum sino en `RecetasSoldadura.factorLaton(tipo)`, para que
§7 tenga una única transcripción. §4.1 es interpretación obligatoria: `p` es latón
respecto a la **plata**, no sobre el peso final.

### DurezaSoldaduraLey *(enum, §5.4)*

| Valor | analyticsId | Factor `r` (oro 18 K / base) |
|---|---|---:|
| `MUY_FLOJA` | `"muy_floja"` | 0,3 |
| `FLOJA` | `"floja"` | 0,5 |
| `MEDIA` | `"media"` | 1 |
| `FUERTE` | `"fuerte"` | 2 |
| `MUY_FUERTE` | `"muy_fuerte"` | 3 |

El orden del enum es el orden del selector (r creciente). Más base = más floja; más oro =
más fuerte (§5.5). El factor vive en `RecetasSoldadura.factorOro(dureza)`.

### RecetaSoldadura

- **componentes**: `List<ComponenteReceta>` — cada uno con `metal: MetalSoldadura` y
  `pesoPatron: BigDecimal`. **Lista, no mapa**: el orden es el de la tabla del documento y
  es el orden estable de presentación (§8.2).
- **totalPatron**: suma exacta de los pesos patrón (8; 6,50; 1,44; 13,26).
- **Invariantes de construcción** (`init`): no vacía, todos los pesos > 0, sin metales
  repetidos.

### RecetasSoldadura *(objeto, única fuente de verdad de §7)*

| Miembro | Contenido (literales `String`) |
|---|---|
| `clasica(FLOJA)` | oro 18K «5», plata fina «2», latón «1» — total 8 (§3.2) |
| `clasica(FUERTE)` | oro 18K «5», plata fina «0.50», cobre «0.50», latón «0.50» — total 6,50 (§3.3) |
| `clasica(MUY_FLOJA_LEY)` | oro 24K «1», plata fina «0.10», latón «0.16», cadmio «0.18» — total 1,44 (§3.4) |
| `BASE` | oro 24K «10», **cobre «0.54», plata fina «0.80», zinc «0.92», cadmio «1.00»** — total 13,26 (§5.2) |
| `factorLaton(tipo)` | «0.75» / «0.50» / «0.40» / «0.30» (§4.1) |
| `factorOro(dureza)` | «0.3» / «0.5» / «1» / «2» / «3» (§5.4) |
| `VERSION_RECETAS` | `"1.0"` (patrón `RecetasOro`) |

Los valores de `BASE` son los de §5.2/§7. El mockup los muestra intercambiados
(plata 0,54 / cobre 0,80 / cadmio 0,92 / zinc 1,00) y §12 da prevalencia al documento:
**prohibido «corregirlos» mirando el PNG**. TEST 6 lo blinda.

### CalculoSoldadura *(resultado escalado: clásicas, plata y base)*

- **componentes**: `List<ComponenteCalculado>` (`metal`, `gramos: BigDecimal`), en el
  orden de la receta.
- **total**: peso final teórico. En los cálculos por receta, suma exacta de los
  componentes; en plata, `plata + latón` exacto.

**Constantes** (propias, no importadas de los otros motores):
- `ESCALA = 15` para la única división de cada cálculo. §2.1 pide aritmética decimal;
  15 decimales dejan el residuo 12 órdenes por debajo de la balanza de 0,001 g.
- `TOLERANCIA = 1E-9`, tolerancia puramente computacional de las verificaciones.

**Invariantes** (`check`, red de seguridad — propiedades de §10):
- Todos los `gramos > 0`.
- `total` coincide con la suma de componentes (dentro de `TOLERANCIA`).
- En los inversos, `|total − pesoDeseado| < TOLERANCIA`.

### CalculoSoldaduraLey *(resultado de la mezcla base + oro 18 K, §5.4)*

Tipo propio, no un `CalculoSoldadura`: la **base no es un `MetalSoldadura`** (es un
preparado de la otra pantalla) y el color debe viajar en el resultado (TEST 9).

- **base**: gramos de soldadura base.
- **oro18K**: gramos de oro 18 K del color elegido.
- **color**: `ColorOroSoldadura` — no altera cantidades.
- **dureza**: `DurezaSoldaduraLey`.
- **total**: `base + oro18K` exacto.

**Invariantes**: `base > 0`, `oro18K > 0`, `total = base + oro18K`.

## Fórmulas por caso de uso (una división por cálculo, escala 15, `HALF_UP`)

| Caso de uso | Entrada | Fórmula | División |
|---|---|---|---|
| `CalcularSoldaduraClasicaUseCase` | oro disponible `G`, tipo | `factor = G / pesoPatronOro`; resto = patrón × factor | `G / {5; 5; 1}` |
| `CalcularSoldaduraClasicaInversaUseCase` | peso final `T`, tipo | `factor = T / totalPatron` (§2.2) | `T / {8; 6,50; 1,44}` |
| `CalcularSoldaduraPlataUseCase` | plata `S`, tipo | `laton = S × p`; `total = S × (1+p)` (§4.2) | — (exacto) |
| `CalcularSoldaduraPlataInversaUseCase` | peso final `T`, tipo | `plata = T / (1+p)`; `laton = T − plata` (§4.3) | `T / (1+p)` |
| `CalcularSoldaduraBaseUseCase` | oro 24K `G` | `factor = G / 10` (§5.2) | `G / 10` |
| `CalcularSoldaduraBaseInversaUseCase` | peso de base `B` | `factor = B / 13,26` (§5.2) | `B / 13,26` |
| `CalcularSoldaduraLeyDesdeOroUseCase` | oro 18K `G`, dureza, color | `base = G / r`; `total = G + base` | `G / r` |
| `CalcularSoldaduraLeyUseCase` | base `B`, dureza, color | `oro = B × r`; `total = B × (1+r)` (§5.4) | — (exacto) |
| `CalcularSoldaduraLeyInversaUseCase` | peso final `T`, dureza, color | `base = T / (1+r)`; `oro = T − base` | `T / (1+r)` |

- **Validación común** (§8.1, TEST 10): `require(entrada > 0)` con mensaje. El parseo de
  texto (vacío, no numérico, coma/punto) es responsabilidad del ViewModel, nunca del motor.
- **Redondeo**: `HALF_UP` en la única división; multiplicaciones y restas exactas, sin
  redondeos intermedios (§8.1). Aquí no hay ley de contraste que proteger, a diferencia
  del DOWN/UP direccional del motor de plata.
- Las clásicas **no aceptan color por diseño de tipos** (§8.1); los tres casos de uso de
  ley lo **exigen** por parámetro no nulo.
- `CalcularSoldaduraLeyUseCase` (desde la base) existe y se prueba **sin UI en v1**
  (precedente 005; ver Complexity Tracking del plan).

## Estado de UI (fuera del motor, paquete `ui/soldaduras/`)

Conceptos de presentación, como `HomeModule`: ningún caso de uso los recibe.

- **FamiliaSoldadura**: `ORO_LEY`, `CLASICA`, `PLATA` (+`analyticsId`). En
  `SoldadurasUiState` es **nullable**: `null` = primera visita, solo el selector.
- **ModoEntradaSoldadura**: `DESDE_METAL` (defecto), `PESO_FINAL` (+`analyticsId`).
- **IngredienteSoldadura**: los siete metales **más `BASE`** — existe porque la base no es
  un `MetalSoldadura` y la fila de oro 18 K necesita el color para su etiqueta. Cada
  pantalla lo mapea a imagen y string en `PresentacionSoldadura.kt`.
- **SoldadurasUiState**: `familia?`, `modo`, `cantidadTexto`, `colorOro` (AMARILLO),
  `dureza` (MUY_FLOJA), `tipoClasica` (FLOJA), `tipoPlata` (MUY_FLOJA),
  `resultado: ResultadoSoldaduras?` — filas ya formateadas y ordenadas por el ViewModel +
  `totalFormateado`.
- **SoldaduraBaseUiState**: `modo`, `cantidadTexto`, `resultado?` (filas + total).
- **Formateo** (solo ViewModel): `setScale(3, HALF_UP)`, coma decimal, 3 decimales fijos.
  Lo mostrado nunca realimenta el cálculo.

## Transiciones de estado (contratos del ViewModel)

| Evento | Efecto |
|---|---|
| Elegir familia | Estado limpio con la nueva familia (cantidad vacía, modo directo, selecciones por defecto) |
| Cambiar modo | Vacía `cantidadTexto` y `resultado`; conserva familia, tipo, dureza y color |
| Cambiar tipo/dureza/color | Recalcula con la cantidad vigente |
| Entrada inválida | `resultado = null`; rearma la deduplicación de telemetría |
| Limpiar | Estado inicial **conservando la familia**; rearma la deduplicación |
| Guardar favoritos | Solo telemetría; el aviso efímero lo pone la vista |
