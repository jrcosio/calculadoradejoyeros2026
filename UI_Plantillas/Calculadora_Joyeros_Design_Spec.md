# Calculadora de Joyeros — Design Spec para Claude Code / Codex

Este documento describe el sistema visual y de componentes de la app **Calculadora de Joyeros** para que un agente de código (Claude Code, Codex, Cursor, etc.) pueda reconstruir la interfaz con fidelidad.

---

## 1. Objetivo del diseño

Crear una app Android con estética **premium, moderna y elegante**, orientada a talleres de joyería.

### Personalidad visual
- **Dark luxury UI**
- Inspiración: herramientas de joyero, precisión, metal noble, taller premium
- Sensación: **profesional, limpia, técnica, exclusiva**
- Evitar: interfaz infantil, colores chillones, excesivo skeuomorphism, saturación visual

### Conceptos clave
- Oro = valor / metal noble / acción principal
- Azul marino oscuro = base elegante y tecnológica
- Plata / acero = herramientas / precisión / metal neutro
- Turquesa = acento secundario para utilidades concretas

---

## 2. Paleta de colores

Usar esta paleta como referencia principal.

### Colores base
| Token | Hex | Uso |
|---|---|---|
| `background` | `#071018` | fondo general de pantallas |
| `surface` | `#101921` | tarjetas y superficies base |
| `surfaceElevated` | `#17212A` | bloques destacados o contenedores elevados |
| `surfaceWarm` | `#241A0E` | fondos cálidos relacionados con oro |

### Colores principales
| Token | Hex | Uso |
|---|---|---|
| `goldPrimary` | `#F4BD45` | color principal, CTA, highlights |
| `goldSecondary` | `#C98B17` | sombras, bordes, detalles dorados |
| `goldSoft` | `#E8C36B` | dorado suave para brillo o rellenos sutiles |
| `silverPrimary` | `#C7CDD2` | herramientas, elementos metálicos neutros |
| `silverDark` | `#707980` | texto o detalles secundarios metálicos |
| `tealPrimary` | `#14B8B8` | acento secundario para utilidades/herramientas |
| `tealDark` | `#087D82` | bordes o sombras del teal |

### Texto
| Token | Hex | Uso |
|---|---|---|
| `textPrimary` | `#F7F7F5` | títulos y texto principal |
| `textSecondary` | `#B8BEC3` | descripciones y texto de apoyo |
| `textMuted` | `#808990` | texto desactivado o de baja jerarquía |

### Bordes / estados
| Token | Hex | Uso |
|---|---|---|
| `border` | `#5D6870` | borde neutro |
| `borderGold` | `#D9A22D` | borde premium / foco dorado |
| `borderTeal` | `#198F91` | borde para utilidades |
| `success` | `#48B68A` | feedback positivo |
| `warning` | `#F4BD45` | advertencia suave |
| `danger` | `#E45454` | error |

---

## 3. Tipografía

### Estilo
- Recomendada: **Manrope**
- Alternativa: **Inter**
- Fallback Android: **Roboto**

### Escala tipográfica
| Token | Tamaño | Peso | Uso |
|---|---:|---:|---|
| `displayLarge` | 32sp | 700 | títulos principales muy destacados |
| `titleLarge` | 24sp | 700 | títulos de pantalla |
| `titleMedium` | 20sp | 700 | títulos de tarjetas o secciones |
| `bodyLarge` | 16sp | 400 | texto principal |
| `bodyMedium` | 14sp | 400 | descripciones |
| `labelMedium` | 12sp | 600 | botones, etiquetas, navegación |

### Reglas tipográficas
- Títulos siempre claros y muy legibles
- Evitar fuentes decorativas
- El color de títulos suele ser `textPrimary`
- Descripciones: `textSecondary`
- En botones o acciones premium, permitir acento en `goldPrimary`

---

## 4. Espaciado y geometría

### Radios
| Token | Valor |
|---|---:|
| `radiusSmall` | 12dp |
| `radiusMedium` | 18dp |
| `radiusLarge` | 28dp |
| `radiusExtraLarge` | 34dp |
| `radiusPill` | 999dp |

### Espaciado
| Token | Valor |
|---|---:|
| `spaceXs` | 4dp |
| `spaceSm` | 8dp |
| `spaceMd` | 12dp |
| `spaceLg` | 16dp |
| `spaceXl` | 24dp |
| `spaceXxl` | 32dp |

### Reglas
- Diseño con aire: no compactar demasiado
- Tarjetas con padding interno mínimo de `20dp`
- Separación vertical entre tarjetas: `16dp` o `20dp`
- Botones y elementos táctiles: mínimo `48dp` de altura o área táctil

---

## 5. Sombras, volumen y acabado

### Sombras
- Sombras suaves y elegantes, nunca agresivas
- Preferir elevación visual por contraste y borde antes que sombras muy oscuras

### Estilo material
- UI moderna con un leve toque premium 3D
- Componentes planos + detalles metálicos o bordes dorados
- No hacer estilo glassmorphism
- No usar neón exagerado

### Texturas
- Fondo: oscuro, elegante, casi mate
- Elementos metálicos: brillo controlado
- En iconos / assets hero se permite acabado metal cepillado o pulido

---

## 6. Estilo general de componentes

### Filosofía
Los componentes deben parecer de una app actual Android premium, cercana a **Material 3**, pero con identidad de marca propia.

### Características generales
- Bordes redondeados grandes
- Fondo oscuro
- Toques metálicos en dorado / plata
- Jerarquía visual clara
- Acciones importantes destacadas en dorado

---

## 7. Componentes principales

### 7.1 App Bar / Header
**Objetivo:** mostrar branding sin sobrecargar.

**Características**
- Fondo: `background` o `surface`
- Altura: estándar Material 3
- Título: `titleLarge`
- Puede incluir logo a la izquierda
- Acciones a la derecha: iconos simples, limpios

**No hacer**
- Header demasiado alto
- Mucho texto en la cabecera

---

### 7.2 Tarjetas de menú principal
Estas son las tarjetas más importantes de la home.

**Estructura ideal**
- Tarjeta horizontal o vertical grande
- Icono/imagen a la izquierda o arriba
- Título fuerte
- Subtítulo descriptivo
- Flecha o CTA a la derecha

**Estilo**
- Fondo: `surface` o `surfaceElevated`
- Radio: `radiusLarge` o `radiusExtraLarge`
- Sombra suave
- Iconografía metálica o recurso visual premium
- CTA en dorado

**Ejemplo de secciones**
- Aleaciones de Oro
- Aleaciones de Plata
- Soldaduras
- Precio del Oro y la Plata
- Herramientas

**Comportamiento visual**
- `pressed`: ligera elevación o overlay
- `selected`: borde dorado o resalte

---

### 7.3 Bottom Navigation
**Necesidad:** la app debe tener barra de navegación inferior moderna.

**Características**
- Altura: `88dp`
- Fondo: `surface` o un tono cercano a `background`
- Iconos simples, legibles
- Label debajo del icono
- Item activo: dorado
- Item inactivo: `textMuted`

**Secciones sugeridas**
- Inicio
- Oro
- Plata
- Soldaduras
- Precios
- Utilidades / Herramientas

**Estilo**
- Minimalista
- Muy limpio
- Sin exceso de contornos

---

### 7.4 Botones
#### Botón primario
- Fondo: `goldPrimary`
- Texto: oscuro o `background`
- Radio: `radiusPill` o `radiusLarge`
- Uso: acciones principales

#### Botón secundario
- Fondo: `surfaceElevated`
- Borde: `borderGold` o `border`
- Texto: `textPrimary`

#### Botón de utilidad
- Puede usar `tealPrimary`
- Reservado para módulos de herramientas o utilidades específicas

---

### 7.5 Inputs / campos numéricos
Pensados para introducir gramos, quilates, milésimas, etc.

**Estilo**
- Fondo oscuro o cálido suave según sección
- Label clara
- Borde fino
- Estado focus: dorado o teal según contexto
- Número grande y legible

**Comportamiento**
- Teclado numérico
- Formato amigable
- Validación visible

---

### 7.6 Chips / Tabs / Segmented controls
Muy útiles para:
- tipo de oro: 18K / 14K / 12K / 9K
- color del oro: amarillo / blanco / rosa
- milésimas de plata
- tipo de soldadura

**Estilo**
- Forma redondeada
- Contenedor oscuro
- Opción seleccionada con relleno dorado o contraste fuerte
- Texto centrado y claro

---

### 7.7 Result cards
Tarjetas donde se muestran cantidades calculadas.

**Ejemplo**
- Plata fina: `2,70 gr`
- Cobre: `6,59 gr`
- Paladio: `7,37 gr`

**Estilo**
- Número muy grande
- Unidad `gr` secundaria
- Puede incluir icono del metal
- Fondo: oscuro
- Borde/acento según metal

---

## 8. Iconografía

### Estilo de iconos
- Minimalista premium
- Grosor limpio y consistente
- Inspiración técnica / taller / joyería
- Mejor si combina líneas finas con detalles metálicos

### Temas de iconos
- Oro: lingote `Au`
- Plata: lingote `Ag`
- Cobre: `Cu`
- Cadmio: `Cd`
- Zinc: `Zn`
- Paladio: `Pd`
- Soldaduras: soplete, llama, anillo soldándose
- Herramientas: calibre, lupa, pinzas, útil de taller
- Precios: globo/mercado, metales, gráfico discreto

### Regla importante
Para los lingotes, las letras deben aparecer **en bajo relieve** y con estilo elegante.

---

## 9. Imaginería / assets visuales

### Estilo general
- Fondos transparentes reales en PNG para assets principales
- Aspecto HD / nítido
- Iluminación cuidada
- Enfoque producto / herramienta premium

### Assets principales definidos en el proyecto
- Lingote de oro aislado
- Teclado calculadora cuadrado aislado
- Soplete de joyero aislado
- Anillo soldándose sobre ladrillo refractario
- Herramientas aisladas (calibre digital, pinzas, lupa, útil)

### Uso
- Home y tarjetas hero
- Secciones destacadas
- Logo/app icon

---

## 10. Logo / App icon

### Concepto aprobado
El logo/icono combina:
- referencia al **oro** (lingote)
- referencia a **calculadora** (teclado cuadrado con símbolos matemáticos)
- referencia a **herramientas de joyero** (calibre de fondo con estilo)
- referencia sutil a joyería (anillo de línea fina)

### Estilo visual
- App icon cuadrado con esquinas redondeadas
- Fondo azul marino oscuro
- Marco dorado premium
- Lingote dorado principal
- Calculadora cuadrada en dorado/azul
- Calibre lineal de fondo con estilo minimalista
- Todo debe verse perfecto como icono de app
- Fondo transparente real si se requiere exportación del recurso aislado

### Reglas
- No sobrecargar
- Mantener lectura clara incluso a pequeño tamaño
- El teclado matemático debe ser **cuadrado**

---

## 11. Pantalla principal (Home)

### Objetivo
Presentar de forma clara los módulos de la app.

### Layout recomendado
1. App bar con branding
2. Sección hero o lista principal de tarjetas
3. Tarjetas de acceso a módulos
4. Bottom navigation fija

### Orden recomendado de módulos
1. Oro
2. Plata
3. Soldaduras
4. Precios
5. Herramientas

### Home moderna actual
- Cards grandes con imagen premium
- Descripción corta
- CTA implícito con flecha
- Mucho orden, mucho aire

---

## 12. Pantallas calculadora / formularios

### Patrones
- Un bloque superior con input principal
- Un bloque de selección de variantes (chips)
- Resultados en tarjetas inferiores
- CTA inferior si hace falta

### Ejemplo: aleaciones de oro
- Input: gramos de oro 24K
- Selector: 18K / 14K / 12K / 9K
- Selector: amarillo / blanco / rosa
- Resultados: plata, cobre, paladio, etc.

### Ejemplo: plata
- Input: gramos plata fina 999
- Selector: 950 / 925 / 900 / 800
- Resultado: cobre necesario

### Ejemplo: soldaduras
- Selector tipo: oro ley / clásica / plata
- Tipo de soldadura: muy floja / floja / media / fuerte
- Resultado: soldadura base necesaria

---

## 13. Accesibilidad y UX

### Reglas UX
- Contraste alto
- Números muy legibles
- Objetivos táctiles amplios
- Jerarquía clara
- Evitar ambigüedad en la navegación

### Accesibilidad
- Labels claras
- No depender solo del color
- Iconos acompañados de texto en navegación
- Buen tamaño tipográfico

---

## 14. Reglas de implementación para Claude Code / Codex

### Si se implementa en Android Jetpack Compose
Usar:
- `MaterialTheme` personalizado
- `darkColorScheme()`
- `RoundedCornerShape` grandes
- `NavigationBar` para la barra inferior
- `Card` y `ElevatedCard` personalizadas
- `OutlinedTextField` o campos custom para inputs numéricos

### Prioridades de implementación
1. Crear **theme** y tokens de diseño
2. Crear componentes reutilizables
3. Construir home
4. Construir calculadoras
5. Integrar assets PNG
6. Refinar interacciones visuales

### Convención sugerida de componentes
- `JewelryAppTheme`
- `HomeModuleCard`
- `MetalResultCard`
- `SegmentedSelector`
- `BottomNavBar`
- `GoldInputCard`
- `ToolsHeroCard`

---

## 15. JSON/Tokens rápidos para el agente

```json
{
  "background": "#071018",
  "surface": "#101921",
  "surfaceElevated": "#17212A",
  "goldPrimary": "#F4BD45",
  "goldSecondary": "#C98B17",
  "silverPrimary": "#C7CDD2",
  "tealPrimary": "#14B8B8",
  "textPrimary": "#F7F7F5",
  "textSecondary": "#B8BEC3",
  "border": "#5D6870",
  "borderGold": "#D9A22D",
  "radiusLarge": 28,
  "radiusExtraLarge": 34,
  "spaceLg": 16,
  "spaceXl": 24,
  "bottomNavHeight": 88
}
```

---

## 16. Prompt operativo para un agente de código

Puedes darle a Claude Code o Codex una instrucción como esta:

> Implementa una app Android Jetpack Compose llamada “Calculadora de Joyeros” usando un sistema visual dark luxury. Usa fondo `#071018`, superficies oscuras `#101921` y acentos dorados `#F4BD45`. Crea una home con tarjetas premium para Oro, Plata, Soldaduras, Precios y Herramientas, además de bottom navigation. Usa bordes redondeados grandes, tipografía Manrope/Roboto, y componentes modernos estilo Material 3 personalizados. Integra imágenes PNG de lingotes, soplete, anillo soldándose y herramientas con fondo transparente. El branding debe sentirse elegante, técnico y orientado a joyería profesional.

---

## 17. Resumen ejecutivo

Este sistema visual debe comunicar:
- **precisión**
- **lujo**
- **tecnología**
- **artesanía joyera**

La app no debe parecer una simple calculadora genérica, sino una herramienta profesional para talleres de joyería con una identidad cuidada y moderna.

