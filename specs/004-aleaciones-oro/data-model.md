# Data Model: Calculadora de aleaciones de oro

**Feature**: `004-aleaciones-oro` · **Fecha**: 2026-08-23
**Fuente de verdad numérica**: `UI_Plantillas/Feature_Oro/ESPECIFICACION_CALCULADORA_ALEACIONES_ORO.md` (§ citados)

Todo el modelo es Kotlin puro (capa `domain/`), sin Android ni Firebase. Las cantidades
son `BigDecimal` construidos desde literales `String` para no heredar error binario.

## Entidades

### ColorOro *(enum)*

Los cuatro colores de oro que ofrece la calculadora.

| Valor | analyticsId |
|---|---|
| AMARILLO | `amarillo` |
| BLANCO | `blanco` |
| ROSA | `rosa` |
| ROJO | `rojo` |

### LeyOro *(enum)*

Las cuatro leyes objetivo. `finura` es la fracción decimal exacta; `esSoloTecnica` marca
la advertencia obligatoria de 500‰ (§2).

| Valor | Milésimas | Finura | esSoloTecnica | analyticsId |
|---|---:|---:|---|---|
| LEY_18K | 750 | 0.750 | no | `18k` |
| LEY_14K | 585 | 0.585 | no | `14k` |
| LEY_12K | 500 | 0.500 | **sí** | `12k` |
| LEY_9K | 375 | 0.375 | no | `9k` |

### MetalLiga *(enum)*

Los metales que pueden componer la liga.

| Valor | analyticsId |
|---|---|
| PLATA_FINA | `plata` |
| COBRE | `cobre` |
| PALADIO | `paladio` |

### RecetaLiga

Proporciones internas de la liga para una combinación color×ley (§6).

- **color**: `ColorOro`
- **ley**: `LeyOro`
- **proporciones**: mapa `MetalLiga → BigDecimal`, solo con los metales que la receta usa

**Invariantes** (validados al construir):
- Cada proporción es > 0.
- La suma de proporciones es 1 con tolerancia computacional 1e-9 (los literales del
  documento no siempre suman 1 exacto: blanco 750 suma 0,9999999999999999).

### RecetasOro *(objeto, única fuente de verdad — §15)*

Las 16 recetas, transcritas literalmente del documento (§7.1–§7.4). Prohibido
normalizarlas o sustituirlas (§24, §26). `recipes_version: 1.0`.

| Color | Ley | Proporciones internas de la liga (literales exactos) |
|---|---:|---|
| Amarillo | 750 | Ag `0.6600660066006601` · Cu `0.3399339933993399` |
| Amarillo | 585 | Ag `0.7194244604316547` · Cu `0.2805755395683453` |
| Amarillo | 500 | Ag `0.6997596153846154` · Cu `0.3002403846153846` |
| Amarillo | 375 | Ag `0.68` · Cu `0.32` |
| Blanco | 750 | Pd `0.4425442544254425` · Ag `0.3954395439543954` · Cu `0.1620162016201620` |
| Blanco | 585 | Ag `0.7721822541966426` · Pd `0.2278177458033573` |
| Blanco | 500 | Ag `0.8858173076923077` · Pd `0.1141826923076923` |
| Blanco | 375 | Ag `1.0` |
| Rojo | 750/585/500/375 | Cu `1.0` |
| Rosa | 750 | Ag `0.112` · Cu `0.888` |
| Rosa | 585 | Ag `0.2206235011990408` · Cu `0.7793764988009592` |
| Rosa | 500 | Ag `0.2701923076923077` · Cu `0.7298076923076923` |
| Rosa | 375 | Ag `0.32` · Cu `0.68` |

Las de blanco son sin níquel (§7.2). El blanco 585 y 500 no llevan cobre; el blanco 375
es solo plata; el rojo es siempre solo cobre.

### CalculoAleacion *(resultado del motor)*

- **masaOrigen**: gramos de oro 999‰ de partida
- **oroPuro**: `masaOrigen × 0.999` (§4.1)
- **ligaTotal**: `masaFinal − masaOrigen` (§4.3)
- **metales**: mapa `MetalLiga → BigDecimal`, `ligaTotal × proporción` por metal (§6)
- **masaFinal**: `oroPuro ÷ finuraObjetivo` (§4.2)
- **leyTeorica**: `oroPuro ÷ (masaOrigen + Σ metales)` (§9)

**Constantes**: `FINURA_ORIGEN = 0.999` (§3.1). La regla clásica «100 g + 33,333 g» está
explícitamente prohibida; con Au999 la liga de 750‰ es 33,200 g por cada 100 g (§3.1).

**Invariantes** (verificación §12, con tolerancia computacional):
- `Σ metales == ligaTotal`
- `masaFinal == masaOrigen + ligaTotal`
- `leyTeorica >= finuraObjetivo` — **nunca** por debajo (sin tolerancia en menos, Ley 17/1985)

**Reglas de precisión** (§10, §21):
- Ningún redondeo intermedio; multiplicaciones exactas.
- Divisiones con escala fija 15 y redondeo a la baja (`DOWN`), lo que garantiza el
  invariante de ley (la liga resultante nunca se pasa por exceso).
- El redondeo a 3 decimales (`HALF_UP`, como los ejemplos de §17) es exclusivo de la
  capa de presentación y jamás realimenta el cálculo.

## Operaciones del motor

### Cálculo directo (§9) — con UI

Entrada: `masaOrigen > 0`, `ColorOro`, `LeyOro` → `CalculoAleacion`.

Validaciones (§16): masa > 0 y finita; la finura objetivo es siempre < 0.999 por
construcción del enum. Entradas de texto se normalizan antes (coma → punto) en la capa
de presentación.

### Cálculo inverso (§14) — sin UI en esta versión

Entrada: `masaFinalDeseada > 0`, `ColorOro`, `LeyOro` → `CalculoAleacion` donde
`masaOrigen = masaFinalDeseada × finuraObjetivo ÷ 0.999` y
`ligaTotal = masaFinalDeseada − masaOrigen`, repartida con la misma receta.

## Estado de la pantalla (capa ui)

`OroUiState` — inmutable, expuesto por un único `StateFlow`:

- **cantidadTexto**: texto tal cual lo escribió el usuario (con coma o punto)
- **ley**: `LeyOro` seleccionada (inicial: LEY_18K)
- **color**: `ColorOro` seleccionado (inicial: AMARILLO)
- **resultado**: presente solo con entrada válida; lleva las cifras ya formateadas
  (3 decimales, coma) por metal y el total, en el orden Plata fina → Cobre → Paladio
- **avisoLeyTecnica**: derivado de `ley.esSoloTecnica`

**Transiciones**:
- Cambio de cantidad/ley/color → recálculo inmediato; si la entrada no es válida
  (vacía, no numérica, ≤ 0) → `resultado` ausente, sin error visible.
- «Limpiar» → estado inicial completo.
- Salir y volver a entrar → estado inicial (sin memoria).
- «Guardar en favoritos» → no muta el estado; solo telemetría + aviso efímero.

## Casos de contraste (§13 — criterios de aceptación)

| Entrada | Esperado (exacto) |
|---|---|
| 10 g · amarillo · 750 | liga 3.320000 · Ag 2.191419142 · Cu 1.128580858 · final 13.320000 |
| 10 g · blanco · 750 | liga 3.320000 · Pd 1.469246925 · Ag 1.312859286 · Cu 0.537893789 |
| 17.35 g · rosa · 750 | liga 5.760200 · Ag 0.645142400 · Cu 5.115057600 · final 23.110200 |
| 25 g · amarillo · 585 | liga 17.692307692 · Ag 12.728278915 · Cu 4.964028777 |
| 5 g · rojo · 375 | liga 8.320000 · Cu 8.320000 · final 13.320000 |
| Inverso: 20 g finales · amarillo · 750 | Au999 15.015015015 · liga 4.984984985 |
