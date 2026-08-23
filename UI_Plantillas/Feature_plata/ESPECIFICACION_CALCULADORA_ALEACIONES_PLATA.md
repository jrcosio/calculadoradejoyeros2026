# Especificación técnica — Calculadora de aleaciones de plata

**Versión:** 1.0  
**Fecha:** 2026-08-23  
**Objetivo:** especificación para implementación mediante Claude Code, Codex o equipo de desarrollo.

---

# 1. Objetivo

Crear una calculadora de joyería que permita obtener distintas leyes de plata a partir de **plata fina 999‰**, añadiendo **cobre fino (Cu)** como metal de liga.

La aplicación debe permitir seleccionar:

- Plata 950‰
- Plata 925‰
- Plata 900‰
- Plata 800‰

El usuario introduce una cantidad de **plata 999‰** y la calculadora debe indicar exactamente cuánto cobre debe añadir.

---

# 2. Base metalúrgica

La aleación tradicional de plata de joyería se realiza con plata y cobre.

La plata Sterling tradicional es:

```text
92,5 % Ag
7,5 % Cu
```

El cobre aumenta la dureza y resistencia mecánica respecto de la plata fina.

Para esta calculadora se define:

```text
Metal precioso = Ag
Metal de liga = Cu
```

No añadir automáticamente:

- zinc;
- germanio;
- estaño;
- níquel;
- otros metales.

Existen aleaciones comerciales especiales que utilizan otros elementos, pero quedan fuera del alcance de esta calculadora.

---

# 3. Legislación española

La Ley 17/1985 vigente establece actualmente para plata las siguientes leyes:

```text
999‰
925‰
800‰
```

Y establece que:

```text
No se admite tolerancia en menos.
```

Por tanto:

| Selección calculadora | Situación |
|---|---|
| 999‰ | Ley reconocida en España |
| 950‰ | Preset técnico de mayor finura; no es punzón oficial español 950 |
| 925‰ | Ley oficial española |
| 900‰ | Preset técnico; no es punzón oficial español 900 |
| 800‰ | Ley oficial española |

### Tratamiento en interfaz

Para `950‰` mostrar:

> Plata 950‰: composición técnica. En España 950‰ no es una de las leyes oficiales de contraste de plata; supera la ley 925‰.

Para `900‰` mostrar:

> Plata 900‰: composición técnica. En España 900‰ no es una de las leyes oficiales de contraste de plata; supera la ley 800‰ pero no alcanza 925‰.

No mostrar 950 ni 900 como "ley oficial española".

### Referencias legales

Ley 17/1985, art. 9:

https://www.boe.es/buscar/act.php?id=BOE-A-1985-12768

Real Decreto 197/1988:

https://www.boe.es/buscar/act.php?id=BOE-A-1988-6186

---

# 4. Pureza de origen

La calculadora parte de:

```text
PLATA_ORIGEN = 999‰
PUREZA_ORIGEN = 0.999
```

Esto es crítico.

No tratar Ag999 como si fuese Ag1000.

Por ejemplo:

```text
100.000 g de Ag999
```

contienen realmente:

```text
99.900 g de plata pura
```

---

# 5. Fórmula matemática principal

Variables:

```text
m_source = masa inicial de plata 999, en gramos
F_source = 0.999
F_target = finura objetivo en decimal
```

Objetivos:

```text
950‰ = 0.950
925‰ = 0.925
900‰ = 0.900
800‰ = 0.800
```

## 5.1 Plata pura contenida

```text
pureSilver = m_source * F_source
```

## 5.2 Masa final

```text
finalMass = pureSilver / F_target
```

## 5.3 Cobre que hay que añadir

Al ser el cobre el único metal de liga:

```text
copperToAdd = finalMass - m_source
```

Forma compacta:

```text
copperToAdd =
    m_source * ((F_source / F_target) - 1)
```

---

# 6. Coeficientes exactos desde Ag999

Por cada gramo de Ag999:

| Objetivo | Coeficiente Cu |
|---|---:|
| 950‰ | 0.0515789473684210526 |
| 925‰ | 0.0800000000000000000 |
| 900‰ | 0.1100000000000000000 |
| 800‰ | 0.2487500000000000000 |

Por tanto:

```text
Cu = Ag999 * coeficiente
```

---

# 7. Tabla de taller — por cada 100 g de Ag999

| Ley objetivo | Ag999 inicial | Cu a añadir | Masa final |
|---|---:|---:|---:|
| 950‰ | 100.000000 g | 5.157894737 g | 105.157894737 g |
| 925‰ | 100.000000 g | 8.000000000 g | 108.000000000 g |
| 900‰ | 100.000000 g | 11.000000000 g | 111.000000000 g |
| 800‰ | 100.000000 g | 24.875000000 g | 124.875000000 g |

---

# 8. Presets de la aplicación

```json
{
  "source": {
    "metal": "Ag",
    "fineness": 999
  },
  "alloy_metal": "Cu",
  "targets": {
    "950": {
      "fineness": 0.950,
      "official_spain": false
    },
    "925": {
      "fineness": 0.925,
      "official_spain": true
    },
    "900": {
      "fineness": 0.900,
      "official_spain": false
    },
    "800": {
      "fineness": 0.800,
      "official_spain": true
    }
  }
}
```

No guardar los gramos de cobre como tablas fijas.

Los gramos deben calcularse mediante la fórmula general.

---

# 9. Algoritmo obligatorio

```text
function calculateSilverAlloy(
    sourceMass,
    sourceFineness,
    targetFineness
):
    validate(sourceMass > 0)

    validate(
        sourceFineness > targetFineness
    )

    pureSilver =
        sourceMass * sourceFineness

    finalMass =
        pureSilver / targetFineness

    copperToAdd =
        finalMass - sourceMass

    calculatedFineness =
        pureSilver / finalMass

    return {
        sourceMass,
        sourceFineness,
        pureSilver,
        targetFineness,
        copperToAdd,
        finalMass,
        calculatedFineness
    }
```

---

# 10. Ejemplo — Plata 925

Entrada:

```text
Ag999 = 100.000 g
Objetivo = 925‰
```

Plata pura real:

```text
100 * 0.999
= 99.900 g
```

Masa final necesaria:

```text
99.9 / 0.925
= 108.000 g
```

Cobre:

```text
108.000 - 100.000
= 8.000 g
```

Resultado:

```text
100.000 g Ag999
+ 8.000 g Cu
----------------
108.000 g plata 925
```

Comprobación:

```text
99.900 / 108.000
= 0.925
= 925‰
```

---

# 11. Ejemplo — Plata 950

Entrada:

```text
Ag999 = 100.000 g
Objetivo = 950‰
```

Plata pura:

```text
99.900 g
```

Masa final:

```text
99.900 / 0.950
= 105.157894736842...
```

Cobre:

```text
5.157894736842... g
```

Resultado matemático exacto:

```text
100.000000000 g Ag999
+ 5.157894737 g Cu
-------------------
105.157894737 g
```

---

# 12. Ejemplo — Plata 900

Entrada:

```text
Ag999 = 100.000 g
Objetivo = 900‰
```

Resultado:

```text
100.000 g Ag999
+ 11.000 g Cu
--------------
111.000 g
```

Comprobación:

```text
99.9 / 111
= 0.900
```

---

# 13. Ejemplo — Plata 800

Entrada:

```text
Ag999 = 100.000 g
Objetivo = 800‰
```

Resultado:

```text
100.000 g Ag999
+ 24.875 g Cu
---------------
124.875 g
```

Comprobación:

```text
99.9 / 124.875
= 0.800
```

---

# 14. Regla crítica de precisión

Nunca redondear los cálculos internos.

Incorrecto:

```text
copper = round(copperExact, 2)
finalMass = source + copper
```

Correcto:

```text
copperExact = ...
finalMassExact = ...

displayCopper =
    format(copperExact)
```

El cálculo interno debe conservar toda la precisión disponible.

---

# 15. Usar Decimal / BigDecimal

Recomendado:

- Python → `Decimal`
- Kotlin / Java → `BigDecimal`
- JavaScript / TypeScript → `decimal.js` o equivalente
- Dart → implementación decimal equivalente

No depender de errores de coma flotante binaria para una calculadora de metales preciosos.

---

# 16. Regla MUY IMPORTANTE de redondeo para uso de taller

La legislación española no admite tolerancia en menos.

Esto implica que **añadir demasiado cobre puede hacer que la plata quede por debajo de la ley objetivo**.

Ejemplo:

```text
100 g Ag999 → 950‰

Cu exacto:
5.157894736... g
```

Si la balanza tiene resolución de:

```text
0.001 g
```

el redondeo matemático convencional produciría:

```text
5.158 g Cu
```

Esto añade ligeramente más cobre del necesario y matemáticamente produce una finura ligeramente inferior a 950‰.

Para un modo denominado:

```text
MODO TALLER SEGURO
```

la cantidad de cobre mostrada debe truncarse / redondearse hacia abajo a la resolución de la balanza:

```text
5.157 g Cu
```

Esto deja la ley ligeramente por encima de 950‰, nunca por debajo debido al redondeo.

---

# 17. Función de redondeo seguro

Conceptualmente:

```text
function safeCopperForScale(
    copperExact,
    scaleResolution
):
    return floor(
        copperExact / scaleResolution
    ) * scaleResolution
```

Ejemplo:

```text
copperExact = 5.157894736
resolution = 0.001

safeCopper = 5.157
```

---

# 18. Resolución configurable de balanza

La aplicación debería permitir seleccionar:

```text
0.1 g
0.01 g
0.001 g
0.0001 g
```

Valor recomendado por defecto:

```text
0.001 g
```

Mostrar dos valores:

```text
Cu teórico:
5.157895 g

Cu recomendado según balanza 0.001 g:
5.157 g
```

Después recalcular la ley real esperada con la cantidad práctica:

```text
practicalFinalMass =
    sourceMass + safeCopper

practicalFineness =
    pureSilver / practicalFinalMass
```

---

# 19. Salida recomendada

Ejemplo:

```text
PLATA 950‰

Plata de partida
100.000 g Ag999

Plata pura contenida
99.900 g

Añadir Cu teórico
5.157895 g

Añadir con balanza 0.001 g
5.157 g

Peso final práctico
105.157 g

Ley teórica práctica
950.008‰
```

La interfaz debe diferenciar claramente:

```text
VALOR TEÓRICO
```

de:

```text
VALOR PARA PESADA
```

---

# 20. Cálculo de ley práctica

```text
practicalFineness =
    pureSilver
    /
    (sourceMass + copperPractical)
```

En milésimas:

```text
practicalFinenessPerMille =
    practicalFineness * 1000
```

Debe verificarse:

```text
practicalFineness >= targetFineness
```

cuando se utilice el modo de redondeo seguro.

---

# 21. Casos de prueba obligatorios

## Test A — 10 g Ag999 → 950

Entrada:

```text
sourceMass = 10
sourceFineness = 0.999
target = 0.950
```

Esperado:

```text
pureSilver =
9.990000000 g

copperExact =
0.5157894736842105... g

finalMass =
10.5157894736842105... g
```

Con balanza 0.001:

```text
copperSafe =
0.515 g
```

La finura práctica debe ser:

```text
> 950‰
```

---

## Test B — 10 g Ag999 → 925

Esperado:

```text
pureSilver = 9.990 g
Cu = 0.800 g
finalMass = 10.800 g
fineness = 925‰
```

---

## Test C — 10 g Ag999 → 900

Esperado:

```text
Cu = 1.100 g
finalMass = 11.100 g
fineness = 900‰
```

---

## Test D — 10 g Ag999 → 800

Esperado:

```text
Cu = 2.487500 g
finalMass = 12.487500 g
fineness = 800‰
```

Con balanza 0.001:

```text
Cu seguro =
2.487 g
```

y:

```text
fineness práctica > 800‰
```

---

# 22. Modo inverso

La calculadora debería soportar también:

> Quiero obtener X gramos finales de plata de una ley concreta.

Variables:

```text
desiredFinalMass
sourceFineness
targetFineness
```

Ag999 necesario:

```text
requiredSourceMass =
    desiredFinalMass
    * targetFineness
    / sourceFineness
```

Cobre:

```text
copperToAdd =
    desiredFinalMass
    - requiredSourceMass
```

---

# 23. Ejemplo inverso

Objetivo:

```text
100.000 g de plata 925‰
```

Origen:

```text
Ag999
```

Cálculo:

```text
Ag999 =
100 * 0.925 / 0.999

Ag999 =
92.5925925926... g
```

Cobre:

```text
100
- 92.5925925926

= 7.4074074074... g
```

Resultado:

```text
92.592592593 g Ag999
+ 7.407407407 g Cu
-------------------
100.000000000 g plata 925
```

---

# 24. Modelo de datos recomendado

```json
{
  "calculator": "silver_alloy",
  "version": "1.0",
  "source": {
    "metal": "Ag",
    "fineness": 999
  },
  "ligature": {
    "metal": "Cu"
  },
  "targets": [
    {
      "fineness": 950,
      "official_spain": false,
      "technical_preset": true
    },
    {
      "fineness": 925,
      "official_spain": true,
      "technical_preset": false
    },
    {
      "fineness": 900,
      "official_spain": false,
      "technical_preset": true
    },
    {
      "fineness": 800,
      "official_spain": true,
      "technical_preset": false
    }
  ]
}
```

---

# 25. Funciones mínimas

```text
calculateFromSourceMass(...)
calculateFromDesiredFinalMass(...)
calculatePracticalFineness(...)
safeRoundLigatureDown(...)
validateInput(...)
formatResult(...)
```

---

# 26. Validaciones

Rechazar:

```text
masa <= 0
NaN
Infinity
campos vacíos
target >= sourceFineness
target <= 0
resolución de balanza <= 0
```

Aceptar coma y punto decimal:

```text
12,35
12.35
```

Normalizar internamente.

---

# 27. Arquitectura

Separar:

```text
UI
↓
Silver Calculator Use Case
↓
Silver Alloy Engine
↓
Legal/Target Metadata
```

El motor matemático no debe conocer:

- Android;
- Flutter;
- Compose;
- SwiftUI;
- React;
- idioma;
- diseño visual.

---

# 28. No codificar fórmulas individuales innecesarias

No hacer:

```text
if target == 925:
    copper = source * 0.08

if target == 900:
    copper = source * 0.11
```

Aunque produzca el mismo resultado.

Implementar la fórmula general:

```text
copper =
    source *
    ((sourceFineness / targetFineness) - 1)
```

De este modo el motor podrá soportar futuras leyes sin modificar la lógica.

---

# 29. Extensiones futuras

Diseñar para poder incorporar más adelante:

- Ag999.9;
- plata reciclada de ley conocida;
- mezcla de dos platas de distinta ley;
- subida de ley añadiendo plata fina;
- bajada de ley;
- cálculo de ley desconocida de una mezcla;
- cobre con pureza configurable;
- merma de fundición;
- soldaduras de plata;
- aleaciones con germanio;
- Argentium;
- aleaciones anti-firescale;
- zinc;
- costes de metales;
- precio final;
- densidad;
- conversión gramos / onzas / pennyweight.

No implementar salvo petición expresa.

---

# 30. Fuente técnica de la aleación Ag-Cu

Rio Grande describe la Sterling tradicional como:

```text
92.5 % Ag
7.5 % Cu
```

y explica que la adición de cobre aumenta dureza y resistencia respecto a la plata fina.

Referencia:

https://www.riogrande.com/knowledge-hub/articles/the-rio-grande-guide-to-types-of-silver-metals-for-jewelry/

También:

https://products.riogrande.com/content/Instruction-Sheets/PMC-Sterling-Insert.pdf

---

# 31. Nota sobre 950 y 900

Las composiciones:

```text
950‰ Ag
900‰ Ag
```

son perfectamente calculables y fabricables como aleaciones Ag-Cu.

Sin embargo, dentro de esta aplicación:

```text
950 = preset técnico
900 = preset técnico
```

porque las leyes oficiales españolas de plata recogidas en la Ley 17/1985 son:

```text
999
925
800
```

La aplicación no debe confundir:

```text
composición metalúrgica
```

con:

```text
ley oficial española de contraste
```

---

# 32. Criterios de aceptación

- [ ] Permite introducir gramos de Ag999.
- [ ] Permite seleccionar 950, 925, 900 y 800‰.
- [ ] Utiliza 0.999 como pureza real del metal de origen.
- [ ] Calcula el cobre mediante fórmula general.
- [ ] Muestra plata pura real contenida.
- [ ] Muestra Cu exacto.
- [ ] Muestra masa final.
- [ ] Recalcula la ley obtenida.
- [ ] Identifica 925 y 800 como leyes oficiales españolas.
- [ ] Identifica 950 y 900 como presets técnicos.
- [ ] Utiliza aritmética decimal de alta precisión.
- [ ] No redondea cálculos intermedios.
- [ ] Implementa modo de redondeo seguro hacia abajo para el cobre.
- [ ] Permite configurar resolución de balanza.
- [ ] Comprueba que la ley práctica no quede por debajo del objetivo por redondeo.
- [ ] Incluye tests unitarios.
- [ ] Mantiene lógica y UI desacopladas.

---

# 33. Instrucción directa para Claude Code / Codex

Implementa esta calculadora como un motor matemático reutilizable.

La regla principal es:

```text
Cu =
Ag999 *
(
    0.999 / targetFineness
    - 1
)
```

donde `targetFineness` se expresa en decimal.

Ejemplo:

```text
925‰ → 0.925
```

Prioridades:

1. exactitud;
2. cálculo a partir de Ag999 real, no Ag1000;
3. no introducir por redondeo una finura inferior al objetivo;
4. usar Decimal/BigDecimal;
5. diferenciar cálculo teórico y cantidad práctica de pesada;
6. permitir balanzas con diferentes resoluciones;
7. mantener el motor independiente de la UI;
8. tests automáticos;
9. no inventar otras aleaciones.

Para esta versión, la liga es exclusivamente:

```text
Cu
```

No sustituir ni complementar el cobre con otros metales salvo modificación expresa de la especificación.
