# Data Model: Calculadora de aleaciones de plata

**Feature**: `005-aleaciones-plata` · **Fecha**: 2026-08-24
**Fuente de verdad numérica**: `UI_Plantillas/Feature_plata/ESPECIFICACION_CALCULADORA_ALEACIONES_PLATA.md` (§ citados)

Todo el modelo es Kotlin puro: sin `android.*`, sin `androidx.*`, sin `R`. Las cantidades
son `BigDecimal` y las constantes decimales se construyen desde literales `String`, nunca
desde `Double`, para no heredar el error de la coma flotante binaria (§15).

A diferencia del motor de oro, **aquí no hay tabla de recetas**. §28 prohíbe expresamente
tabular coeficientes por ley y exige la fórmula general; con un único metal de liga no hay
proporciones que repartir. Los coeficientes de §6 son valores esperados de los tests, no
datos del motor.

## Entidades

### LeyPlata *(enum)*

| Valor | Milésimas | Finura | esSoloTecnica | analyticsId | Situación legal (§3, §31) |
|---|---:|---:|---|---|---|
| `LEY_950` | 950 | 0.950 | `true` | `"950"` | Preset técnico: no es punzón oficial español |
| `LEY_925` | 925 | 0.925 | `false` | `"925"` | Ley oficial española (Ley 17/1985) |
| `LEY_900` | 900 | 0.900 | `true` | `"900"` | Preset técnico: no es punzón oficial español |
| `LEY_800` | 800 | 0.800 | `false` | `"800"` | Ley oficial española (Ley 17/1985) |

- **finura**: derivada de las milésimas con escala 3 exacta (`BigDecimal(milesimas).movePointLeft(3)`).
- **esSoloTecnica**: mismo nombre de bandera que en `LeyOro`, para que la pantalla decida
  la advertencia igual que en oro. Aquí la llevan **dos** de las cuatro leyes, no una.
- **analyticsId**: las milésimas en texto. Estable e independiente del idioma.
- El orden del enum es el orden de pintado del selector: de mayor a menor finura, como el
  mockup.
- Las cuatro finuras son estrictamente menores que `FINURA_ORIGEN`, así que el cobre a
  añadir es siempre positivo por construcción (§9 pide validar
  `sourceFineness > targetFineness`; el enum lo garantiza y el motor lo verifica).

### Cobre de liga

No es una entidad del dominio: es una constante del problema. §2 y §33 fijan el cobre como
**único** metal de liga de esta calculadora y prohíben añadir zinc, germanio, estaño o
níquel. Por eso `CalculoPlata` expone un campo `cobre` y no un mapa de metales, y el nombre
y la imagen del cobre viven en la capa de presentación.

### CalculoPlata *(resultado del motor)*

- **masaOrigen**: gramos de plata fina 999‰ de partida
- **plataPura**: `masaOrigen × 0.999` (§5.1)
- **cobre**: `masaFinal − masaOrigen` (§5.3)
- **masaFinal**: `plataPura ÷ finuraObjetivo` (§5.2)
- **leyTeorica**: `plataPura ÷ masaFinal` (§9)

**Constantes** (propias, no importadas del motor de oro):
- `FINURA_ORIGEN = 0.999` (§4). Ag999 **nunca** se trata como Ag1000: 100 g de plata fina
  contienen 99,900 g de plata pura, no 100.
- `ESCALA = 15` para las divisiones internas. §15 pide aritmética decimal de alta
  precisión; 15 decimales dejan el error computacional muy por debajo de cualquier balanza.
- `TOLERANCIA = 1E-9`, tolerancia puramente computacional de las verificaciones.

**Invariantes** (verificación §20, red de seguridad, no lógica de negocio):
- `cobre > 0`
- `masaFinal == masaOrigen + cobre`
- `leyTeorica >= finuraObjetivo` — **nunca** por debajo (sin tolerancia en menos, Ley 17/1985)

**Reglas de precisión** (§14, §16, §21):
- Ningún redondeo intermedio; las multiplicaciones `BigDecimal` son exactas.
- Divisiones con escala fija 15 y redondeo **a favor de la ley**: `DOWN` en el modo directo
  (menos cobre → ley igual o superior), `UP` en el inverso (más plata fina → ley igual o
  superior).
- El redondeo a 3 decimales de la vista es **truncado** (`DOWN`), no a la media, y es
  exclusivo de la capa de presentación: jamás realimenta un cálculo. Truncar a 3 decimales
  equivale al «modo taller seguro» de §16-§17 con la resolución de balanza de 0,001 g que
  §18 recomienda por defecto, de modo que **pesar exactamente la cifra mostrada nunca deja
  la aleación por debajo de la ley objetivo**.

## Operaciones del motor

### Cálculo directo (§9) — con UI

Entrada: `masaOrigen > 0`, `LeyPlata` → `CalculoPlata`.

Validaciones (§26): masa > 0 y finita. La finura objetivo es siempre < 0.999 por
construcción del enum. Las entradas de texto se normalizan antes (coma → punto) en la capa
de presentación.

Forma compacta equivalente de §5.3, útil para leer los tests:
`cobre = masaOrigen × (0.999 ÷ finuraObjetivo − 1)`.

### Cálculo inverso (§22) — sin UI en esta versión

Entrada: `masaFinalDeseada > 0`, `LeyPlata` → `CalculoPlata` donde
`masaOrigen = masaFinalDeseada × finuraObjetivo ÷ 0.999` (escala 15, `UP`) y
`cobre = masaFinalDeseada − masaOrigen`.

## Estado de la pantalla (capa ui)

`PlataUiState`:
- **cantidadTexto**: lo que el joyero ha tecleado, tal cual, con coma o con punto. Inicial `""`.
- **ley**: `LeyPlata`. Inicial `LEY_925` — la plata Sterling, y la que el mockup muestra activa.
- **resultado**: `ResultadoPlata?`, presente solo con entrada válida. Ausente = no se pinta nada.

`ResultadoPlata` lleva las cifras **ya formateadas** (3 decimales truncados, coma decimal):
- **cobreFormateado**
- **totalFormateado**

La advertencia de ley técnica no tiene campo propio: se deriva de `LeyPlata.esSoloTecnica`.
El valor exacto vive solo en el motor y jamás se recalcula desde estas cadenas (§14, §21).

**Transiciones**:
- Teclear cantidad → recalcula; entrada inválida → `resultado = null` sin mensaje de error.
- Elegir ley → recalcula con la cantidad intacta.
- «Limpiar» → `PlataUiState()`, el estado inicial completo.
- «Guardar en favoritos» → no altera el estado; solo telemetría y aviso efímero.
- Entrar de nuevo en el módulo → estado inicial; no hay memoria entre visitas.

## Coeficientes de cobre por gramo de Ag999 (§6)

Valores esperados de los tests, **no** datos del motor (§28):

| Ley objetivo | Coeficiente de Cu |
|---|---:|
| 950‰ | 0,0515789473684210526 |
| 925‰ | 0,08 |
| 900‰ | 0,11 |
| 800‰ | 0,24875 |

## Casos de contraste (§21 — criterios de aceptación)

Con 10 g de plata fina 999‰:

| Ley | Plata pura | Cobre exacto | Masa final exacta | Cobre mostrado | Total mostrado |
|---|---:|---:|---:|---:|---:|
| 950‰ | 9,990 | 0,515789473684210 | 10,515789473684210 | **0,515** | **10,515** |
| 925‰ | 9,990 | 0,800000000000000 | 10,800000000000000 | **0,800** | **10,800** |
| 900‰ | 9,990 | 1,100000000000000 | 11,100000000000000 | **1,100** | **11,100** |
| 800‰ | 9,990 | 2,487500000000000 | 12,487500000000000 | **2,487** | **12,487** |

Tabla de taller de §7, con 100 g de plata fina:

| Ley | Cobre exacto | Masa final exacta | Cobre mostrado | Total mostrado |
|---|---:|---:|---:|---:|
| 950‰ | 5,157894736842105 | 105,157894736842105 | **5,157** | **105,157** |
| 925‰ | 8,000000000000000 | 108,000000000000000 | **8,000** | **108,000** |
| 900‰ | 11,000000000000000 | 111,000000000000000 | **11,000** | **111,000** |
| 800‰ | 24,875000000000000 | 124,875000000000000 | **24,875** | **124,875** |

Caso del mockup, 25 g hacia 925‰: cobre **2,000** g, total **27,000** g.

Modo inverso (§22-§23), para 100 g de aleación final:

| Ley | Plata fina necesaria | Cobre | Ley teórica |
|---|---:|---:|---:|
| 950‰ | 95,095095095095096 | 4,904904904904904 | 0,950000000000000 |
| 925‰ | 92,592592592592593 | 7,407407407407407 | 0,925000000000000 |
| 900‰ | 90,090090090090091 | 9,909909909909909 | 0,900000000000000 |
| 800‰ | 80,080080080080081 | 19,919919919919919 | 0,800000000000000 |

La fila de 925‰ es el ejemplo literal de §23. Nótese que el redondeo `UP` de la división
hace que la plata de partida acabe en `...593` y no en `...592`: una billonésima más de
plata fina, que es lo que aquí protege la ley.

## Ley práctica: la propiedad que exige §20

Pesar exactamente el cobre **mostrado** debe dar una ley igual o superior a la objetivo:
`plataPura ÷ (masaOrigen + cobreMostrado) >= finuraObjetivo`.

Comparado con lo que ocurriría redondeando a la media, que es lo que hace el módulo de oro:

| Caso | Cu truncado | Ley práctica | Cu con `HALF_UP` | Ley práctica |
|---|---:|---:|---:|---:|
| 10 g → 950‰ | **0,515** | 950,071‰ ✅ | 0,516 | 949,980‰ ❌ |
| 10 g → 925‰ | **0,800** | 925,000‰ ✅ | 0,800 | 925,000‰ ✅ |
| 10 g → 900‰ | **1,100** | 900,000‰ ✅ | 1,100 | 900,000‰ ✅ |
| 10 g → 800‰ | **2,487** | 800,032‰ ✅ | 2,488 | 799,967‰ ❌ |
| 100 g → 950‰ | **5,157** | 950,008‰ ✅ | 5,158 | 949,999‰ ❌ |
| 100 g → 800‰ | **24,875** | 800,000‰ ✅ | 24,875 | 800,000‰ ✅ |

Con `HALF_UP` caerían por debajo de la ley objetivo **la mitad de los casos obligatorios de
§21** —950‰ y 800‰ con 10 g— y también el ejemplo de salida de §19, los 100 g hacia 950‰. No
es un caso de borde exótico. Esa es la razón de que la presentación trunque.
