# Especificación técnica: calculadora de peso de chapas de oro y plata

> Aplicación: **Calculadora de Joyeros**.  
> Módulo: **Herramientas → Peso de chapas**.  
> Plataforma prevista: Android, Kotlin y Jetpack Compose.  
> Destinatarios: equipo de desarrollo, Codex y Claude Code.  
> Fecha de revisión: 25 de agosto de 2026.

---

## 1. Finalidad

Calcular el peso aproximado, en gramos, de una chapa rectangular maciza de oro o plata utilizando:

- Ancho, expresado en milímetros.
- Largo, expresado en milímetros.
- Espesor o grueso, expresado en milímetros.
- Material y ley de la aleación.

El resultado corresponde al peso total de la aleación, no solamente a su contenido de oro o plata fina.

Opcionalmente, la herramienta también puede mostrar:

- Volumen de la chapa.
- Densidad aplicada.
- Contenido de oro fino o plata fina.
- Peso del resto de metales de la aleación.
- Cantidad de material necesaria considerando un margen de merma.
- Coste estimado cuando se disponga de cotizaciones por gramo.
- Cálculo inverso del largo, ancho o espesor a partir de un peso objetivo.

---

## 2. Materiales que deben aparecer en el selector

### 2.1. Oro amarillo

| Opción | Ley aplicada | Contenido nominal de oro |
| --- | ---: | ---: |
| Oro amarillo 18 K | `750 ‰` | `75,0 %` |
| Oro amarillo 14 K | `585 ‰` | `58,5 %` |
| Oro amarillo 12 K | `500 ‰` | `50,0 %` |
| Oro amarillo 9 K | `375 ‰` | `37,5 %` |

### 2.2. Plata

| Opción | Ley aplicada | Plata fina | Cobre supuesto |
| --- | ---: | ---: | ---: |
| Plata 950 | `950 ‰` | `95,0 %` | `5,0 %` |
| Plata 925 | `925 ‰` | `92,5 %` | `7,5 %` |
| Plata 900 | `900 ‰` | `90,0 %` | `10,0 %` |
| Plata 800 | `800 ‰` | `80,0 %` | `20,0 %` |

**Corrección del requisito original:** la lista recibida repetía `900`: `900, 925, 900, 800`. Para conservar cuatro opciones distintas se interpreta como `950, 925, 900, 800`, de acuerdo con los niveles de plata utilizados en el resto de la aplicación. Si el producto finalmente quiere otra ley, modificar el catálogo, pero no duplicar `900`.

La columna «cobre supuesto» solo es válida para una aleación binaria plata-cobre. Una plata comercial puede contener otros componentes; en ese caso debe utilizarse la composición real del proveedor.

---

## 3. Aclaración legal para España

El artículo 9 de la Ley 17/1985 recoge las siguientes leyes comerciales:

- Oro: `999`, `916`, `750`, `585` y `375` milésimas.
- Plata: `999`, `925` y `800` milésimas.

Consecuencias para la aplicación:

- Oro de `18 K / 750`, `14 K / 585` y `9 K / 375` coinciden con leyes enumeradas.
- Oro de `12 K / 500` puede incorporarse como aleación técnica o internacional, pero `500` no figura en esa relación española de leyes del oro.
- Plata `925` y `800` coinciden con las leyes enumeradas.
- Plata `950` y `900` pueden calcularse como composiciones técnicas, pero no aparecen en esa relación española de leyes de la plata.
- No mostrar «ley oficial española» para `12 K`, `950` o `900`.
- Esto no impide utilizarlas como opciones matemáticas, referencias internacionales, materias primas o cálculos de taller.
- Las condiciones de comercialización de productos terminados deben verificarse por separado.

Fuente oficial: [Ley 17/1985, artículo 9, BOE](https://www.boe.es/buscar/act.php?id=BOE-A-1985-12768).

### 3.1. Precisión de 14 quilates

Matemáticamente:

```text
14 / 24 = 0,583333...
```

Sin embargo, para esta aplicación se usará la denominación comercial española:

```text
Oro 14 K = 585 ‰ = 58,5 % de oro fino
```

No calcular el contenido de oro de una pieza 14 K con `14 / 24` si el selector dice explícitamente `585`. El valor de la ley registrada es el que determina la fracción de metal fino.

---

## 4. Fórmula principal

La masa se obtiene multiplicando volumen por densidad:

```text
peso = volumen × densidad
```

Con medidas en milímetros y densidad en gramos por centímetro cúbico:

```text
peso_g = (ancho_mm × largo_mm × espesor_mm × densidad_g_cm3) / 1000
```

La división entre `1000` convierte el volumen de milímetros cúbicos a centímetros cúbicos:

```text
1 cm = 10 mm

1 cm³ = 10 mm × 10 mm × 10 mm

1 cm³ = 1000 mm³
```

### 4.1. Desarrollo por pasos

```text
área_mm2 = ancho_mm × largo_mm

volumen_mm3 = área_mm2 × espesor_mm

volumen_cm3 = volumen_mm3 / 1000

peso_g = volumen_cm3 × densidad_g_cm3
```

### 4.2. Fórmula lista para programar

```kotlin
val pesoGramos = anchoMm
    .multiply(largoMm)
    .multiply(espesorMm)
    .multiply(densidadGramosCm3)
    .divide(BigDecimal("1000"))
```

Los parámetros anteriores deben ser `BigDecimal`.

---

## 5. Densidades predeterminadas

### 5.1. Tabla recomendada para la primera versión

| Material | Ley | Densidad orientativa | Tipo de referencia |
| --- | ---: | ---: | --- |
| Oro amarillo 18 K | `750` | `15,58 g/cm³` | Tabla técnica de aleaciones. |
| Oro amarillo 14 K | `585` | `13,07 g/cm³` | Tabla técnica de aleaciones. |
| Oro amarillo 12 K | `500` | `12,75 g/cm³` | Referencia orientativa; requiere calibración. |
| Oro amarillo 9 K | `375` | `11,20 g/cm³` | Tabla de fabricante de aleaciones. |
| Plata 950 | `950` | `10,40 g/cm³` | Estimación teórica plata-cobre. |
| Plata 925 | `925` | `10,36 g/cm³` | Ficha técnica de plata sterling. |
| Plata 900 | `900` | `10,31 g/cm³` | Estimación teórica plata-cobre. |
| Plata 800 | `800` | `10,14 g/cm³` | Estimación teórica plata-cobre. |

**Estas densidades son configuraciones orientativas, no constantes universales.** La misma ley puede tener densidades diferentes dependiendo de la proporción de plata, cobre, zinc, paladio, níquel u otros metales; también influyen la composición comercial y el estado físico de la pieza.

Prioridad recomendada:

```text
1. Densidad medida de la aleación real del taller.
2. Densidad comunicada por el fabricante de la chapa o de la liga.
3. Valor predeterminado de esta especificación.
```

### 5.2. Fuentes de los valores de oro

La tabla técnica de Maguire Refining publica:

```text
Oro amarillo 18 K: 15,58 g/cm³
Oro amarillo 14 K: 13,07 g/cm³
```

Fuente: [Maguire Refining: Physical Properties of Various Metals and Alloys](https://www.maguireref.com/wp-content/uploads/2012/03/Physical-Properties.pdf).

Regal Castings publica:

```text
Oro amarillo 9 K: 11,2 g/cm³
```

Fuente: [Regal Castings: Alloy Density Table](https://www.regal.co.nz/pages/Alloy-Density-Table).

La referencia de `12,75 g/cm³` para 12 quilates figura en una tabla numismática de densidades. Se adopta como valor inicial práctico, pero debe sustituirse por la ficha técnica de la liga real si está disponible.

Fuente: [Newman Numismatic Portal, Washington University in St. Louis: Specific Gravity](https://nnp.wustl.edu/library/dictionarydetail/516784).

### 5.3. Fuente y cálculo para la plata

ESPI Metals especifica para plata sterling `925` compuesta por `92,5 % Ag` y `7,5 % Cu`:

```text
densidad_plata_925 = 10,36 g/cm³
```

Fuente: [ESPI Metals: Silver - Sterling](https://www.espimetals.com/index.php/technical-data/81-Silver%20-%20Sterling).

Para `950`, `900` y `800`, si se supone exclusivamente plata y cobre, puede estimarse la densidad por la regla aproximada de volúmenes específicos:

```text
1 / densidad_aleacion ≈
    fraccion_masa_plata / densidad_plata_fina
  + fraccion_masa_cobre / densidad_cobre
```

Con:

```text
densidad_plata_fina = 10,49 g/cm³

densidad_cobre = 8,96 g/cm³

fraccion_masa_plata = ley / 1000

fraccion_masa_cobre = 1 - fraccion_masa_plata
```

Se obtiene aproximadamente:

| Plata | Cálculo teórico | Valor configurado |
| --- | ---: | ---: |
| `950` | `10,401195... g/cm³` | `10,40 g/cm³` |
| `925` | `10,357354... g/cm³` | `10,36 g/cm³` |
| `900` | `10,313881... g/cm³` | `10,31 g/cm³` |
| `800` | `10,143578... g/cm³` | `10,14 g/cm³` |

La regla asume aditividad aproximada del volumen y no incorpora posibles contracciones o expansiones de mezcla. En particular, una plata `800` con aleantes adicionales puede presentar una densidad distinta. No etiquetar estos valores como datos medidos si no se han medido.

---

## 6. Comprobación de la captura de referencia

La pantalla adjunta muestra:

```text
Material: oro 18 K

Ancho: 10 mm

Largo: 20 mm

Espesor: 0,5 mm
```

### 6.1. Volumen

```text
volumen_mm3 = 10 × 20 × 0,5

volumen_mm3 = 100 mm³

volumen_cm3 = 100 / 1000

volumen_cm3 = 0,1 cm³
```

### 6.2. Peso

```text
peso_g = 0,1 × 15,58

peso_g = 1,558 g

peso_visible = 1,56 g
```

**Conclusión: el resultado `1,56 g` de la captura es correcto para oro amarillo 18 K con densidad `15,58 g/cm³`.**

### 6.3. Contenido de oro fino

```text
oro_fino_g = 1,558 × 0,750

oro_fino_g = 1,1685 g
```

### 6.4. Otros metales

```text
liga_g = 1,558 × 0,250

liga_g = 0,3895 g
```

No debe mostrarse `1,56 g` como oro fino: `1,56 g` es el peso aproximado de toda la chapa de oro de 18 K.

---

## 7. Tabla de resultados para las mismas medidas

Referencia común:

```text
ancho = 10 mm

largo = 20 mm

espesor = 0,5 mm

volumen = 100 mm³ = 0,1 cm³
```

| Material | Densidad | Peso bruto exacto | Mostrar | Metal fino aproximado |
| --- | ---: | ---: | ---: | ---: |
| Oro 18 K / 750 | `15,58` | `1,558 g` | `1,56 g` | `1,1685 g Au` |
| Oro 14 K / 585 | `13,07` | `1,307 g` | `1,31 g` | `0,764595 g Au` |
| Oro 12 K / 500 | `12,75` | `1,275 g` | `1,28 g` | `0,6375 g Au` |
| Oro 9 K / 375 | `11,20` | `1,120 g` | `1,12 g` | `0,4200 g Au` |
| Plata 950 | `10,40` | `1,040 g` | `1,04 g` | `0,9880 g Ag` |
| Plata 925 | `10,36` | `1,036 g` | `1,04 g` | `0,9583 g Ag` |
| Plata 900 | `10,31` | `1,031 g` | `1,03 g` | `0,9279 g Ag` |
| Plata 800 | `10,14` | `1,014 g` | `1,01 g` | `0,8112 g Ag` |

Los importes de la columna «Mostrar» se redondean a dos decimales con `HALF_UP`.

---

## 8. Fórmulas auxiliares

### 8.1. Contenido de metal fino

```text
metal_fino_g = peso_total_g × ley_milesimas / 1000
```

Ejemplos:

```text
oro_fino_18k = peso_total_g × 0,750

oro_fino_14k = peso_total_g × 0,585

oro_fino_12k = peso_total_g × 0,500

oro_fino_9k = peso_total_g × 0,375

plata_fina_925 = peso_total_g × 0,925
```

### 8.2. Resto de aleación

```text
otros_metales_g = peso_total_g - metal_fino_g
```

En plata binaria plata-cobre:

```text
cobre_g = peso_total_g × (1 - ley_milesimas / 1000)
```

En oro, el peso de la liga puede incluir plata, cobre y otros metales. Sin conocer la receta exacta no se puede desglosar correctamente por componentes.

### 8.3. Cálculo inverso del largo

```text
largo_mm =
    (peso_objetivo_g × 1000)
    / (ancho_mm × espesor_mm × densidad_g_cm3)
```

Ejemplo:

```text
peso_objetivo = 2,5 g

ancho = 10 mm

espesor = 0,5 mm

densidad_oro_18k = 15,58 g/cm³

largo = (2,5 × 1000) / (10 × 0,5 × 15,58)

largo = 32,092426... mm

largo_visible = 32,09 mm
```

### 8.4. Cálculo inverso del ancho

```text
ancho_mm =
    (peso_objetivo_g × 1000)
    / (largo_mm × espesor_mm × densidad_g_cm3)
```

### 8.5. Cálculo inverso del espesor

```text
espesor_mm =
    (peso_objetivo_g × 1000)
    / (ancho_mm × largo_mm × densidad_g_cm3)
```

### 8.6. Merma como margen añadido sobre la pieza final

Si el usuario añade un margen del `5 %` sobre el peso de la pieza:

```text
peso_con_margen = peso_pieza × (1 + porcentaje_margen / 100)
```

Ejemplo:

```text
peso_pieza = 1,558 g

margen = 5 %

peso_con_margen = 1,558 × 1,05

peso_con_margen = 1,6359 g

mostrar = 1,64 g
```

### 8.7. Merma como porcentaje de pérdida del material inicial

Si realmente se pierde el `5 %` del material inicial durante el proceso:

```text
peso_inicial_necesario = peso_pieza_final / (1 - porcentaje_perdida / 100)
```

Ejemplo:

```text
peso_pieza_final = 1,558 g

pérdida = 5 %

peso_inicial_necesario = 1,558 / 0,95

peso_inicial_necesario = 1,64 g
```

Ambas fórmulas no significan lo mismo. La interfaz debe indicar si el porcentaje es «margen adicional» o «pérdida sobre material inicial».

---

## 9. Estimación de costes usando cotizaciones

### 9.1. Si se conoce el precio de la aleación terminada

```text
coste_chapa = peso_total_g × precio_aleacion_eur_g
```

### 9.2. Si únicamente se conoce la cotización del oro fino

```text
valor_oro_fino = peso_total_g × fraccion_oro × precio_oro_fino_eur_g
```

La fórmula anterior calcula solo el valor del contenido de oro fino. No incluye el valor de plata, cobre, otros aleantes, fabricación, margen comercial, impuestos ni merma.

### 9.3. Plata-cobre con cotizaciones por gramo

```text
coste_metal =
    plata_fina_g × precio_plata_fina_eur_g
  + cobre_g × precio_cobre_eur_g
```

### 9.4. Oro amarillo con receta conocida

```text
coste_metal =
    oro_fino_g × precio_oro_fino_eur_g
  + plata_g × precio_plata_fina_eur_g
  + cobre_g × precio_cobre_eur_g
  + otros_metales_g × sus_precios
```

Si se conecta con el módulo «Precio metales», comprobar primero que todas las cotizaciones están expresadas en `€/g`. No multiplicar directamente un precio por onza troy o por kilogramo por el peso en gramos.

---

## 10. Precisión y redondeo

### 10.1. Reglas

- Utilizar `BigDecimal` para medidas, densidades, pesos y precios.
- Construir `BigDecimal` desde cadenas, por ejemplo `BigDecimal("15.58")`.
- Evitar `BigDecimal(15.58)` y evitar `Float` para cálculos monetarios.
- No redondear el volumen ni el peso antes de obtener el metal fino.
- Mantener al menos ocho decimales internos cuando exista una división.
- Mostrar normalmente el peso con dos decimales.
- Permitir opcionalmente tres decimales para trabajos muy pequeños.
- Aceptar coma decimal española y punto decimal.
- Usar `RoundingMode.HALF_UP` al mostrar.
- Conservar los valores internos completos para cálculos posteriores.

### 10.2. Ejemplo

```text
peso exacto: 1,558 g

mostrar: 1,56 g

oro fino correcto: 1,558 × 0,750 = 1,1685 g

oro fino calculado desde el valor ya redondeado: 1,56 × 0,750 = 1,17 g
```

La segunda forma pierde precisión y no debe usarse como base de operaciones sucesivas.

---

## 11. Validaciones de entrada

### 11.1. Obligatorios

```text
ancho > 0

largo > 0

espesor > 0

densidad > 0

ley >= 0 y ley <= 1000
```

### 11.2. Formatos válidos

```text
10

10.5

10,5

0.50

0,50
```

### 11.3. Valores que deben rechazarse

```text
cadena vacía

0

-1

texto no numérico

1,2,3

1.2.3

NaN

Infinity
```

Evitar convertir indiscriminadamente cualquier combinación de comas y puntos. Elegir una política clara: admitir un único separador decimal y no admitir separadores de miles en estos campos de entrada.

### 11.4. Límites operativos sugeridos

Valores configurables según las necesidades de la aplicación:

```text
ancho máximo: 10.000 mm

largo máximo: 10.000 mm

espesor máximo: 1.000 mm

densidad máxima personalizada: 30 g/cm³
```

Los límites son controles de interfaz, no leyes físicas generales. Ajustarlos si el taller trabaja con piezas de otras dimensiones.

---

## 12. Modelo Kotlin: tipo de metal

### 12.1. `MetalFamily.kt`

```kotlin
package com.ejemplo.calculadorajoyeros.domain.sheet

enum class MetalFamily {
    GOLD,
    SILVER
}
```

### 12.2. `SheetMaterial.kt`

```kotlin
package com.ejemplo.calculadorajoyeros.domain.sheet

import java.math.BigDecimal

enum class SheetMaterial(
    val family: MetalFamily,
    val displayName: String,
    val finenessPerThousand: Int,
    val densityGramsPerCm3: BigDecimal,
    val isSpanishCommercialFineness: Boolean
) {
    GOLD_18K(
        family = MetalFamily.GOLD,
        displayName = "Oro amarillo 18 K · 750",
        finenessPerThousand = 750,
        densityGramsPerCm3 = BigDecimal("15.58"),
        isSpanishCommercialFineness = true
    ),

    GOLD_14K(
        family = MetalFamily.GOLD,
        displayName = "Oro amarillo 14 K · 585",
        finenessPerThousand = 585,
        densityGramsPerCm3 = BigDecimal("13.07"),
        isSpanishCommercialFineness = true
    ),

    GOLD_12K(
        family = MetalFamily.GOLD,
        displayName = "Oro amarillo 12 K · 500",
        finenessPerThousand = 500,
        densityGramsPerCm3 = BigDecimal("12.75"),
        isSpanishCommercialFineness = false
    ),

    GOLD_9K(
        family = MetalFamily.GOLD,
        displayName = "Oro amarillo 9 K · 375",
        finenessPerThousand = 375,
        densityGramsPerCm3 = BigDecimal("11.20"),
        isSpanishCommercialFineness = true
    ),

    SILVER_950(
        family = MetalFamily.SILVER,
        displayName = "Plata 950",
        finenessPerThousand = 950,
        densityGramsPerCm3 = BigDecimal("10.40"),
        isSpanishCommercialFineness = false
    ),

    SILVER_925(
        family = MetalFamily.SILVER,
        displayName = "Plata 925 · de ley",
        finenessPerThousand = 925,
        densityGramsPerCm3 = BigDecimal("10.36"),
        isSpanishCommercialFineness = true
    ),

    SILVER_900(
        family = MetalFamily.SILVER,
        displayName = "Plata 900",
        finenessPerThousand = 900,
        densityGramsPerCm3 = BigDecimal("10.31"),
        isSpanishCommercialFineness = false
    ),

    SILVER_800(
        family = MetalFamily.SILVER,
        displayName = "Plata 800 · de ley",
        finenessPerThousand = 800,
        densityGramsPerCm3 = BigDecimal("10.14"),
        isSpanishCommercialFineness = true
    );

    val fineMetalFraction: BigDecimal
        get() = BigDecimal.valueOf(finenessPerThousand.toLong())
            .divide(BigDecimal("1000"))
}
```

El atributo `isSpanishCommercialFineness` describe si la ley figura en la relación del artículo 9; no certifica que una pieza concreta cumpla todas las obligaciones legales de contraste, etiquetado y comercialización.

---

## 13. Modelo de entrada y resultado

### 13.1. `SheetCalculationInput.kt`

```kotlin
package com.ejemplo.calculadorajoyeros.domain.sheet

import java.math.BigDecimal

data class SheetCalculationInput(
    val widthMm: BigDecimal,
    val lengthMm: BigDecimal,
    val thicknessMm: BigDecimal,
    val material: SheetMaterial,
    val customDensityGramsPerCm3: BigDecimal? = null
)
```

### 13.2. `SheetCalculationResult.kt`

```kotlin
package com.ejemplo.calculadorajoyeros.domain.sheet

import java.math.BigDecimal

data class SheetCalculationResult(
    val material: SheetMaterial,
    val widthMm: BigDecimal,
    val lengthMm: BigDecimal,
    val thicknessMm: BigDecimal,
    val areaSquareMm: BigDecimal,
    val volumeCubicMm: BigDecimal,
    val volumeCubicCm: BigDecimal,
    val appliedDensityGramsPerCm3: BigDecimal,
    val totalWeightGrams: BigDecimal,
    val fineMetalWeightGrams: BigDecimal,
    val alloyWeightGrams: BigDecimal,
    val usesCustomDensity: Boolean
)
```

---

## 14. Motor principal de cálculo

### 14.1. `SheetWeightCalculator.kt`

```kotlin
package com.ejemplo.calculadorajoyeros.domain.sheet

import java.math.BigDecimal
import java.math.RoundingMode

class SheetWeightCalculator(
    private val divisionScale: Int = 12
) {

    fun calculate(input: SheetCalculationInput): SheetCalculationResult {
        requirePositive(input.widthMm, "El ancho debe ser mayor que cero")
        requirePositive(input.lengthMm, "El largo debe ser mayor que cero")
        requirePositive(input.thicknessMm, "El espesor debe ser mayor que cero")

        val density = input.customDensityGramsPerCm3
            ?: input.material.densityGramsPerCm3

        requirePositive(density, "La densidad debe ser mayor que cero")

        val areaSquareMm = input.widthMm.multiply(input.lengthMm)

        val volumeCubicMm = areaSquareMm.multiply(input.thicknessMm)

        val volumeCubicCm = volumeCubicMm.divide(
            MM3_PER_CM3,
            divisionScale,
            RoundingMode.HALF_UP
        )

        val totalWeightGrams = volumeCubicCm.multiply(density)

        val fineMetalWeightGrams = totalWeightGrams.multiply(
            input.material.fineMetalFraction
        )

        val alloyWeightGrams = totalWeightGrams.subtract(
            fineMetalWeightGrams
        )

        return SheetCalculationResult(
            material = input.material,
            widthMm = input.widthMm,
            lengthMm = input.lengthMm,
            thicknessMm = input.thicknessMm,
            areaSquareMm = areaSquareMm,
            volumeCubicMm = volumeCubicMm,
            volumeCubicCm = volumeCubicCm,
            appliedDensityGramsPerCm3 = density,
            totalWeightGrams = totalWeightGrams,
            fineMetalWeightGrams = fineMetalWeightGrams,
            alloyWeightGrams = alloyWeightGrams,
            usesCustomDensity = input.customDensityGramsPerCm3 != null
        )
    }

    fun calculateLengthMm(
        targetWeightGrams: BigDecimal,
        widthMm: BigDecimal,
        thicknessMm: BigDecimal,
        densityGramsPerCm3: BigDecimal
    ): BigDecimal {
        requirePositive(targetWeightGrams, "El peso debe ser mayor que cero")
        requirePositive(widthMm, "El ancho debe ser mayor que cero")
        requirePositive(thicknessMm, "El espesor debe ser mayor que cero")
        requirePositive(densityGramsPerCm3, "La densidad debe ser mayor que cero")

        val denominator = widthMm
            .multiply(thicknessMm)
            .multiply(densityGramsPerCm3)

        return targetWeightGrams
            .multiply(MM3_PER_CM3)
            .divide(denominator, divisionScale, RoundingMode.HALF_UP)
    }

    fun addMargin(
        finishedWeightGrams: BigDecimal,
        marginPercentage: BigDecimal
    ): BigDecimal {
        requirePositive(finishedWeightGrams, "El peso debe ser mayor que cero")

        require(marginPercentage.signum() >= 0) {
            "El margen no puede ser negativo"
        }

        val multiplier = BigDecimal.ONE.add(
            marginPercentage.divide(
                ONE_HUNDRED,
                divisionScale,
                RoundingMode.HALF_UP
            )
        )

        return finishedWeightGrams.multiply(multiplier)
    }

    fun requiredInitialWeightForLoss(
        finishedWeightGrams: BigDecimal,
        lossPercentage: BigDecimal
    ): BigDecimal {
        requirePositive(finishedWeightGrams, "El peso debe ser mayor que cero")

        require(
            lossPercentage.signum() >= 0 &&
                lossPercentage < ONE_HUNDRED
        ) {
            "La pérdida debe ser igual o superior a 0 e inferior a 100"
        }

        val remainingFraction = BigDecimal.ONE.subtract(
            lossPercentage.divide(
                ONE_HUNDRED,
                divisionScale,
                RoundingMode.HALF_UP
            )
        )

        return finishedWeightGrams.divide(
            remainingFraction,
            divisionScale,
            RoundingMode.HALF_UP
        )
    }

    private fun requirePositive(value: BigDecimal, message: String) {
        require(value.signum() > 0) { message }
    }

    private companion object {
        val MM3_PER_CM3 = BigDecimal("1000")
        val ONE_HUNDRED = BigDecimal("100")
    }
}
```

Si se requiere minimizar el redondeo interno, el peso también puede calcularse directamente mediante `ancho × largo × espesor × densidad / 1000` y conservar el volumen únicamente para presentación.

---

## 15. Parser decimal para España

### 15.1. `DecimalInputParser.kt`

```kotlin
package com.ejemplo.calculadorajoyeros.domain.sheet

import java.math.BigDecimal

object DecimalInputParser {

    private val validDecimal = Regex("^\\d+(?:[.,]\\d+)?$")

    fun parsePositive(value: String): BigDecimal? {
        val trimmed = value.trim()

        if (!validDecimal.matches(trimmed)) {
            return null
        }

        val normalized = trimmed.replace(',', '.')
        val parsed = normalized.toBigDecimalOrNull() ?: return null

        return parsed.takeIf { number -> number.signum() > 0 }
    }
}
```

Este parser admite `0,5` y `0.5`, pero rechaza formatos ambiguos como `1,2,3` o `1.000,50`. En una pantalla de medidas de taller, una política simple es preferible a interpretar separadores de miles de manera incierta.

---

## 16. Estado de interfaz y ViewModel

### 16.1. `SheetWeightUiState.kt`

```kotlin
package com.ejemplo.calculadorajoyeros.ui.tools.sheet

import com.ejemplo.calculadorajoyeros.domain.sheet.SheetCalculationResult
import com.ejemplo.calculadorajoyeros.domain.sheet.SheetMaterial

data class SheetWeightUiState(
    val widthInput: String = "10",
    val lengthInput: String = "20",
    val thicknessInput: String = "0,5",
    val selectedMaterial: SheetMaterial = SheetMaterial.GOLD_18K,
    val result: SheetCalculationResult? = null,
    val validationError: String? = null
)
```

### 16.2. `SheetWeightViewModel.kt`

```kotlin
package com.ejemplo.calculadorajoyeros.ui.tools.sheet

import androidx.lifecycle.ViewModel
import com.ejemplo.calculadorajoyeros.domain.sheet.DecimalInputParser
import com.ejemplo.calculadorajoyeros.domain.sheet.SheetCalculationInput
import com.ejemplo.calculadorajoyeros.domain.sheet.SheetMaterial
import com.ejemplo.calculadorajoyeros.domain.sheet.SheetWeightCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SheetWeightViewModel(
    private val calculator: SheetWeightCalculator = SheetWeightCalculator()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SheetWeightUiState())

    val uiState: StateFlow<SheetWeightUiState> = _uiState.asStateFlow()

    init {
        calculate()
    }

    fun onWidthChange(value: String) {
        _uiState.update { current ->
            current.copy(widthInput = value, validationError = null)
        }
    }

    fun onLengthChange(value: String) {
        _uiState.update { current ->
            current.copy(lengthInput = value, validationError = null)
        }
    }

    fun onThicknessChange(value: String) {
        _uiState.update { current ->
            current.copy(thicknessInput = value, validationError = null)
        }
    }

    fun onMaterialChange(material: SheetMaterial) {
        _uiState.update { current ->
            current.copy(selectedMaterial = material, validationError = null)
        }

        calculate()
    }

    fun calculate() {
        val state = _uiState.value

        val width = DecimalInputParser.parsePositive(state.widthInput)
        val length = DecimalInputParser.parsePositive(state.lengthInput)
        val thickness = DecimalInputParser.parsePositive(state.thicknessInput)

        if (width == null || length == null || thickness == null) {
            _uiState.update { current ->
                current.copy(
                    result = null,
                    validationError =
                        "Introduce ancho, largo y espesor válidos mayores que cero"
                )
            }

            return
        }

        val input = SheetCalculationInput(
            widthMm = width,
            lengthMm = length,
            thicknessMm = thickness,
            material = state.selectedMaterial
        )

        val result = calculator.calculate(input)

        _uiState.update { current ->
            current.copy(result = result, validationError = null)
        }
    }
}
```

El cálculo es local, ligero y no necesita llamadas de red ni corrutinas. La variante con actualización instantánea puede recalcular al modificar cada campo cuando todos los valores sean válidos, siempre respetando el diseño actual de la aplicación.

---

## 17. Ejemplo Compose de presentación del resultado

```kotlin
package com.ejemplo.calculadorajoyeros.ui.tools.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ejemplo.calculadorajoyeros.domain.sheet.MetalFamily
import com.ejemplo.calculadorajoyeros.domain.sheet.SheetCalculationResult
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SheetWeightResultCard(
    result: SheetCalculationResult,
    modifier: Modifier = Modifier
) {
    val metalName = when (result.material.family) {
        MetalFamily.GOLD -> "Oro fino"
        MetalFamily.SILVER -> "Plata fina"
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "PESO DE LA CHAPA",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "${result.totalWeightGrams.formatDecimal(2)} g",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = result.material.displayName,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Densidad: " +
                    "${result.appliedDensityGramsPerCm3.formatDecimal(2)} g/cm³",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "$metalName: " +
                    "${result.fineMetalWeightGrams.formatDecimal(3)} g",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun BigDecimal.formatDecimal(decimals: Int): String {
    val formatter = NumberFormat.getNumberInstance(
        Locale.forLanguageTag("es-ES")
    )

    formatter.minimumFractionDigits = decimals
    formatter.maximumFractionDigits = decimals
    formatter.roundingMode = RoundingMode.HALF_UP

    return formatter.format(this)
}
```

Utilizar el tema, componentes, espaciados, tipografías e imágenes ya existentes en la aplicación. El ejemplo no pretende sustituir la identidad visual del proyecto.

---

## 18. Requisitos visuales basados en la pantalla existente

### 18.1. Navegación

Dentro de **Herramientas**, ofrecer dos selectores superiores:

```text
Precio metales

Peso de chapas
```

La denominación recomendada es **Peso de chapas**. Es más descriptiva que «Laminado», porque la función calcula el peso de una chapa; no simula por sí sola un proceso completo de laminación.

### 18.2. Ilustración

Representar una chapa rectangular con:

- Ancho identificado visualmente.
- Largo identificado visualmente.
- Espesor identificado visualmente.
- Color coherente con el metal seleccionado.
- Etiquetas legibles para lectores de pantalla mediante descripciones de contenido.

Si la vista es isométrica, las proporciones dibujadas pueden adaptarse visualmente, pero los cálculos deben usar exclusivamente los valores numéricos introducidos.

### 18.3. Selector de material

Propuesta de agrupación:

```text
ORO

Oro amarillo 18 K · 750
Oro amarillo 14 K · 585
Oro amarillo 12 K · 500
Oro amarillo 9 K · 375

PLATA

Plata 950
Plata 925 · de ley
Plata 900
Plata 800 · de ley
```

No presentar `950` o `900` como «ley oficial española». Si el producto se distribuye internacionalmente, separar la denominación técnica de la información regulatoria por país.

### 18.4. Campos

```text
Ancho          mm

Largo          mm

Espesor        mm
```

Se puede mantener el orden de la captura:

```text
Ancho

Espesor

Largo deseado
```

El orden visual no altera la fórmula. Usar teclado decimal y permitir tanto coma como punto.

### 18.5. Resultado

Resultado principal:

```text
PESO DE LA CHAPA

1,56 g

Oro amarillo 18 K · 750
```

Detalle opcional:

```text
Volumen: 100 mm³

Densidad utilizada: 15,58 g/cm³

Oro fino: 1,169 g

Otros metales: 0,390 g
```

Indicar que el resultado es **aproximado** cuando se haya utilizado una densidad estándar y no una densidad medida específicamente para la chapa.

---

## 19. Personalización de densidades

### 19.1. Motivo

Dos chapas de oro de 18 K pueden pesar distinto para el mismo volumen si sus ligas tienen composiciones diferentes. Por ejemplo, una referencia técnica publica valores distintos para colores de oro de igual ley:

```text
18 K amarillo: 15,58 g/cm³

18 K blanco: 14,64 g/cm³

18 K rojo: 15,18 g/cm³

18 K verde: 15,90 g/cm³
```

Estos son ejemplos concretos de una tabla; otro fabricante puede publicar cifras diferentes.

### 19.2. Funcionalidad recomendada

Permitir, opcionalmente:

```text
Configuración → Materiales → Editar densidad
```

Para cada material:

- Mostrar valor predeterminado.
- Permitir introducir densidad personalizada.
- Indicar si procede de una ficha técnica o medición.
- Permitir restablecer el valor predeterminado.
- Persistir la configuración mediante DataStore o el mecanismo existente.

### 19.3. Medición de una chapa conocida

Si se dispone de una chapa de dimensiones y peso conocidos:

```text
densidad_g_cm3 =
    (peso_medido_g × 1000)
    / (ancho_mm × largo_mm × espesor_mm)
```

Ejemplo:

```text
peso medido = 1,60 g

ancho = 10 mm

largo = 20 mm

espesor = 0,5 mm

densidad = (1,60 × 1000) / (10 × 20 × 0,5)

densidad = 16,00 g/cm³
```

Esta calibración solo es fiable si las medidas y el peso se obtienen con instrumentos adecuados y la chapa es homogénea y maciza.

---

## 20. Casos fuera del alcance de la fórmula

La fórmula principal presupone una chapa:

- Rectangular.
- Maciza.
- Homogénea.
- De espesor uniforme.
- Sin huecos, perforaciones, biseles ni relieves.

No aplicar directamente la fórmula sin ajustes a:

- Chapas perforadas.
- Piezas recortadas con formas irregulares.
- Materiales huecos.
- Chapas compuestas por varias capas.
- Piezas con soldaduras, engastes o piedras.
- Materiales chapados, laminados bimetálicos o baños.
- Superficies con espesor variable.

Para una perforación rectangular, por ejemplo:

```text
volumen_neto = volumen_exterior - volumen_hueco

peso = volumen_neto × densidad
```

Para otras geometrías, calcular antes el volumen real y aplicar después la densidad.

---

## 21. Pruebas unitarias

### 21.1. `SheetWeightCalculatorTest.kt`

```kotlin
package com.ejemplo.calculadorajoyeros.domain.sheet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class SheetWeightCalculatorTest {

    private val calculator = SheetWeightCalculator()

    @Test
    fun screenshotExampleFor18kGoldReturns1558Grams() {
        val result = calculator.calculate(
            SheetCalculationInput(
                widthMm = BigDecimal("10"),
                lengthMm = BigDecimal("20"),
                thicknessMm = BigDecimal("0.5"),
                material = SheetMaterial.GOLD_18K
            )
        )

        assertDecimalEquals("1.558", result.totalWeightGrams)
        assertDecimalEquals("1.1685", result.fineMetalWeightGrams)
        assertDecimalEquals("0.3895", result.alloyWeightGrams)
    }

    @Test
    fun fourteenKaratGoldUses585Fineness() {
        val result = calculateReferenceSheet(SheetMaterial.GOLD_14K)

        assertDecimalEquals("1.307", result.totalWeightGrams)
        assertDecimalEquals("0.764595", result.fineMetalWeightGrams)
    }

    @Test
    fun twelveKaratGoldUsesConfiguredDensity() {
        val result = calculateReferenceSheet(SheetMaterial.GOLD_12K)

        assertDecimalEquals("1.275", result.totalWeightGrams)

        assertEquals(
            BigDecimal("1.28"),
            result.totalWeightGrams.setScale(2, RoundingMode.HALF_UP)
        )
    }

    @Test
    fun nineKaratGoldReturns112Grams() {
        val result = calculateReferenceSheet(SheetMaterial.GOLD_9K)

        assertDecimalEquals("1.12", result.totalWeightGrams)
    }

    @Test
    fun sterlingSilverReturns1036Grams() {
        val result = calculateReferenceSheet(SheetMaterial.SILVER_925)

        assertDecimalEquals("1.036", result.totalWeightGrams)
        assertDecimalEquals("0.9583", result.fineMetalWeightGrams)
        assertDecimalEquals("0.0777", result.alloyWeightGrams)
    }

    @Test
    fun customDensityOverridesDefaultDensity() {
        val result = calculator.calculate(
            SheetCalculationInput(
                widthMm = BigDecimal("10"),
                lengthMm = BigDecimal("20"),
                thicknessMm = BigDecimal("0.5"),
                material = SheetMaterial.GOLD_18K,
                customDensityGramsPerCm3 = BigDecimal("16.00")
            )
        )

        assertDecimalEquals("1.60", result.totalWeightGrams)
        assertEquals(true, result.usesCustomDensity)
    }

    @Test
    fun inverseLengthReturnsExpectedValue() {
        val result = calculator.calculateLengthMm(
            targetWeightGrams = BigDecimal("2.5"),
            widthMm = BigDecimal("10"),
            thicknessMm = BigDecimal("0.5"),
            densityGramsPerCm3 = BigDecimal("15.58")
        )

        assertEquals(
            BigDecimal("32.09"),
            result.setScale(2, RoundingMode.HALF_UP)
        )
    }

    @Test
    fun fivePercentMarginIsAddedToFinishedWeight() {
        val result = calculator.addMargin(
            finishedWeightGrams = BigDecimal("1.558"),
            marginPercentage = BigDecimal("5")
        )

        assertDecimalEquals("1.6359", result)
    }

    @Test
    fun rejectsZeroThickness() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.calculate(
                SheetCalculationInput(
                    widthMm = BigDecimal("10"),
                    lengthMm = BigDecimal("20"),
                    thicknessMm = BigDecimal.ZERO,
                    material = SheetMaterial.GOLD_18K
                )
            )
        }
    }

    private fun calculateReferenceSheet(
        material: SheetMaterial
    ): SheetCalculationResult {
        return calculator.calculate(
            SheetCalculationInput(
                widthMm = BigDecimal("10"),
                lengthMm = BigDecimal("20"),
                thicknessMm = BigDecimal("0.5"),
                material = material
            )
        )
    }

    private fun assertDecimalEquals(expected: String, actual: BigDecimal) {
        assertEquals(0, BigDecimal(expected).compareTo(actual))
    }
}
```

### 21.2. `DecimalInputParserTest.kt`

```kotlin
package com.ejemplo.calculadorajoyeros.domain.sheet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class DecimalInputParserTest {

    @Test
    fun parsesSpanishDecimalComma() {
        assertEquals(BigDecimal("0.5"), DecimalInputParser.parsePositive("0,5"))
    }

    @Test
    fun parsesDecimalPoint() {
        assertEquals(BigDecimal("0.5"), DecimalInputParser.parsePositive("0.5"))
    }

    @Test
    fun rejectsZero() {
        assertNull(DecimalInputParser.parsePositive("0"))
    }

    @Test
    fun rejectsRepeatedDecimalSeparators() {
        assertNull(DecimalInputParser.parsePositive("1,2,3"))
    }

    @Test
    fun rejectsNegativeNumbers() {
        assertNull(DecimalInputParser.parsePositive("-1"))
    }
}
```

### 21.3. Casos mínimos de aceptación

```text
10 × 20 × 0,5 mm en oro 18 K → 1,56 g

10 × 20 × 0,5 mm en oro 14 K → 1,31 g

10 × 20 × 0,5 mm en oro 12 K → 1,28 g

10 × 20 × 0,5 mm en oro 9 K → 1,12 g

10 × 20 × 0,5 mm en plata 950 → 1,04 g

10 × 20 × 0,5 mm en plata 925 → 1,04 g

10 × 20 × 0,5 mm en plata 900 → 1,03 g

10 × 20 × 0,5 mm en plata 800 → 1,01 g
```

---

## 22. Instrucción preparada para Claude Code o Codex

```text
Implementa en la aplicación Android existente un módulo llamado "Peso de chapas"
dentro de "Herramientas", manteniendo el estilo visual y la arquitectura actual.

En la cabecera de Herramientas debe existir un selector con dos opciones:
1. Precio metales.
2. Peso de chapas.

La calculadora debe solicitar:
- Ancho en milímetros.
- Largo en milímetros.
- Espesor en milímetros.
- Material de la chapa.

Materiales y densidades iniciales:
- Oro amarillo 18 K, ley 750: 15.58 g/cm³.
- Oro amarillo 14 K, ley 585: 13.07 g/cm³.
- Oro amarillo 12 K, ley 500: 12.75 g/cm³.
- Oro amarillo 9 K, ley 375: 11.20 g/cm³.
- Plata 950: 10.40 g/cm³.
- Plata 925: 10.36 g/cm³.
- Plata 900: 10.31 g/cm³.
- Plata 800: 10.14 g/cm³.

Fórmula obligatoria:

peso_g = ancho_mm × largo_mm × espesor_mm × densidad_g_cm3 / 1000.

Cálculos opcionales:
- Volumen en mm³ y cm³.
- Metal fino = peso × ley / 1000.
- Resto de liga = peso - metal fino.
- Margen de merma configurable.
- Densidad personalizada por material.
- Coste estimado si existen cotizaciones en euros por gramo.

Requisitos técnicos:
- Utiliza BigDecimal para las operaciones.
- Acepta coma y punto decimal.
- Redondea el resultado visible a dos decimales con HALF_UP.
- No redondees prematuramente los cálculos internos.
- Usa ViewModel y StateFlow respetando la arquitectura existente.
- Integra los resultados en Jetpack Compose.
- No dupliques dependencias ni reemplaces la navegación actual.
- Añade pruebas unitarias para todos los materiales.
- Añade pruebas para densidad personalizada y entradas no válidas.
- No confundas peso total de la chapa con peso de oro o plata fina.
- No presentes plata 950/900 ni oro de 12 K como leyes comerciales españolas
  enumeradas en el artículo 9 de la Ley 17/1985.
- Trata todas las densidades como orientativas y configurables.

Caso obligatorio de validación:
10 mm × 20 mm × 0.5 mm en oro amarillo 18 K = 1.558 g,
que debe mostrarse como 1,56 g.
```

---

## 23. Fuentes y referencias

- [BOE: Ley 17/1985 sobre objetos fabricados con metales preciosos](https://www.boe.es/buscar/act.php?id=BOE-A-1985-12768).
- [Maguire Refining: Physical Properties of Various Metals and Alloys](https://www.maguireref.com/wp-content/uploads/2012/03/Physical-Properties.pdf).
- [Regal Castings: Alloy Density Table](https://www.regal.co.nz/pages/Alloy-Density-Table).
- [ESPI Metals: Silver - Sterling](https://www.espimetals.com/index.php/technical-data/81-Silver%20-%20Sterling).
- [London Bullion Market Association: Density of Gold](https://www.lbma.org.uk/wonders-of-gold/items/density-of-gold).
- [Newman Numismatic Portal: Specific Gravity](https://nnp.wustl.edu/library/dictionarydetail/516784).
- [United Precious Metal Refining: 9K–14K Yellow Gold Fabrication Master Alloys](https://www.unitedpmr.com/master-alloys-for-fabrication/9k-14k-yellow-gold-fabrication-master-alloys/).

La densidad definitiva para una aleación concreta debe obtenerse de su ficha técnica o de una medición calibrada. Las tablas orientativas permiten estimaciones de taller; no sustituyen el ensayo, contraste, pesaje real ni asesoramiento normativo específico.
