# Especificación técnica — Calculadora de aleaciones de oro

**Versión:** 1.0  
**Fecha:** 2026-08-23  
**Objetivo:** documento de implementación para Claude Code, Codex o equipo de desarrollo.

---

## 1. Objetivo de la aplicación

Crear una calculadora de joyería que permita obtener aleaciones de oro a partir de **oro fino de 999‰**.

El usuario debe poder introducir una cantidad de oro 999, seleccionar:

- color del oro:
  - Amarillo
  - Blanco
  - Rojo
  - Rosa
- ley / quilataje:
  - 18 K → 750‰
  - 14 K → 585‰
  - 12 K → 500‰ (**preset técnico, no ley oficial española**)
  - 9 K → 375‰

La aplicación debe calcular:

1. oro fino real contenido en el oro 999;
2. peso final de la aleación;
3. cantidad total de liga que debe añadirse;
4. gramos exactos de cada metal de la liga:
   - Ag = plata fina;
   - Cu = cobre fino;
   - Pd = paladio;
5. ley teórica final resultante.

La aplicación debe trabajar siempre **por peso**.

---

# 2. Reglas legales para España

Según la Ley 17/1985 sobre objetos fabricados con metales preciosos, las leyes admitidas para oro en España son:

- 999‰
- 916‰
- 750‰
- 585‰
- 375‰

No existe tolerancia legal en menos.

Por tanto:

| Nombre mostrado | Ley objetivo | Uso en España |
|---|---:|---|
| 18 K | 750‰ | Oficial |
| 14 K | 585‰ | Oficial |
| 12 K | 500‰ | **No oficial — solo cálculo técnico** |
| 9 K | 375‰ | Oficial |

### Requisito de interfaz

Cuando el usuario seleccione **12 K / 500‰**, mostrar una advertencia visible:

> 500‰ / 12 K se incluye únicamente como referencia técnica de cálculo. No es una de las leyes oficiales de oro previstas para comercialización en España.

No presentar nunca 500‰ como ley oficial española.

Referencia legal:

- BOE — Ley 17/1985, art. 9:  
  https://www.boe.es/buscar/act.php?id=BOE-A-1985-12768

---

# 3. Conceptos fundamentales

## 3.1. No confundir oro 999 con oro 1000

El origen de todos los cálculos de esta aplicación será por defecto:

```text
ORO_ORIGEN = 999‰
PUREZA_ORIGEN = 0.999
```

100 gramos de oro 999 contienen:

```text
99.9 g de Au puro
0.1 g de otras materias / impurezas
```

Por tanto, **NO** se debe utilizar directamente la regla clásica:

```text
100 g oro + 33.333 g liga = oro 750
```

Esa regla solo sería exacta si el oro de partida fuese Au 1000‰.

Con oro 999‰, la liga correcta para 750‰ es:

```text
33.200 g de liga por cada 100 g de Au 999
```

---

# 4. Fórmula matemática principal

Variables:

```text
m_source = masa de oro de partida, en gramos
F_source = finura del oro de partida, en decimal
F_target = finura objetivo, en decimal
```

Para esta versión:

```text
F_source = 0.999
```

Ejemplos:

```text
750‰ = 0.750
585‰ = 0.585
500‰ = 0.500
375‰ = 0.375
```

## 4.1. Oro puro real contenido

```text
pure_gold = m_source * F_source
```

## 4.2. Peso final teórico

```text
final_mass = pure_gold / F_target
```

## 4.3. Liga total a añadir

```text
ligature_mass = final_mass - m_source
```

Forma equivalente:

```text
ligature_mass = m_source * ((F_source / F_target) - 1)
```

---

# 5. Coeficientes de liga para Au 999

Por cada gramo de oro 999:

| Ley | Finura | Liga por 1 g Au999 | Liga por 100 g Au999 |
|---|---:|---:|---:|
| 18 K | 0.750 | 0.332000000 | 33.200 g |
| 14 K | 0.585 | 0.707692308 | 70.769 g |
| 12 K | 0.500 | 0.998000000 | 99.800 g |
| 9 K | 0.375 | 1.664000000 | 166.400 g |

Estos coeficientes calculan únicamente la **cantidad total de liga**.

Después hay que repartir dicha liga según el color seleccionado.

---

# 6. Regla de implementación de las recetas

Las recetas deben guardarse como **proporciones internas de la liga**, no como porcentajes de la masa total final.

Ejemplo:

```text
18 K amarillo
Liga:
Ag = 66.00660066 %
Cu = 33.99339934 %
```

Entonces:

```text
Ag_to_add = ligature_mass * 0.6600660066
Cu_to_add = ligature_mass * 0.3399339934
```

La suma de todos los porcentajes de la liga debe ser siempre:

```text
1.0000000000
```

No calcular los metales de liga como porcentajes directos del peso final cuando se parte de Au999, porque el propio oro 999 ya contiene 1‰ de material no-Au.

---

# 7. Presets oficiales de la aplicación

## 7.1. ORO AMARILLO

### 18 K / 750‰ — receta de taller requerida

Receta original:

```text
Por cada 100 g de oro fino 1000:
22.00 g Ag
11.33 g Cu
```

Relación interna de la liga:

```text
Ag = 22 / 33.33 = 0.6600660066
Cu = 11.33 / 33.33 = 0.3399339934
```

Preset:

```json
{
  "color": "yellow",
  "fineness": 750,
  "ligature": {
    "Ag": 0.6600660066006601,
    "Cu": 0.3399339933993399
  }
}
```

Con 100 g de Au999:

```text
Liga total = 33.200 g
Ag = 21.914191 g
Cu = 11.285809 g
Peso final = 133.200 g
```

---

### 14 K / 585‰

Basado en la relación Ag/Cu típica publicada para oro amarillo 14 K:

```text
Ag : Cu = 30 : 11.7
```

Relación de la liga:

```text
Ag = 0.7194244604
Cu = 0.2805755396
```

Preset:

```json
{
  "color": "yellow",
  "fineness": 585,
  "ligature": {
    "Ag": 0.7194244604316547,
    "Cu": 0.2805755395683453
  }
}
```

Con 100 g de Au999:

```text
Liga total = 70.769231 g
Ag = 50.913116 g
Cu = 19.856115 g
Peso final = 170.769231 g
```

---

### 12 K / 500‰ — preset técnico

No existe una receta legal u oficial española para 500‰.

Para que la aplicación tenga un comportamiento determinista, usar este preset técnico derivado por interpolación entre las composiciones típicas de 14 K y 9 K:

```text
Ag = 0.6997596154
Cu = 0.3002403846
```

Preset:

```json
{
  "color": "yellow",
  "fineness": 500,
  "technical_only": true,
  "ligature": {
    "Ag": 0.6997596153846154,
    "Cu": 0.3002403846153846
  }
}
```

Con 100 g de Au999:

```text
Liga total = 99.800 g
Ag = 69.836010 g
Cu = 29.963990 g
Peso final = 199.800 g
```

---

### 9 K / 375‰

Composición típica:

```text
Au = 37.5 %
Ag = 42.5 %
Cu = 20.0 %
```

Relación dentro de la liga:

```text
Ag = 0.68
Cu = 0.32
```

Preset:

```json
{
  "color": "yellow",
  "fineness": 375,
  "ligature": {
    "Ag": 0.68,
    "Cu": 0.32
  }
}
```

Con 100 g de Au999:

```text
Liga total = 166.400 g
Ag = 113.152 g
Cu = 53.248 g
Peso final = 266.400 g
```

---

# 7.2. ORO BLANCO — SIN NÍQUEL

La aplicación debe utilizar exclusivamente la familia de oro blanco indicada aquí.

No introducir níquel automáticamente.

## 18 K / 750‰ — receta requerida

Receta original:

```text
Por cada 100 g de oro fino 1000:
14.75 g Pd
5.40 g Cu
13.18 g Ag
Total liga = 33.33 g
```

Relación interna:

```text
Pd = 0.4425442544
Ag = 0.3954395440
Cu = 0.1620162016
```

Preset:

```json
{
  "color": "white",
  "fineness": 750,
  "nickel_free": true,
  "ligature": {
    "Pd": 0.4425442544254425,
    "Ag": 0.3954395439543954,
    "Cu": 0.1620162016201620
  }
}
```

Con 100 g de Au999:

```text
Liga total = 33.200 g
Pd = 14.692469 g
Ag = 13.128593 g
Cu = 5.378938 g
Peso final = 133.200 g
```

---

## 14 K / 585‰

Relación típica nickel-free:

```text
Ag : Pd = 32.2 : 9.5
```

Relación interna:

```text
Ag = 0.7721822542
Pd = 0.2278177458
```

Preset:

```json
{
  "color": "white",
  "fineness": 585,
  "nickel_free": true,
  "ligature": {
    "Ag": 0.7721822541966426,
    "Pd": 0.2278177458033573
  }
}
```

Con 100 g de Au999:

```text
Liga total = 70.769231 g
Ag = 54.646744 g
Pd = 16.122487 g
Peso final = 170.769231 g
```

---

## 12 K / 500‰ — preset técnico

Preset interpolado entre las composiciones nickel-free de 14 K y 9 K:

```text
Ag = 0.8858173077
Pd = 0.1141826923
```

Preset:

```json
{
  "color": "white",
  "fineness": 500,
  "technical_only": true,
  "nickel_free": true,
  "ligature": {
    "Ag": 0.8858173076923077,
    "Pd": 0.1141826923076923
  }
}
```

Con 100 g de Au999:

```text
Liga total = 99.800 g
Ag = 88.404567 g
Pd = 11.395433 g
Peso final = 199.800 g
```

---

## 9 K / 375‰

Preset nickel-free de referencia:

```text
Ag = 1.0
```

Preset:

```json
{
  "color": "white",
  "fineness": 375,
  "nickel_free": true,
  "ligature": {
    "Ag": 1.0
  }
}
```

Con 100 g de Au999:

```text
Liga total = 166.400 g
Ag = 166.400 g
Peso final = 266.400 g
```

> Nota metalúrgica: el color visual de un oro blanco depende mucho de la formulación, acabado y eventual rodiado. Este preset es una referencia de composición, no una garantía de blancura visual exacta.

---

# 7.3. ORO ROJO

Para esta calculadora se define el oro rojo como sistema Au + Cu.

Toda la liga será cobre.

```text
Cu = 1.0
```

## 18 K / 750‰

```text
100 g Au999
+ 33.200 g Cu
= 133.200 g
```

## 14 K / 585‰

```text
100 g Au999
+ 70.769231 g Cu
= 170.769231 g
```

## 12 K / 500‰ — preset técnico

```text
100 g Au999
+ 99.800 g Cu
= 199.800 g
```

## 9 K / 375‰

```text
100 g Au999
+ 166.400 g Cu
= 266.400 g
```

Preset genérico:

```json
{
  "color": "red",
  "ligature": {
    "Cu": 1.0
  }
}
```

---

# 7.4. ORO ROSA

## 18 K / 750‰

Usar:

```text
Au = 75.0 %
Cu = 22.2 %
Ag = 2.8 %
```

Dentro de la liga de 25 %:

```text
Ag = 2.8 / 25 = 0.112
Cu = 22.2 / 25 = 0.888
```

Preset:

```json
{
  "color": "rose",
  "fineness": 750,
  "ligature": {
    "Ag": 0.112,
    "Cu": 0.888
  }
}
```

Con 100 g de Au999:

```text
Liga total = 33.200 g
Ag = 3.718400 g
Cu = 29.481600 g
Peso final = 133.200 g
```

---

## 14 K / 585‰

Relación típica:

```text
Ag : Cu = 9.2 : 32.5
```

Relación interna:

```text
Ag = 0.2206235012
Cu = 0.7793764988
```

Preset:

```json
{
  "color": "rose",
  "fineness": 585,
  "ligature": {
    "Ag": 0.2206235011990408,
    "Cu": 0.7793764988009592
  }
}
```

Con 100 g de Au999:

```text
Liga total = 70.769231 g
Ag = 15.613355 g
Cu = 55.155875 g
Peso final = 170.769231 g
```

---

## 12 K / 500‰ — preset técnico

Preset interpolado:

```text
Ag = 0.2701923077
Cu = 0.7298076923
```

Preset:

```json
{
  "color": "rose",
  "fineness": 500,
  "technical_only": true,
  "ligature": {
    "Ag": 0.2701923076923077,
    "Cu": 0.7298076923076923
  }
}
```

Con 100 g de Au999:

```text
Liga total = 99.800 g
Ag = 26.965192 g
Cu = 72.834808 g
Peso final = 199.800 g
```

---

## 9 K / 375‰

Composición típica:

```text
Au = 37.5 %
Ag = 20.0 %
Cu = 42.5 %
```

Dentro de la liga:

```text
Ag = 0.32
Cu = 0.68
```

Preset:

```json
{
  "color": "rose",
  "fineness": 375,
  "ligature": {
    "Ag": 0.32,
    "Cu": 0.68
  }
}
```

Con 100 g de Au999:

```text
Liga total = 166.400 g
Ag = 53.248 g
Cu = 113.152 g
Peso final = 266.400 g
```

---

# 8. Tabla maestra resumida

Valores en gramos por cada **100 g de Au999**.

| Color | Ley | Ag | Cu | Pd | Liga total | Peso final |
|---|---:|---:|---:|---:|---:|---:|
| Amarillo | 750 | 21.914 | 11.286 | 0 | 33.200 | 133.200 |
| Amarillo | 585 | 50.913 | 19.856 | 0 | 70.769 | 170.769 |
| Amarillo | 500* | 69.836 | 29.964 | 0 | 99.800 | 199.800 |
| Amarillo | 375 | 113.152 | 53.248 | 0 | 166.400 | 266.400 |
| Blanco | 750 | 13.129 | 5.379 | 14.692 | 33.200 | 133.200 |
| Blanco | 585 | 54.647 | 0 | 16.122 | 70.769 | 170.769 |
| Blanco | 500* | 88.405 | 0 | 11.395 | 99.800 | 199.800 |
| Blanco | 375 | 166.400 | 0 | 0 | 166.400 | 266.400 |
| Rojo | 750 | 0 | 33.200 | 0 | 33.200 | 133.200 |
| Rojo | 585 | 0 | 70.769 | 0 | 70.769 | 170.769 |
| Rojo | 500* | 0 | 99.800 | 0 | 99.800 | 199.800 |
| Rojo | 375 | 0 | 166.400 | 0 | 166.400 | 266.400 |
| Rosa | 750 | 3.718 | 29.482 | 0 | 33.200 | 133.200 |
| Rosa | 585 | 15.613 | 55.156 | 0 | 70.769 | 170.769 |
| Rosa | 500* | 26.965 | 72.835 | 0 | 99.800 | 199.800 |
| Rosa | 375 | 53.248 | 113.152 | 0 | 166.400 | 266.400 |

`* 500‰ / 12 K = preset técnico, no ley oficial española.`

---

# 9. Algoritmo obligatorio

Pseudocódigo:

```text
function calculateAlloy(
    sourceMass,
    sourceFineness,
    targetFineness,
    ligatureRatios
):
    validate(sourceMass > 0)
    validate(0 < sourceFineness <= 1)
    validate(0 < targetFineness < sourceFineness)
    validate(sum(ligatureRatios) == 1)

    pureGold = sourceMass * sourceFineness

    finalMass = pureGold / targetFineness

    ligatureMass = finalMass - sourceMass

    additions = {}

    for each metal, ratio in ligatureRatios:
        additions[metal] = ligatureMass * ratio

    calculatedGoldFineness =
        pureGold / (
            sourceMass +
            sum(additions)
        )

    return:
        sourceMass
        pureGold
        targetFineness
        finalMass
        ligatureMass
        additions
        calculatedGoldFineness
```

---

# 10. Regla crítica de redondeo

## NO redondear cálculos intermedios

Nunca hacer esto:

```text
liga = round(liga, 2)
Ag = liga * ratio
```

Hacer:

```text
liga_exacta = ...
Ag_exacto = liga_exacta * ratio
Cu_exacto = liga_exacta * ratio
```

Redondear únicamente para mostrar al usuario.

### Precisión interna recomendada

Mínimo:

```text
6 decimales
```

Preferible:

```text
Decimal / BigDecimal
```

Evitar `float` binario cuando el lenguaje tenga una implementación decimal adecuada.

Ejemplos:

- Python → `decimal.Decimal`
- Kotlin / Java → `BigDecimal`
- Dart / Flutter → paquete decimal o implementación decimal equivalente
- JavaScript / TypeScript → librería decimal (`decimal.js`, equivalente)

---

# 11. Redondeo de interfaz

Configuración recomendada:

```text
Vista normal: 3 decimales
Vista técnica: 6 decimales
```

Ejemplo:

```text
Plata fina: 2.191 g
Cobre fino: 1.129 g
```

No modificar el cálculo interno al cambiar el número de decimales mostrado.

---

# 12. Verificación matemática obligatoria

Tras calcular una receta, verificar siempre:

```text
sum(additions) == ligatureMass
```

con una tolerancia puramente computacional muy pequeña.

También:

```text
actualFineness =
    (sourceMass * sourceFineness)
    /
    (sourceMass + sum(additions))
```

Debe cumplirse:

```text
actualFineness >= targetFineness
```

y, salvo error decimal computacional:

```text
actualFineness == targetFineness
```

### Importante

La normativa española no admite tolerancia en menos.

La calculadora no debe introducir una ley inferior a la objetivo por culpa del redondeo.

---

# 13. Casos de prueba obligatorios

## Test 1 — Amarillo 18 K

Entrada:

```text
Au999 = 10.000 g
Color = amarillo
Ley = 750
```

Esperado:

```text
Au puro = 9.990000 g
Peso final = 13.320000 g
Liga = 3.320000 g

Ag = 2.191419142 g
Cu = 1.128580858 g
```

Verificación:

```text
2.191419142 + 1.128580858 = 3.320000000
9.99 / 13.32 = 0.750000000
```

---

## Test 2 — Blanco 18 K

Entrada:

```text
Au999 = 10.000 g
Color = blanco
Ley = 750
```

Esperado:

```text
Liga = 3.320000 g

Pd = 1.469246925 g
Ag = 1.312859286 g
Cu = 0.537893789 g
```

Suma:

```text
1.469246925
+ 1.312859286
+ 0.537893789
= 3.320000000 aproximadamente
```

---

## Test 3 — Rosa 18 K

Entrada:

```text
Au999 = 17.350 g
Color = rosa
Ley = 750
```

Esperado:

```text
Au puro = 17.332650 g
Peso final = 23.110200 g
Liga = 5.760200 g

Ag = 0.645142400 g
Cu = 5.115057600 g
```

---

## Test 4 — Amarillo 14 K

Entrada:

```text
Au999 = 25.000 g
Color = amarillo
Ley = 585
```

Esperado:

```text
Au puro = 24.975000 g
Peso final = 42.692307692 g
Liga = 17.692307692 g

Ag = 12.728278915 g
Cu = 4.964028777 g
```

---

## Test 5 — Rojo 9 K

Entrada:

```text
Au999 = 5.000 g
Color = rojo
Ley = 375
```

Esperado:

```text
Au puro = 4.995000 g
Peso final = 13.320000 g
Liga = 8.320000 g

Cu = 8.320000 g
```

---

# 14. Modo inverso recomendado

Además del modo principal, implementar preferentemente un segundo modo:

> Quiero fabricar X gramos de aleación final.

Variables:

```text
desiredFinalMass
targetFineness
sourceFineness
```

Cálculo:

```text
requiredSourceMass =
    desiredFinalMass
    * targetFineness
    / sourceFineness
```

Liga:

```text
ligatureMass =
    desiredFinalMass
    - requiredSourceMass
```

Luego:

```text
metalToAdd =
    ligatureMass
    * ligatureRatio
```

Ejemplo:

```text
Quiero 20.000 g de oro amarillo 750
Origen = Au999
```

Resultado:

```text
Au999 necesario = 15.015015015 g
Liga = 4.984984985 g
```

Después repartir la liga según el preset de amarillo 750.

Este modo es muy útil en taller y debería diseñarse desde el principio aunque pueda quedar oculto en una primera versión.

---

# 15. Modelo de datos recomendado

Las recetas NO deben estar dispersas por el código.

Crear una única fuente de verdad.

Ejemplo:

```json
{
  "source_fineness_default": 999,
  "recipes_version": "1.0",
  "recipes": {
    "yellow": {
      "750": {
        "Ag": 0.6600660066006601,
        "Cu": 0.3399339933993399
      },
      "585": {
        "Ag": 0.7194244604316547,
        "Cu": 0.2805755395683453
      },
      "500": {
        "technical_only": true,
        "Ag": 0.6997596153846154,
        "Cu": 0.3002403846153846
      },
      "375": {
        "Ag": 0.68,
        "Cu": 0.32
      }
    },

    "white": {
      "750": {
        "nickel_free": true,
        "Pd": 0.4425442544254425,
        "Ag": 0.3954395439543954,
        "Cu": 0.1620162016201620
      },
      "585": {
        "nickel_free": true,
        "Ag": 0.7721822541966426,
        "Pd": 0.2278177458033573
      },
      "500": {
        "technical_only": true,
        "nickel_free": true,
        "Ag": 0.8858173076923077,
        "Pd": 0.1141826923076923
      },
      "375": {
        "nickel_free": true,
        "Ag": 1.0
      }
    },

    "red": {
      "750": {
        "Cu": 1.0
      },
      "585": {
        "Cu": 1.0
      },
      "500": {
        "technical_only": true,
        "Cu": 1.0
      },
      "375": {
        "Cu": 1.0
      }
    },

    "rose": {
      "750": {
        "Ag": 0.112,
        "Cu": 0.888
      },
      "585": {
        "Ag": 0.2206235011990408,
        "Cu": 0.7793764988009592
      },
      "500": {
        "technical_only": true,
        "Ag": 0.2701923076923077,
        "Cu": 0.7298076923076923
      },
      "375": {
        "Ag": 0.32,
        "Cu": 0.68
      }
    }
  }
}
```

---

# 16. Validaciones

La aplicación debe rechazar:

```text
masa <= 0
NaN
Infinity
valores vacíos
finura origen <= finura objetivo
ratios negativos
sumatorio de ratios != 1
```

Debe permitir números con coma o punto decimal en la interfaz española:

```text
12,35
12.35
```

Internamente normalizar ambos a:

```text
12.35
```

---

# 17. Salida de la calculadora

Ejemplo de resultado:

```text
ORO AMARILLO — 18 K / 750‰

Oro de partida
10.000 g Au999

Oro puro contenido
9.990 g

Añadir
2.191 g Plata fina (Ag)
1.129 g Cobre fino (Cu)

Liga total
3.320 g

Peso final
13.320 g

Ley teórica
750.000‰
```

Para 12 K añadir:

```text
⚠ 500‰ es un preset técnico y no una ley oficial española.
```

---

# 18. Arquitectura lógica recomendada

Separar completamente:

```text
UI
↓
Use Case / Calculator Service
↓
Alloy Calculation Engine
↓
Recipe Repository
```

El motor matemático no debe depender de:

- Android;
- Flutter;
- Compose;
- React;
- framework gráfico;
- idioma;
- formato visual.

Debe poder probarse con tests unitarios puros.

---

# 19. Funciones mínimas

```text
calculateFromSourceMass(...)
calculateFromDesiredFinalMass(...)
getRecipe(color, fineness)
validateRecipe(recipe)
verifyResult(result)
formatResult(result, decimals)
```

---

# 20. Requisito de test de todas las recetas

Crear tests parametrizados para todas las combinaciones:

```text
4 colores × 4 leyes = 16 presets
```

Para cada uno comprobar:

1. `ligatureMass > 0`
2. todos los metales añadidos son >= 0;
3. suma de ratios = 1;
4. suma de metales añadidos = liga;
5. masa final = masa inicial + liga;
6. finura final = objetivo;
7. no hay pérdida de precisión por redondeo de UI.

---

# 21. Precisión y seguridad

Nunca modificar las cantidades para "hacerlas bonitas".

Por ejemplo, si el cálculo real es:

```text
Ag = 2.191419142
```

la UI puede mostrar:

```text
2.191 g
```

pero el motor debe conservar:

```text
2.191419142
```

No utilizar la cantidad mostrada en pantalla para recalcular resultados posteriores.

---

# 22. Posible evolución futura

La estructura debe permitir añadir posteriormente:

- oro de origen 999.9‰;
- oro de origen 916‰;
- recuperación / reciclado de oro con ley conocida;
- mezclas de dos aleaciones con leyes distintas;
- afinado / subida de ley;
- cálculo de ley de una mezcla existente;
- mermas de fundición;
- soldaduras;
- nuevas recetas de taller;
- zinc;
- platino;
- master alloys comerciales;
- densidades;
- coste de los metales;
- precio final de la liga.

No implementar estas funciones en la primera versión salvo petición expresa, pero **no diseñar el motor de forma que impida añadirlas**.

---

# 23. Procedencia de las recetas

### Legislación española

BOE — Ley 17/1985:

https://www.boe.es/buscar/act.php?id=BOE-A-1985-12768

La ley vigente recoge para oro:

```text
999, 916, 750, 585 y 375‰
```

y establece que no se admite tolerancia en menos.

### Composiciones de referencia

World Gold Council — Gold Jewellery: Colour, Carat & Purity:

https://www.gold.org/about-gold/about-gold-jewellery

Se utilizan como referencia sus proporciones típicas de:

- amarillo 14 K;
- amarillo 9 K;
- blanco nickel-free 14 K;
- blanco nickel-free 9 K;
- rosa 14 K;
- rosa 9 K.

El propio World Gold Council indica que estas composiciones son ejemplos típicos de la industria y **no son las únicas formulaciones posibles**.

### Nota específica sobre rosa 18 K

Algunas tablas web, incluida actualmente una fila publicada por World Gold Council, muestran:

```text
75 Au + 9.2 Ag + 22.2 Cu
```

lo cual suma 106.4 % y por tanto no puede utilizarse como composición física.

Para esta aplicación se fija la formulación coherente y ampliamente publicada:

```text
75.0 % Au
22.2 % Cu
2.8 % Ag
```

Referencia adicional:

https://www.finks.com/blogs/news-and-events/everyone-looks-glamorous-in-rose-gold-jewelry-find-your-dream-design-today

---

# 24. Nota sobre las recetas de 18 K proporcionadas por el taller

Estas dos formulaciones son requisitos explícitos del proyecto y **no deben ser sustituidas por otras recetas genéricas**:

## Amarillo 18 K

```text
22.00 partes Ag
11.33 partes Cu
por cada 100 partes de Au fino
```

## Blanco 18 K

```text
14.75 partes Pd
13.18 partes Ag
5.40 partes Cu
por cada 100 partes de Au fino
```

La aplicación debe adaptar esas relaciones al hecho de que el oro de partida es 999‰ utilizando la fórmula general de liga.

No debe añadir literalmente 33.33 g de liga a 100 g de Au999.

Debe añadir:

```text
33.200 g
```

repartidos con la misma relación interna.

---

# 25. Criterio de aceptación

La implementación estará terminada cuando:

- [ ] Permita introducir gramos de Au999.
- [ ] Permita seleccionar Amarillo, Blanco, Rojo o Rosa.
- [ ] Permita seleccionar 750, 585, 500 o 375‰.
- [ ] Calcule la liga usando la pureza real 0.999.
- [ ] Reparta la liga usando los presets definidos en este documento.
- [ ] Muestre cada metal a añadir.
- [ ] Muestre peso final.
- [ ] Muestre ley teórica final.
- [ ] Marque 500‰ como preset técnico no oficial en España.
- [ ] Use cálculo decimal de alta precisión.
- [ ] No redondee cálculos intermedios.
- [ ] Tenga tests unitarios para las 16 combinaciones.
- [ ] Pase todos los casos de prueba indicados.
- [ ] Mantenga las recetas en una única fuente de verdad.
- [ ] No sustituya las recetas 18 K de amarillo y blanco definidas por el proyecto.

---

# 26. Instrucción directa para Claude Code / Codex

Implementa la calculadora siguiendo **exactamente** las reglas matemáticas y presets de este documento.

Prioridades:

1. exactitud matemática;
2. no bajar nunca involuntariamente de la ley objetivo por redondeos;
3. separación entre motor de cálculo y UI;
4. recetas centralizadas y versionadas;
5. tests automáticos;
6. interfaz sencilla para uso real en un taller de joyería.

Si existe contradicción entre una fórmula genérica encontrada en Internet y los presets definidos expresamente en este documento, **prevalecen los presets de este documento**, salvo que el responsable del proyecto solicite expresamente modificarlos.

No inventar ni normalizar recetas por cuenta propia.

