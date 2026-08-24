# Implementation Plan: Calculadora de soldaduras de joyería

**Branch**: `006-soldaduras-joyeria` | **Date**: 2026-08-24 | **Spec**: [spec.md](./spec.md)

## Summary

`Route.Soldaduras` deja de caer en `PlaceholderScreen` y pasa a la calculadora real: un
selector de familia (ORO LEY / CLÁSICA / PLATA) que en la primera visita se muestra solo,
y debajo el formulario de la familia elegida, con conmutador de modo («tengo el metal» /
«peso final deseado»), cálculo reactivo, filas de resultado, total teórico, advertencias
de seguridad de cadmio/zinc y los botones «Limpiar» y «Guardar en favoritos». ORO LEY
añade una segunda pantalla nueva, la de la **soldadura BASE** (oro 24K + cobre + plata +
zinc + cadmio), con el proceso de taller y su propia calculadora en dos modos.

Es la tercera calculadora del proyecto y estrena el **tercer motor** de `domain/`, fiel a
su propio documento técnico: tres recetas clásicas escalables, la receta de la base, los
factores de latón de plata y los factores de oro por dureza, todo centralizado en un
objeto de recetas (patrón `RecetasOro`) y repartido en pares de casos de uso
directo/inverso (patrón oro/plata). En UI reutiliza entero el catálogo de
`ui/components/` — el único cambio compartido es un parámetro opcional nuevo en
`SelectorSegmentado` para partir las 5 durezas en dos filas.

La lógica de negocio es transcripción del documento técnico
`UI_Plantillas/Feature_Soldadura/ESPECIFICACION_CALCULADORA_SOLDADURAS_JOYERIA.md`
(§ citados desde aquí), que **prevalece sobre los mockups** (§12): en particular la base
es cobre 0,54 / plata 0,80 / zinc 0,92 / cadmio 1,00 por 10 g de oro 24K, aunque el
mockup muestre esos valores intercambiados.

## Technical Context

**Language/Version**: Kotlin 2.2.10 (el que fija AGP 9.3.1) · Java 17

**Primary Dependencies**: Compose BOM 2026.08.00, Material 3, Navigation Compose con
rutas `@Serializable`, Koin 4.2.2, Firebase BoM (Analytics + Crashlytics);
`java.math.BigDecimal` del JDK para el motor

**Storage**: N/A — nada se persiste; las pantallas arrancan limpias en cada visita

**Testing**: JUnit4 + MockK + Turbine + `kotlinx-coroutines-test` (JVM);
`compose-ui-test-junit4` (instrumentado)

**Target Platform**: Android, minSdk 24 / targetSdk 36 / compileSdk 37, solo vertical

**Project Type**: app Android de un solo módulo (`:app`), MVVM `ui → domain ← data`

**Performance Goals**: recálculo por pulsación de tecla en el hilo principal sin jank —
unas pocas operaciones `BigDecimal`, muy por debajo de un frame

**Constraints**: sin dependencias nuevas; sin `material-icons`; precisión interna completa
sin redondeos intermedios (§2.1, §8.1); una única división por cálculo; los ingredientes
mostrados jamás se ajustan para cuadrar la suma (§8.3); textos traducibles; advertencia
de seguridad obligatoria con cadmio/zinc (§9)

**Scale/Scope**: dos pantallas nuevas, un motor con 3 familias (9 modelos, 9 casos de
uso), 1 componente compartido ampliado, 5 imágenes nuevas, 0 iconos vectoriales nuevos

**Esta feature no añade ninguna dependencia.** Los recursos nuevos son cinco PNG que ya
aporta el encargo en `UI_Plantillas/Feature_Soldadura/` (granalla, cadmio, zinc, latón,
proceso), que se copian a `drawable-nodpi/` redimensionados al tamaño de los bitmaps de
metal existentes. El oro, la plata y el cobre reutilizan `modulo_oro.png`,
`modulo_plata.png` y `cobre.png`; los iconos (`ic_lingotes`, `ic_paleta`, `ic_refrescar`,
`ic_estrella`, `ic_aviso`, `ic_balanza`, `ic_check`) ya existen.

### Decisiones y sus porqués

| Decisión | Motivo |
|---|---|
| Tercer motor propio (`RecetasSoldadura`, `CalculoSoldadura`…) sin reutilizar tipos de oro ni de plata | Doctrina del proyecto: cada motor es fiel a su documento técnico. `ColorOro` tiene ROJO y §5.1 solo admite amarillo/blanco/rosa → enum propio `ColorOroSoldadura`. Las constantes `ESCALA = 15` y `TOLERANCIA = 1E-9` se duplican a propósito, como ya hizo plata respecto a oro |
| `RecetaSoldadura` con **lista** ordenada de componentes, no mapa | §8.2 exige presentar los ingredientes y las tablas de §3.2–§3.4 y §5.2 fijan un orden por receta. `RecetaLiga` (oro) usa mapa porque su orden lo pone el enum; aquí cada receta ordena distinto, así que el orden viaja en la receta |
| `RecetasSoldadura` como única fuente de verdad (§7), con `VERSION_RECETAS` | Patrón `RecetasOro`: recetas y factores desde literales `String`, prohibidos los números mágicos en UI o casos de uso. Los valores de la base son los de §5.2/§7, **no** los del mockup (§12); el test del caso patrón lo blinda |
| **Pares de casos de uso directo/inverso**, no un parámetro de modo | Patrón consolidado del proyecto (oro y plata). Cada modo tiene *su* única división con su denominador y su comentario de redondeo auditable. El enum de modo existe, pero en UI (`ModoEntradaSoldadura`), no en el motor |
| 9 casos de uso: clásica ±, plata ±, base ±, ley desde oro / desde base / inversa | Base y mezcla son cálculos independientes por mandato de §5 («dos cálculos independientes», «no mezclar con las clásicas»). El modo del mockup de ORO LEY entra por el oro (base = oro / r), el inverso por el peso final (base = T/(1+r)), y el que entra por la base (oro = base × r, §5.4, TEST 7) **existe y se prueba sin UI en v1** — precedente literal de `CalcularAleacionInversaPlataUseCase` en la 005 |
| Redondeo interno: una división por cálculo, escala 15, `HALF_UP` | Aquí no hay ley de contraste que proteger (a diferencia del DOWN/UP direccional de plata): son recetas de taller y §2.1 solo pide conservar la precisión. Multiplicaciones y restas exactas, sin redondeos intermedios (§8.1) |
| Formateo de vista `setScale(3, HALF_UP)` con coma, como `OroViewModel` | §2.1 pide 3 decimales; sin ley que proteger, la cifra más cercana es la más útil. Los 3 decimales fijos (sin eliminar ceros finales, divergencia menor respecto a §2.1 declarada aquí) mantienen la coherencia visual con oro y plata |
| §8.3 → opción 1: nota «la suma puede variar mínimamente por redondeo» junto al total | El reparto de residuo (opción 2) complica la presentación sin beneficio de taller. Ningún ingrediente se ajusta jamás para cuadrar la vista (FR-021); test explícito con el peso final 10 sobre la receta de total 1,44 |
| `familia: FamiliaSoldadura? = null` en el estado inicial | Mockup de primera visita: solo el selector. Sin familia no se calcula ni se emite telemetría de cálculo; el `SelectorSegmentado` recibe `seleccionada = -1` (compara por índice, ya lo soporta) y los botones de acción no se muestran |
| Cambiar de familia reinicia el formulario; cambiar de modo vacía cantidad y resultado conservando selecciones; «Limpiar» conserva la familia | La misma cifra significaría otro metal (familia) u otra semántica (modo): nunca se reinterpreta en silencio un «10» tecleado. Contratos fijados por test (FR-023) |
| En modo directo, el metal introducido no se repite como fila; el total se muestra siempre | Los mockups pintan solo los ingredientes a añadir; §8.2 exige además el peso final teórico, también donde el mockup lo omite (decisión recogida en la spec). En ORO LEY directo: fila «Soldadura BASE necesaria» (granalla) + total |
| `SelectorSegmentado` gana `maxPorFila: Int = Int.MAX_VALUE` | Cinco durezas no caben legibles en una fila de pesos iguales. Con el valor por defecto el comportamiento es idéntico → cero cambios en oro/plata (sus androidTest hacen de regresión). Soldaduras usa `maxPorFila = 3` (fila de 3 + fila de 2). Se rechazan etiquetas cortas (perderían los términos de taller) y un componente paralelo (duplicaría la píldora) |
| Selector de color de 3 opciones con acento por color (`GoldPrimary`/`TealPrimary`/`RoseGold`) | Decisión confirmada con el autor; mismo mecanismo de acento por opción que estrenó la calculadora de oro. El color no toca cantidades: viaja al resultado y a la telemetría (TEST 9) |
| Aviso de seguridad §9 con `AvisoTecnico`, texto literal por string; en la base, **antes** del proceso | §9 lo exige «antes del proceso informativo». En CLÁSICA aparece solo con el tipo muy floja de ley, derivado de `llevaCadmio` en el enum (patrón `LeyPlata.esSoloTecnica`), sin campo de UI |
| El consejo de doble fundido (§5.6) es caption informativa, no aviso | Es una recomendación de la receta, no una advertencia; reservar el ámbar de `AvisoTecnico` para seguridad evita banalizarlo |
| La pantalla de la base nunca muestra milésimas ni corrige a 750 | Mandato de §5.2: se conserva el nombre «base de oro de 18 K», no se muestra 754,15‰ y no se recalculan los pesos |
| Telemetría: `soldaduras_calculado` con `familia`/`modo`/`tipo`/`color`, deduplicado por combinación; pantallas `"soldaduras"` y `"soldadura_base"` | FR-027. `screen_view` de la principal conserva el nombre que ya emitía el placeholder para no romper la serie. La base es pantalla nueva y estrena serie. Clave de deduplicación compuesta, patrón `OroViewModel`; se rearma con entrada inválida, Limpiar y cambios de selección. Favoritos → `soldaduras_favoritos_proximamente` / `soldadura_base_favoritos_proximamente` |
| Botones «Limpiar» y «Guardar en favoritos» dorados en ambas pantallas; sin botón «OK» | El cálculo es reactivo y no hay nada que confirmar (decisión confirmada con el autor). `BotonDorado` sin parametrizar color: el dorado es el lenguaje de acción de la app. Toast de «Próximamente» desde la vista, patrón plata |
| Acentos: familia en dorado (ORO LEY, CLÁSICA) y plateado (PLATA); tipos y resultados en teal; entrada según familia | Es la paleta de los mockups y la misma gramática que plata (selección/resultado en teal, entrada/total en el metal). La tarjeta-botón de SOLDADURA BASE va en teal, como el mockup |
| Cinco PNG nuevos redimensionados a ~512 px en `drawable-nodpi/`; iconos de dureza descartados | Los bitmaps de metal existentes rondan 512 px; los originales (1254–1297 px) engordarían el APK sin ganancia visual. Los selectores de la app son tipográficos («usa los estilos de la aplicación», encargo), así que `MuyFloja/Floja/Media/Fuerte.png` no se incorporan |
| Cálculo síncrono, sin corrutinas ni `DispatcherProvider` | Unas pocas operaciones aritméticas no justifican saltar de hilo; mismo criterio que oro y plata |
| Strings nuevos bajo prefijo `soldadura_*` + metales compartidos `metal_laton/zinc/cadmio/oro_24k` | Los metales van al bloque compartido porque son nombres de material, no textos de la feature (precedente del renombrado de la 005). Se reutilizan `oro_color_*`, `plata_entrada_titulo`, `unidad_gramos`, `accion_*`, `aviso_proximamente` |

## Constitution Check

*GATE: revisado antes de la Fase 0 y de nuevo tras el diseño. Sin violaciones.*

| Principio | Cumplimiento |
|---|---|
| I. SDD obligatorio | spec → plan → tasks → implement. Ningún fichero de producto se toca antes de `tasks.md` aprobado. La lógica viene del documento técnico anexo a la spec; las tres discrepancias con los mockups se cerraron con el autor y están en Assumptions |
| II. MVVM con capas estancas | El motor (9 modelos + 9 casos de uso) es Kotlin puro con `java.math.BigDecimal`: cero imports de `android.*`, `androidx.*`, Firebase o `data.*`. Los dos ViewModels exponen un único `StateFlow` de su estado, no importan `androidx.compose.*` y reciben todo por constructor. Cada pantalla se parte en resolutor + `*Content` sin estado con `@Preview`. Los mapeos enum → recursos viven en `ui/soldaduras/` (fichero `PresentacionSoldadura.kt` interno al paquete) |
| III. DI solo por Koin | 9 `factoryOf` en `domainModule` y dos `viewModelOf` en `viewModelModule`. Sin módulo nuevo que registrar en `featureModules`, sin `get()` interno, sin primitivos en el grafo |
| IV. Test obligatorio | Test unitario por caso de uso (los 10 tests mínimos de §10 + propiedades + divisiones no exactas + validaciones) y por ViewModel (estado inicial, contratos de limpieza, formateo, deduplicación de telemetría, entradas inválidas). `KoinModulesTest` cubre los registros nuevos sin tocarlo. Sin corrutinas no hay `TestDispatcher` que montar |
| V. Versiones en `libs.versions.toml` | No se añaden dependencias ni versiones |

Restricciones técnicas: ninguna API por encima de minSdk 24; ningún producto nuevo de
Firebase; `google-services.json` intacto.

## Project Structure

### Documentation (this feature)

```text
specs/006-soldaduras-joyeria/
├── spec.md
├── plan.md              # este fichero
├── data-model.md        # entidades del motor de soldaduras (Fase 1)
├── tasks.md             # salida de /speckit-tasks
└── checklists/
    └── requirements.md
```

No se generan `research.md`, `contracts/` ni `quickstart.md`, por los mismos motivos que
en la 004 y la 005: la spec no dejó ningún `NEEDS CLARIFICATION` (las decisiones abiertas
se cerraron con el autor antes de redactarla), la app no expone interfaz a terceros, y la
guía de validación vive en la fase de verificación de `tasks.md` y en los criterios de
éxito de la spec. Sí se genera `data-model.md`: la feature estrena el modelo de dominio
más grande del proyecto.

### Source Code (repository root)

```text
app/src/main/
├── java/com/jrblanco/calculadoradejoyeros2021/
│   ├── domain/
│   │   ├── model/
│   │   │   ├── MetalSoldadura.kt         (N) enum ORO_24K…CADMIO con analyticsId; el orden
│   │   │   │                                 de pintado viaja en cada receta, no aquí
│   │   │   ├── ColorOroSoldadura.kt      (N) AMARILLO/BLANCO/ROSA (§5.1; sin ROJO)
│   │   │   ├── TipoSoldaduraClasica.kt   (N) FLOJA/FUERTE/MUY_FLOJA_LEY + llevaCadmio
│   │   │   ├── TipoSoldaduraPlata.kt     (N) MUY_FLOJA/FLOJA/NORMAL/FUERTE
│   │   │   ├── DurezaSoldaduraLey.kt     (N) MUY_FLOJA…MUY_FUERTE (orden = selector)
│   │   │   ├── RecetaSoldadura.kt        (N) lista ordenada de componentes + totalPatron
│   │   │   ├── RecetasSoldadura.kt       (N) única fuente de verdad de §7: 3 clásicas,
│   │   │   │                                 BASE, factorLaton(tipo), factorOro(dureza)
│   │   │   ├── CalculoSoldadura.kt       (N) resultado escalado; ESCALA/TOLERANCIA propias;
│   │   │   │                                 escalar(receta, factor) + checks (§10 propiedades)
│   │   │   └── CalculoSoldaduraLey.kt    (N) base + oro18K + color + dureza + total (§5.4)
│   │   └── usecase/
│   │       ├── CalcularSoldaduraClasicaUseCase.kt         (N) factor = oro / patrónOro (§3)
│   │       ├── CalcularSoldaduraClasicaInversaUseCase.kt  (N) factor = T / totalPatron (§2.2)
│   │       ├── CalcularSoldaduraPlataUseCase.kt           (N) latón = plata × p (§4.2)
│   │       ├── CalcularSoldaduraPlataInversaUseCase.kt    (N) plata = T/(1+p) (§4.3)
│   │       ├── CalcularSoldaduraBaseUseCase.kt            (N) factor = oro24K / 10 (§5.2)
│   │       ├── CalcularSoldaduraBaseInversaUseCase.kt     (N) factor = B / 13,26 (§5.2)
│   │       ├── CalcularSoldaduraLeyDesdeOroUseCase.kt     (N) base = oro / r (mockup)
│   │       ├── CalcularSoldaduraLeyUseCase.kt             (N) oro = base × r (§5.4; sin UI)
│   │       └── CalcularSoldaduraLeyInversaUseCase.kt      (N) base = T/(1+r) (§5.4)
│   ├── ui/
│   │   ├── components/SelectorSegmentado.kt (M) + maxPorFila opcional, retrocompatible
│   │   ├── soldaduras/                   (N) paquete de la feature, dos pantallas
│   │   │   ├── SoldadurasUiState.kt      (N) FamiliaSoldadura? + modo + selecciones +
│   │   │   │                                 resultado formateado (filas + total)
│   │   │   ├── SoldadurasViewModel.kt    (N) StateFlow único + parseo + recálculo + telemetría
│   │   │   ├── SoldadurasScreen.kt       (N) SoldadurasScreen + SoldadurasContent + previews
│   │   │   ├── SoldaduraBaseUiState.kt   (N) modo + cantidad + resultado
│   │   │   ├── SoldaduraBaseViewModel.kt (N) ídem, pantalla de la base
│   │   │   ├── SoldaduraBaseScreen.kt    (N) aviso §9 + proceso §5.3 + calculadora
│   │   │   └── PresentacionSoldadura.kt  (N) mapeos internos ingrediente → imagen/string
│   │   └── navigation/
│   │       ├── Routes.kt                 (M) + Route.SoldaduraBase
│   │       └── AppNavHost.kt             (M) Route.Soldaduras → SoldadurasScreen;
│   │                                         + composable Route.SoldaduraBase
│   └── core/di/
│       ├── DomainModule.kt               (M) + 9 factoryOf
│       └── ViewModelModule.kt            (M) + 2 viewModelOf
└── res/
    ├── values/strings.xml                (M) bloque soldadura_* + metal_laton/zinc/cadmio/
    │                                         oro_24k (+_imagen) en el bloque compartido
    └── drawable-nodpi/                   (N) granalla.png, cadmio.png, zinc.png, laton.png,
                                              proceso.png (desde UI_Plantillas, ~512 px)

app/src/test/.../domain/usecase/CalcularSoldadura*UseCaseTest.kt   (N) ×9
app/src/test/.../ui/soldaduras/SoldadurasViewModelTest.kt          (N)
app/src/test/.../ui/soldaduras/SoldaduraBaseViewModelTest.kt       (N)
app/src/androidTest/.../ui/soldaduras/SoldadurasScreenTest.kt      (N)
app/src/androidTest/.../ui/soldaduras/SoldaduraBaseScreenTest.kt   (N)

CLAUDE.md                                 (M) cuatro destinos pendientes pasan a tres; tercer
                                              motor documentado; maxPorFila en SelectorSegmentado
```

**Structure Decision**: se mantiene el módulo único `:app` con `ui → domain ← data`. La
feature añade el tercer motor a `domain/` y un paquete de pantalla con dos pantallas bajo
`ui/soldaduras/` (misma feature, misma telemetría raíz, mismos mapeos compartidos en
`PresentacionSoldadura.kt`); no toca `data/` — no hay persistencia ni fuente externa, y la
telemetría ya tiene su interfaz de dominio.

## Complexity Tracking

Cero dependencias nuevas, cero capas nuevas y ninguna violación de la constitución. Dos
puntos merecen declaración expresa.

| Divergencia | Por qué es necesaria | Alternativa más simple, y por qué se rechaza |
|---|---|---|
| Esta feature modifica `SelectorSegmentado.kt`, componente compartido que consumen oro y plata | Cinco durezas no caben legibles en una fila de pesos iguales y §5.4 define cinco. `maxPorFila` es opcional con valor por defecto neutro: con ≤ maxPorFila opciones el código sigue el camino actual | Un componente paralelo de dos filas, o etiquetas abreviadas. Se rechazan: el paralelo duplica la píldora degradada y su accesibilidad; abreviar pierde los términos de taller («Muy floja», «Muy fuerte»). El riesgo se acota con el valor por defecto + los androidTest de oro y plata como regresión |
| `CalcularSoldaduraLeyUseCase` (desde la base disponible) existe sin UI en v1 | §5.4 y TEST 7 lo definen y los criterios de §11 exigen que plata y soldadura de base admitan cálculo desde el ingrediente base; la UI de v1 expone los dos modos del encargo (desde el oro y peso final) | No implementarlo hasta que tenga pantalla. Se rechaza: es una fórmula de una línea sobre el mismo modelo, los tests de §10 lo referencian, y el precedente de la 005 (`CalcularAleacionInversaPlataUseCase`, «existe y se prueba sin UI») ya fijó la doctrina |
