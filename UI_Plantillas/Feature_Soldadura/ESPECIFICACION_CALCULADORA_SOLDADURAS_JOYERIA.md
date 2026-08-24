# Especificación funcional y matemática — Calculadora de soldaduras de joyería

## 1. Objetivo

Implementar en la aplicación una calculadora de soldaduras de joyería que permita escalar de forma proporcional las recetas facilitadas por el usuario.

La aplicación debe incluir tres familias independientes:

1. Soldaduras clásicas de oro amarillo.
2. Soldaduras de plata.
3. Soldaduras de oro de ley de 18 K mediante una base previa y una segunda mezcla con oro de 18 K.

> Importante: este documento especifica cómo programar los cálculos a partir de las recetas proporcionadas. No certifica metalúrgicamente las fórmulas ni sustituye la validación de un joyero profesional, un laboratorio o la normativa aplicable.

---

## 2. Reglas generales de cálculo

### 2.1 Unidad

- Unidad interna y visible: gramos (`g`).
- No aceptar valores iguales o inferiores a cero.
- La lógica debe admitir decimales.
- Para cálculos monetarios o de masa exacta, usar aritmética decimal (`BigDecimal`, `Decimal` o equivalente), no `Float`/`Double` como fuente de verdad.
- Conservar toda la precisión durante el cálculo y redondear únicamente al presentar el resultado.
- Mostrar por defecto 3 decimales, eliminando ceros finales si no son necesarios.

### 2.2 Escalado proporcional de una receta

Para una receta con pesos de referencia `w1, w2, ..., wn`:

```text
pesoReferenciaTotal = w1 + w2 + ... + wn
factorEscala = pesoFinalDeseado / pesoReferenciaTotal
pesoCalculadoIngrediente = pesoReferenciaIngrediente × factorEscala
```

Debe cumplirse, salvo la diferencia visual producida por el redondeo:

```text
suma(pesosCalculados) = pesoFinalDeseado
```

### 2.3 Modos de entrada

Implementar, como mínimo, el modo **peso final deseado**. Se recomienda admitir también el modo **cantidad de ingrediente base** cuando se indica expresamente en cada familia.

```text
CalculationMode:
- FINAL_MASS
- BASE_INGREDIENT_MASS
```

---

## 3. Soldaduras clásicas de oro amarillo

### 3.1 Identificadores

```text
GOLD_YELLOW_CLASSIC_SOFT
GOLD_YELLOW_CLASSIC_HARD
GOLD_YELLOW_CLASSIC_VERY_SOFT_LAW
```

### 3.2 Soldadura de oro amarillo clásica — Floja

Receta patrón:

| Ingrediente | Peso patrón |
|---|---:|
| Oro amarillo de 18 K | 5 g |
| Plata fina | 2 g |
| Latón | 1 g |
| **Total** | **8 g** |

Proporciones sobre el total:

| Ingrediente | Proporción |
|---|---:|
| Oro amarillo de 18 K | 62,5 % |
| Plata fina | 25 % |
| Latón | 12,5 % |

Fórmula para un peso final `T`:

```text
oro18K = T × 5 / 8
plataFina = T × 2 / 8
laton = T × 1 / 8
```

Prueba con la receta original:

```text
Entrada: T = 8 g
Salida: oro 18 K = 5 g; plata fina = 2 g; latón = 1 g
```

### 3.3 Soldadura de oro amarillo clásica — Fuerte

Receta patrón:

| Ingrediente | Peso patrón |
|---|---:|
| Latón | 0,50 g |
| Cobre | 0,50 g |
| Plata fina | 0,50 g |
| Oro amarillo de 18 K | 5 g |
| **Total** | **6,50 g** |

Fórmula para un peso final `T`:

```text
laton = T × 0,50 / 6,50
cobre = T × 0,50 / 6,50
plataFina = T × 0,50 / 6,50
oro18K = T × 5 / 6,50
```

Prueba con la receta original:

```text
Entrada: T = 6,50 g
Salida: latón = 0,50 g; cobre = 0,50 g; plata fina = 0,50 g; oro 18 K = 5 g
```

### 3.4 Soldadura de oro amarillo clásica — Muy floja de ley

Receta patrón:

| Ingrediente | Peso patrón |
|---|---:|
| Oro fino de 24 K | 1 g |
| Plata fina | 0,10 g |
| Latón | 0,16 g |
| Cadmio | 0,18 g |
| **Total** | **1,44 g** |

Fórmula para un peso final `T`:

```text
oro24K = T × 1 / 1,44
plataFina = T × 0,10 / 1,44
laton = T × 0,16 / 1,44
cadmio = T × 0,18 / 1,44
```

Prueba con la receta original:

```text
Entrada: T = 1,44 g
Salida: oro 24 K = 1 g; plata fina = 0,10 g; latón = 0,16 g; cadmio = 0,18 g
```

La equivalencia facilitada por el usuario usa un factor `× 7`:

```text
7 g de oro 24 K
0,70 g de plata fina
1,12 g de latón
1,26 g de cadmio
Total = 10,08 g
```

No interpretar esta equivalencia como un lote de 10 g: su total matemático es **10,08 g**.

---

## 4. Soldaduras de plata

### 4.1 Interpretación obligatoria del porcentaje

Los porcentajes indicados representan la cantidad de latón respecto al peso de plata fina, no el porcentaje de latón sobre el peso final.

La propia receta lo confirma:

```text
25 g de plata fina × 75 % = 18,75 g de latón
```

Por tanto, si `S` es la cantidad de plata y `p` el factor de latón:

```text
laton = S × p
pesoFinal = S + laton = S × (1 + p)
```

Factores:

| Tipo | ID | Factor `p` de latón respecto a la plata |
|---|---|---:|
| Muy floja | `SILVER_VERY_SOFT` | 0,75 |
| Floja | `SILVER_SOFT` | 0,50 |
| Normal | `SILVER_NORMAL` | 0,40 |
| Fuerte | `SILVER_HARD` | 0,30 |

### 4.2 Cálculo desde una cantidad de plata fina

Entrada: `silverMass = S`.

```text
plataFina = S
laton = S × p
pesoFinal = S × (1 + p)
```

Ejemplos para `S = 25 g`:

| Tipo | Plata fina | Latón | Peso final |
|---|---:|---:|---:|
| Muy floja | 25 g | 18,75 g | 43,75 g |
| Floja | 25 g | 12,50 g | 37,50 g |
| Normal | 25 g | 10 g | 35 g |
| Fuerte | 25 g | 7,50 g | 32,50 g |

### 4.3 Cálculo desde un peso final deseado

Si el usuario introduce el peso final `T`:

```text
plataFina = T / (1 + p)
laton = T - plataFina
```

Ejemplo: soldadura muy floja, `T = 10 g`, `p = 0,75`:

```text
plataFina = 10 / 1,75 = 5,714285... g
laton = 10 - 5,714285... = 4,285714... g
```

---

## 5. Soldaduras de oro de ley de 18 K mediante base

Este método consta obligatoriamente de dos cálculos independientes:

1. Preparación de la base.
2. Mezcla de la base con oro de 18 K del color seleccionado.

No mezclar esta familia con las recetas clásicas del apartado 3.

### 5.1 Colores admitidos

```text
YELLOW -> Oro amarillo de 18 K
WHITE  -> Oro blanco de 18 K
ROSE   -> Oro rosa de 18 K
```

El color solo cambia el oro de 18 K añadido en la segunda fase. La receta de la base no cambia.

### 5.2 Preparación de la base

Receta patrón:

| Ingrediente | Peso patrón |
|---|---:|
| Oro fino de 24 K | 10 g |
| Cobre | 0,54 g |
| Plata fina | 0,80 g |
| Zinc | 0,92 g |
| Cadmio | 1 g |
| **Total teórico** | **13,26 g** |

#### Cálculo desde la cantidad de oro fino

Si `G` es la cantidad de oro fino de 24 K:

```text
factor = G / 10
oro24K = G
cobre = 0,54 × factor
plataFina = 0,80 × factor
zinc = 0,92 × factor
cadmio = 1 × factor
pesoBaseTeorico = 13,26 × factor
```

#### Cálculo desde el peso final teórico de base

Si `B` es el peso de base deseado:

```text
factor = B / 13,26
oro24K = 10 × factor
cobre = 0,54 × factor
plataFina = 0,80 × factor
zinc = 0,92 × factor
cadmio = 1 × factor
```

#### Nota sobre la ley nominal

Con los valores redondeados de la receta, la proporción teórica de oro fino es:

```text
10 / 13,26 = 0,754147... = 754,15 milésimas ≈ 18,10 K
```

La aplicación debe conservar el nombre tradicional **“base de oro de 18 K”**, pero no debe mostrar que el resultado matemático exacto es `750 milésimas`. La pequeña diferencia procede de las cantidades redondeadas de la receta y podría verse afectada en taller por pérdidas o volatilización. No corregir automáticamente los pesos para forzar 750 milésimas.

### 5.3 Proceso informativo de preparación de la base

Mostrar como texto informativo, separado de la lógica matemática:

1. Fundir primero el oro, la plata y el cobre.
2. Cuando estén bien mezclados, añadir el zinc y el cadmio.
3. Bajar la intensidad del fuego para reducir la volatilización del zinc y el cadmio.
4. Laminar el lingote obtenido para poder cortarlo con tijera.

La masa calculada es teórica. La aplicación no debe compensar automáticamente posibles pérdidas durante la fundición.

### 5.4 Preparación de la soldadura: base + oro de 18 K

Definir los siguientes factores `r`, expresados como gramos de oro de 18 K por cada gramo de base:

| Tipo | ID | Base | Oro de 18 K | Factor `r` |
|---|---|---:|---:|---:|
| Muy floja | `GOLD_18K_BASE_VERY_SOFT` | 1 g | 0,3 g | 0,3 |
| Floja | `GOLD_18K_BASE_SOFT` | 1 g | 0,5 g | 0,5 |
| Media | `GOLD_18K_BASE_MEDIUM` | 1 g | 1 g | 1 |
| Fuerte | `GOLD_18K_BASE_HARD` | 1 g | 2 g | 2 |
| Muy fuerte | `GOLD_18K_BASE_VERY_HARD` | 1 g | 3 g | 3 |

#### Cálculo desde una cantidad de base

Si `B` es la cantidad de base disponible:

```text
base = B
oro18K = B × r
pesoFinal = B × (1 + r)
```

Prueba para `B = 1 g`:

| Tipo | Base | Oro de 18 K | Peso final |
|---|---:|---:|---:|
| Muy floja | 1 g | 0,3 g | 1,3 g |
| Floja | 1 g | 0,5 g | 1,5 g |
| Media | 1 g | 1 g | 2 g |
| Fuerte | 1 g | 2 g | 3 g |
| Muy fuerte | 1 g | 3 g | 4 g |

#### Cálculo desde el peso final deseado

Si `T` es el peso final deseado:

```text
base = T / (1 + r)
oro18K = T - base
```

Ejemplos para `T = 10 g`:

| Tipo | Base | Oro de 18 K |
|---|---:|---:|
| Muy floja | 7,692307... g | 2,307692... g |
| Floja | 6,666666... g | 3,333333... g |
| Media | 5 g | 5 g |
| Fuerte | 3,333333... g | 6,666666... g |
| Muy fuerte | 2,5 g | 7,5 g |

### 5.5 Regla de interpretación de la dureza

En este método:

- Más proporción de base implica una soldadura más floja.
- Más proporción de oro de 18 K implica una soldadura más fuerte.
- El oro añadido debe corresponder al color seleccionado: amarillo, blanco o rosa.

### 5.6 Proceso informativo de la mezcla final

Mostrar como recomendación aportada en la receta:

> Fundir y laminar una primera vez; después, volver a fundir y laminar para favorecer una mezcla homogénea.

---

## 6. Modelo de datos recomendado

El siguiente modelo es orientativo. Puede adaptarse al lenguaje y la arquitectura del proyecto.

```text
Material:
- GOLD_24K
- GOLD_YELLOW_18K
- GOLD_WHITE_18K
- GOLD_ROSE_18K
- FINE_SILVER
- COPPER
- BRASS
- ZINC
- CADMIUM

RecipeComponent:
- material: Material
- referenceWeight: Decimal

ScaledRecipe:
- recipeId: String
- requestedWeight: Decimal
- components: List<CalculatedComponent>
- theoreticalTotal: Decimal
- warnings: List<String>

CalculatedComponent:
- material: Material
- exactWeight: Decimal
- displayWeight: Decimal
```

Para las recetas clásicas se puede utilizar una función genérica:

```text
scaleRecipe(referenceComponents, desiredTotal):
    validate desiredTotal > 0
    referenceTotal = sum(component.referenceWeight)
    factor = desiredTotal / referenceTotal
    result = each component.referenceWeight × factor
    return result
```

Para plata y para la segunda fase de la base de oro de 18 K, usar las fórmulas específicas de los apartados 4 y 5.

---

## 7. Constantes de dominio

No duplicar números mágicos en la interfaz ni dentro de las funciones. Centralizar las recetas en constantes o configuración inmutable.

```json
{
  "goldYellowClassic": {
    "soft": {
      "GOLD_YELLOW_18K": "5",
      "FINE_SILVER": "2",
      "BRASS": "1"
    },
    "hard": {
      "BRASS": "0.50",
      "COPPER": "0.50",
      "FINE_SILVER": "0.50",
      "GOLD_YELLOW_18K": "5"
    },
    "verySoftLaw": {
      "GOLD_24K": "1",
      "FINE_SILVER": "0.10",
      "BRASS": "0.16",
      "CADMIUM": "0.18"
    }
  },
  "silverBrassFactors": {
    "verySoft": "0.75",
    "soft": "0.50",
    "normal": "0.40",
    "hard": "0.30"
  },
  "gold18kBase": {
    "GOLD_24K": "10",
    "COPPER": "0.54",
    "FINE_SILVER": "0.80",
    "ZINC": "0.92",
    "CADMIUM": "1.00"
  },
  "gold18kSolderFactors": {
    "verySoft": "0.30",
    "soft": "0.50",
    "medium": "1.00",
    "hard": "2.00",
    "veryHard": "3.00"
  }
}
```

Los valores se guardan como cadenas en este ejemplo para que se construyan como decimales exactos.

---

## 8. Validaciones y comportamiento esperado

### 8.1 Validaciones obligatorias

- Rechazar valores vacíos, no numéricos, `NaN`, infinitos, cero y negativos.
- Aceptar coma o punto como separador decimal en la entrada, normalizando según la configuración regional.
- No permitir seleccionar un color de oro en las recetas clásicas de oro amarillo.
- Exigir un color en el método de base de oro de 18 K.
- No combinar resultados de familias distintas.
- No redondear cada componente antes de terminar el cálculo.

### 8.2 Presentación del resultado

Mostrar:

- Nombre de la receta.
- Peso solicitado o cantidad base introducida.
- Ingredientes con su peso calculado.
- Peso final teórico.
- Tipo de soldadura.
- Color del oro cuando corresponda.
- Advertencias aplicables.

### 8.3 Ajuste visual del redondeo

Si al mostrar 3 decimales la suma visible difiere del total solicitado en una milésima, no modificar silenciosamente la fórmula. Se puede:

1. Mostrar una nota “La suma puede variar mínimamente por redondeo”, o
2. Aplicar un algoritmo de reparto de residuo únicamente a los valores de presentación, conservando internamente los pesos exactos.

---

## 9. Advertencia de seguridad obligatoria

Las recetas que contienen cadmio y zinc deben mostrar una advertencia antes del proceso informativo:

> **Seguridad:** al calentar materiales con cadmio o zinc pueden generarse humos peligrosos. No inhalar los humos. Trabajar únicamente con extracción localizada/ventilación adecuada, los equipos de protección correspondientes y conforme a la normativa de seguridad aplicable. Esta calculadora no sustituye la formación profesional ni una evaluación de riesgos.

La interfaz no debe presentar las instrucciones de fundición como una garantía de seguridad. El cadmio calentado puede producir humos venenosos y se asocia a efectos graves sobre la salud; los humos de óxido de zinc pueden causar fiebre de los humos metálicos.

Fuentes de seguridad:

- [OSHA — requisitos generales y advertencia para metales de aportación con cadmio](https://www.osha.gov/laws-regs/regulations/standardnumber/1910/1910.252)
- [NIOSH — Zinc oxide: Pocket Guide to Chemical Hazards](https://www.cdc.gov/niosh/npg/npgd0675.html)

---

## 10. Pruebas automáticas mínimas

Implementar pruebas unitarias con tolerancia decimal definida o comparación exacta cuando la división sea finita.

```text
TEST 1 — Clásica floja, lote patrón
input: desiredTotal = 8
expected: gold18K=5, fineSilver=2, brass=1, total=8

TEST 2 — Clásica fuerte, lote patrón
input: desiredTotal = 6.50
expected: brass=0.50, copper=0.50, fineSilver=0.50, gold18K=5, total=6.50

TEST 3 — Clásica muy floja de ley, equivalencia ×7
input: desiredTotal = 10.08
expected: gold24K=7, fineSilver=0.70, brass=1.12, cadmium=1.26

TEST 4 — Plata muy floja desde plata
input: silverMass=25, factor=0.75
expected: fineSilver=25, brass=18.75, total=43.75

TEST 5 — Plata fuerte desde plata
input: silverMass=25, factor=0.30
expected: fineSilver=25, brass=7.50, total=32.50

TEST 6 — Base de oro 18 K desde oro fino
input: gold24K=10
expected: copper=0.54, fineSilver=0.80, zinc=0.92, cadmium=1, total=13.26

TEST 7 — Soldadura base muy floja desde base
input: base=1, factor=0.30
expected: selectedGold18K=0.30, total=1.30

TEST 8 — Soldadura base muy fuerte desde peso final
input: desiredTotal=10, factor=3
expected: base=2.5, selectedGold18K=7.5, total=10

TEST 9 — Color
input: color=WHITE, base=1, type=SOFT
expected material: GOLD_WHITE_18K; weight=0.5

TEST 10 — Validación
inputs: 0, negative, empty, nonNumeric
expected: validation error; no calculation
```

Añadir además tests de propiedad:

```text
- Todos los componentes calculados son mayores que cero.
- La suma interna de componentes coincide con el total teórico.
- Duplicar la entrada duplica todos los componentes.
- Cambiar el color en el método de 18 K cambia el material seleccionado, pero no su peso.
```

---

## 11. Criterios de aceptación

La implementación se considera terminada cuando:

- Están disponibles las tres familias de cálculo.
- Todas las recetas y factores coinciden exactamente con este documento.
- Se puede calcular desde un peso final en todas las familias.
- Plata y soldadura de base admiten también cálculo desde la cantidad de ingrediente base.
- La preparación de la base admite cálculo desde oro fino de 24 K y desde peso final de base.
- El color amarillo, blanco o rosa solo afecta al oro de 18 K de la segunda fase.
- La aplicación diferencia claramente masa teórica y proceso de taller.
- Se muestra la advertencia de seguridad cuando intervienen cadmio o zinc.
- Las pruebas mínimas del apartado 10 pasan correctamente.
- La interfaz no afirma que las recetas estén certificadas o verificadas metalúrgicamente.

---

## 12. Instrucción final para el agente de desarrollo

Implementa la lógica de dominio de esta calculadora sin modificar las proporciones descritas. Mantén los cálculos separados de la interfaz, centraliza todas las constantes, utiliza aritmética decimal y añade las pruebas indicadas. Si ya existe una arquitectura en el proyecto, intégrate en ella sin reescribir componentes ajenos. Ante cualquier discrepancia entre el diseño visual y esta especificación, esta especificación prevalece para la lógica matemática.
