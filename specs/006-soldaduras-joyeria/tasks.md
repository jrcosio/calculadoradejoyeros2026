# Tasks: Calculadora de soldaduras de joyería

**Feature**: `006-soldaduras-joyeria` | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **Modelo**: [data-model.md](./data-model.md)

## Formato: `[ID] [P?] [Story] Descripción`

- **[P]**: paralelizable — ficheros distintos, sin dependencia con tareas pendientes.
- **[Story]**: historia de usuario a la que sirve (US1…US6). Las fases de Setup,
  Fundacional y Pulido no llevan etiqueta.
- Rutas de fichero relativas a la raíz del repositorio.

**Tests**: sí. La constitución (principio IV) exige test unitario por ViewModel **y por
caso de uso**, el documento técnico exige sus diez tests mínimos y sus propiedades (§10)
como criterios de aceptación (§11, SC-001, SC-002), y hay precedente de test instrumentado
por pantalla desde la 002.

**Fuente de verdad numérica**: `UI_Plantillas/Feature_Soldadura/ESPECIFICACION_CALCULADORA_SOLDADURAS_JOYERIA.md`
(§ citados), que **prevalece sobre los mockups** (§12). Los valores esperados están en
[data-model.md](./data-model.md); en particular la base es cobre 0,54 / plata 0,80 /
zinc 0,92 / cadmio 1,00 — el mockup los muestra intercambiados y no se usa.

**Aviso de alcance**: esta feature toca un componente compartido, `SelectorSegmentado`
(parámetro opcional `maxPorFila`), consumido por oro y plata. T022 es la puerta que
verifica la regresión antes de que empiece cualquier historia.

---

## Fase 1 — Setup: assets y strings

**Propósito**: dejar en `res/` las cinco imágenes nuevas y el vocabulario de la feature.

- [X] T001 [P] Copiar las cinco imágenes de `UI_Plantillas/Feature_Soldadura/` a
  `app/src/main/res/drawable-nodpi/`, redimensionadas a ~512 px de lado mayor (con `sips -Z 512`,
  el tamaño de los bitmaps de metal existentes) y renombradas a minúsculas: `granalla.png`,
  `cadmio.png`, `zind.png` → `zinc.png`, `Laton.png` → `laton.png`, y `proceso.png` (este a
  ~800 px: es la ilustración protagonista de una tarjeta). Verificar que el nombre de recurso
  no colisiona con ninguno existente y que AAPT los acepta (solo minúsculas y guion bajo).
- [X] T002 [P] Añadir en `app/src/main/res/values/strings.xml` los metales nuevos al bloque
  `<!-- Compartido: unidades, metales y acciones -->`: `metal_oro_24k` («Oro 24K»),
  `metal_laton` («Latón»), `metal_zinc` («Zinc»), `metal_cadmio` («Cadmio») y sus
  `metal_*_imagen` («Dos lingotes de oro fino de 24 quilates», «Un lingote de latón», «Dos
  lingotes de zinc», «Dos lingotes de cadmio»); y un bloque nuevo
  `<!-- Calculadora de soldaduras -->` con: `soldadura_seccion_familia` («La soldadura que
  desea fabricar»), `soldadura_familia_oro_ley` («ORO LEY»), `soldadura_familia_clasica`
  («CLÁSICA»), `soldadura_familia_plata` («PLATA»), `soldadura_seccion_modo` («Modo de
  cálculo»), `soldadura_modo_tengo_oro18k` («Tengo oro 18K»), `soldadura_modo_tengo_oro`
  («Tengo el oro»), `soldadura_modo_tengo_plata` («Tengo la plata»),
  `soldadura_modo_peso_final` («Peso final»), `soldadura_seccion_tipo` («Tipo de
  soldadura»), `soldadura_dureza_muy_floja` («Muy floja»), `soldadura_dureza_floja`
  («Floja»), `soldadura_dureza_media` («Media»), `soldadura_dureza_fuerte` («Fuerte»),
  `soldadura_dureza_muy_fuerte` («Muy fuerte»), `soldadura_clasica_floja` («Floja»),
  `soldadura_clasica_fuerte` («Fuerte»), `soldadura_clasica_muy_floja_ley` («Muy floja
  (ley 18K)»), `soldadura_plata_muy_floja` («Muy floja *»), `soldadura_plata_floja`
  («Floja»), `soldadura_plata_normal` («Normal»), `soldadura_plata_fuerte` («Fuerte»),
  `soldadura_plata_nota_muy_floja` («* Recomendada para composturas»),
  `soldadura_ley_base_titulo` («Cómo obtener la soldadura BASE*»),
  `soldadura_ley_base_boton` («SOLDADURA BASE»), `soldadura_ley_base_nota` («* Es una
  soldadura de 18K con un punto de fusión muy muy bajo»), `soldadura_entrada_oro_18k`
  («Introduce el oro de 18K»), `soldadura_entrada_oro_24k` («Introduce el oro de 24K»),
  `soldadura_entrada_peso_final` («Peso final deseado»), `soldadura_base_entrada_peso`
  («Peso de base deseado»), `soldadura_fila_base` («Soldadura BASE»),
  `soldadura_fila_base_necesaria` («Soldadura BASE necesaria»), `soldadura_fila_oro18k`
  («Oro 18K %1$s»), `soldadura_granalla_imagen` («Granalla de soldadura base»),
  `soldadura_total` («Total de soldadura:»), `soldadura_nota_redondeo` («La suma puede
  variar mínimamente por redondeo»), `soldadura_aviso_seguridad` (texto **literal** de §9:
  «Seguridad: al calentar materiales con cadmio o zinc pueden generarse humos peligrosos.
  No inhalar los humos. Trabajar únicamente con extracción localizada o ventilación
  adecuada, los equipos de protección correspondientes y conforme a la normativa de
  seguridad aplicable. Esta calculadora no sustituye la formación profesional ni una
  evaluación de riesgos.»), `soldadura_ley_consejo_mezcla` (§5.6: «Fundir y laminar una
  primera vez; después, volver a fundir y laminar para favorecer una mezcla homogénea.»),
  `soldadura_base_titulo` («Soldadura base»), `soldadura_base_proceso_titulo` («Proceso»),
  `soldadura_base_proceso_1` («Fundimos primero el oro, la plata y el cobre y, cuando esté
  bien mezclado, añadimos el zinc y el cadmio.»), `soldadura_base_proceso_2` («Bajamos la
  intensidad del fuego para que no se volatilicen el zinc y el cadmio.»),
  `soldadura_base_proceso_3` («Laminamos el lingote obtenido para poder cortarlo con
  tijera.»), `soldadura_base_masa_teorica` («La masa calculada es teórica: no se compensan
  pérdidas de fundición.»), `soldadura_base_proceso_imagen` («Crisol de joyero vertiendo
  metal fundido»), `soldadura_base_modo_tengo_oro` («Tengo oro 24K»),
  `soldadura_base_modo_peso` («Peso de base»), `soldadura_base_total` («Peso teórico de la
  base:»). Se reutilizan sin duplicar: `unidad_gramos`, `metal_plata_fina`, `metal_cobre`
  (+`_imagen`), `accion_limpiar`, `accion_guardar_favoritos`, `aviso_proximamente`,
  `oro_seccion_color`, `oro_color_{amarillo,blanco,rosa}`, `plata_entrada_titulo`,
  `modulo_soldaduras_titulo` (FR-029).

---

## Fase 2 — Fundacional: el motor de soldaduras y `SelectorSegmentado`

**⚠️ CRÍTICO**: ninguna historia puede empezar hasta que esta fase esté completa. Son dos
bloques independientes —el motor (T003–T020) y el componente (T021)— que confluyen en T022.

Todo el motor es Kotlin puro: sin `android.*`, sin `androidx.*`, sin `R`, con
`java.math.BigDecimal` construido desde literales `String` (§2.1). Tercer documento
técnico → tipos propios, sin depender de los motores de oro ni de plata.

- [X] T003 [P] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/MetalSoldadura.kt`:
  `enum class MetalSoldadura { ORO_24K, ORO_18K, PLATA_FINA, LATON, COBRE, ZINC, CADMIO }`
  con `val analyticsId: String get() = name.lowercase()`. KDoc: el orden del enum **no** es
  el orden de pintado — cada receta fija el suyo (§3.2–§3.4, §5.2); el color del oro 18K no
  vive aquí porque no cambia el metal ni su peso (TEST 9 de §10).
- [X] T004 [P] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/ColorOroSoldadura.kt`:
  `enum class ColorOroSoldadura { AMARILLO, BLANCO, ROSA }` con `analyticsId`. KDoc citando
  §5.1: enum propio porque `ColorOro` (motor de oro) incluye ROJO, que este documento no
  admite; el color solo cambia el oro añadido en la segunda fase, nunca las cantidades.
- [X] T005 [P] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/TipoSoldaduraClasica.kt`:
  `enum class TipoSoldaduraClasica { FLOJA, FUERTE, MUY_FLOJA_LEY }` con `analyticsId` y
  `val llevaCadmio: Boolean get() = this == MUY_FLOJA_LEY` (patrón `LeyPlata.esSoloTecnica`:
  la pantalla deriva de aquí la advertencia §9 sin campo de UI). KDoc citando §3.1 y §8.1:
  recetas de oro amarillo, sin elección de color por diseño de tipos.
- [X] T006 [P] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/TipoSoldaduraPlata.kt`:
  `enum class TipoSoldaduraPlata { MUY_FLOJA, FLOJA, NORMAL, FUERTE }` con `analyticsId`.
  KDoc citando §4.1 (interpretación obligatoria: el factor es latón respecto a la plata
  fina, no sobre el peso final). El factor vive en `RecetasSoldadura`, no aquí.
- [X] T007 [P] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/DurezaSoldaduraLey.kt`:
  `enum class DurezaSoldaduraLey { MUY_FLOJA, FLOJA, MEDIA, FUERTE, MUY_FUERTE }` con
  `analyticsId`. KDoc citando §5.4–§5.5: el orden del enum es el del selector (factor
  creciente); más oro implica soldadura más fuerte.
- [X] T008 Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/RecetaSoldadura.kt`:
  `data class ComponenteReceta(val metal: MetalSoldadura, val pesoPatron: BigDecimal)` y
  `data class RecetaSoldadura(val componentes: List<ComponenteReceta>)` con
  `val totalPatron: BigDecimal` (suma exacta) e `init` que exige lista no vacía, pesos > 0
  y metales sin repetir. KDoc: **lista y no mapa** a propósito — el orden de la tabla del
  documento es el orden estable de presentación (§8.2), a diferencia de `RecetaLiga` de
  oro, cuyo orden lo pone el enum. **Depende de T003**.
- [X] T009 Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/RecetasSoldadura.kt`:
  `object RecetasSoldadura` con `const val VERSION_RECETAS = "1.0"` (patrón `RecetasOro`) y
  la transcripción **literal** de §7 desde `BigDecimal("…")`: `fun clasica(tipo)` — FLOJA
  [oro18K «5», plata «2», latón «1»], FUERTE [oro18K «5», plata «0.50», cobre «0.50», latón
  «0.50»], MUY_FLOJA_LEY [oro24K «1», plata «0.10», latón «0.16», cadmio «0.18»] —,
  `val BASE` [oro24K «10», cobre «0.54», plata «0.80», zinc «0.92», cadmio «1.00»] con
  comentario de que el mockup muestra estos valores intercambiados y §12 da prevalencia al
  documento, `fun factorLaton(tipo): BigDecimal` («0.75»/«0.50»/«0.40»/«0.30», §4.1) y
  `fun factorOro(dureza): BigDecimal` («0.3»/«0.5»/«1»/«2»/«3», §5.4). Prohibido duplicar
  estos números en UI o casos de uso (§7). **Depende de T005, T006, T007 y T008**.
- [X] T010 Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/CalculoSoldadura.kt`:
  `data class ComponenteCalculado(val metal: MetalSoldadura, val gramos: BigDecimal)` y
  `data class CalculoSoldadura(val componentes: List<ComponenteCalculado>, val total: BigDecimal)`.
  En su `companion object`: `const val ESCALA = 15` y `TOLERANCIA = BigDecimal("1E-9")`
  —comentario de que se repiten a propósito respecto a los otros dos motores: tres
  documentos técnicos distintos—, `internal fun escalar(receta: RecetaSoldadura, factor: BigDecimal): CalculoSoldadura`
  (multiplica cada patrón por el factor, exacto, sin redondeos intermedios §8.1; total =
  suma de componentes) e `internal fun de(componentes: List<ComponenteCalculado>): CalculoSoldadura`,
  ambos con `check` de las propiedades de §10: todos los gramos > 0 y total = suma (con
  `TOLERANCIA`), con mensajes que incluyan los valores. Red de seguridad, no lógica de
  negocio. **Depende de T008**.
- [X] T011 Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/CalculoSoldaduraLey.kt`:
  `data class CalculoSoldaduraLey(val base: BigDecimal, val oro18K: BigDecimal, val color: ColorOroSoldadura, val dureza: DurezaSoldaduraLey, val total: BigDecimal)`
  con fábrica `internal fun de(...)` que verifica `base > 0`, `oro18K > 0` y
  `total = base + oro18K` (exacto). KDoc: tipo propio y no `CalculoSoldadura` porque la
  base no es un `MetalSoldadura` y el color debe viajar en el resultado (§5.4, TEST 9).
  **Depende de T004 y T007**.
- [X] T012 [P] Crear los dos casos de uso de CLÁSICA en
  `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/usecase/`:
  `CalcularSoldaduraClasicaUseCase.kt` — `operator fun invoke(oroDisponible: BigDecimal, tipo: TipoSoldaduraClasica): CalculoSoldadura`,
  `require(oroDisponible > ZERO)` con mensaje (§8.1, TEST 10); única división
  `factor = oroDisponible ÷ pesoPatrón del oro de la receta` (5, 5 o 1) a escala 15
  `HALF_UP` —comentario: sin ley de contraste que proteger, la cifra más cercana (a
  diferencia del DOWN/UP de plata)—; delega en `CalculoSoldadura.escalar` — y
  `CalcularSoldaduraClasicaInversaUseCase.kt` — `operator fun invoke(pesoFinal: BigDecimal, tipo): CalculoSoldadura`
  con `factor = pesoFinal ÷ totalPatron` (8; 6,50; 1,44 — división infinita) (§2.2).
  Sin parámetro de color, por diseño (§8.1). **Depende de T009 y T010**.
- [X] T013 [P] Crear los dos casos de uso de PLATA:
  `CalcularSoldaduraPlataUseCase.kt` — `operator fun invoke(plataFina: BigDecimal, tipo: TipoSoldaduraPlata): CalculoSoldadura`
  con `laton = plataFina × factorLaton(tipo)` y `total = plataFina + laton`, **sin
  división, exacto** (§4.2); componentes en orden [plata fina, latón] — y
  `CalcularSoldaduraPlataInversaUseCase.kt` — `operator fun invoke(pesoFinal, tipo)` con
  `plata = pesoFinal ÷ (1 + p)` (escala 15, HALF_UP) y `laton = pesoFinal − plata` (§4.3),
  de modo que el total recupera exactamente el peso pedido. **Depende de T009 y T010**.
- [X] T014 [P] Crear los dos casos de uso de la BASE:
  `CalcularSoldaduraBaseUseCase.kt` — `operator fun invoke(oro24K: BigDecimal): CalculoSoldadura`
  con `factor = oro24K ÷ 10` (§5.2) sobre `RecetasSoldadura.BASE` — y
  `CalcularSoldaduraBaseInversaUseCase.kt` — `operator fun invoke(pesoBase: BigDecimal)` con
  `factor = pesoBase ÷ 13,26` (división infinita, §5.2). KDoc en ambos citando §5.2: la
  proporción teórica de oro es 754,15 milésimas pero se conserva el nombre «base de oro de
  18 K», no se muestra esa ley y **no se corrigen los pesos hacia 750** — prohibición
  expresa. **Depende de T009 y T010**.
- [X] T015 [P] Crear los tres casos de uso de la mezcla de LEY (§5.4):
  `CalcularSoldaduraLeyDesdeOroUseCase.kt` — `operator fun invoke(oro18K: BigDecimal, dureza: DurezaSoldaduraLey, color: ColorOroSoldadura): CalculoSoldaduraLey`
  con `base = oro18K ÷ factorOro(dureza)` (única división; es el modo del mockup: 2 g muy
  floja → 6,67 g de base) —,
  `CalcularSoldaduraLeyUseCase.kt` — `operator fun invoke(baseDisponible, dureza, color)` con
  `oro18K = base × r`, sin división (TEST 7); **sin UI en v1**, precedente
  `CalcularAleacionInversaPlataUseCase` de la 005 — y
  `CalcularSoldaduraLeyInversaUseCase.kt` — `operator fun invoke(pesoFinal, dureza, color)`
  con `base = pesoFinal ÷ (1 + r)` y `oro18K = pesoFinal − base` (TEST 8). El color entra
  obligatorio por parámetro (§8.1) y viaja al resultado sin tocar cantidades. Los tres
  delegan en `CalculoSoldaduraLey.de(...)`. **Depende de T009 y T011**.
- [X] T016 Registrar los nueve casos de uso en
  `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/core/di/DomainModule.kt` con
  `factoryOf`; sin módulo nuevo, así que `core/di/AppModule.kt` no se toca y
  `KoinModulesTest` los cubre solo. **Depende de T012, T013, T014 y T015**.
- [X] T017 [P] Crear `app/src/test/java/com/jrblanco/calculadoradejoyeros2021/domain/usecase/CalcularSoldaduraClasicaUseCaseTest.kt`
  y `CalcularSoldaduraClasicaInversaUseCaseTest.kt` (JUnit4, nombres en backticks en
  español sin tildes, motor real sin mocks, helper `assertCerca(esperado: String, real: BigDecimal)`
  con tolerancia 1E-6 como en oro y plata). Directo: floja con 10 g de oro → plata 4,
  latón 2, total 16 (caso del mockup); fuerte con 10 g → plata/cobre/latón 1 cada uno,
  total 13 (mockup); muy floja de ley con 7 g de oro 24K → plata 0,70, latón 1,12, cadmio
  1,26, total 10,08 (equivalencia ×7 de §3.4, que **no** es un lote de 10 g); propiedad de
  linealidad (duplicar la entrada duplica todos los componentes); `assertThrows` para cero
  y negativo (TEST 10). Inverso: **TEST 1** (T=8 flojo → 5/2/1), **TEST 2** (T=6,50 fuerte
  → 0,50/0,50/0,50/5), **TEST 3** (T=10,08 muy flojo de ley → 7/0,70/1,12/1,26), T=10 muy
  flojo de ley (división infinita 10÷1,44: componentes ≈ 6,944444444444444 /
  0,694444444444444 / 1,111111111111111 / 1,25 y suma == 10 dentro de `TOLERANCIA`), y
  validaciones. **Depende de T012**.
- [X] T018 [P] Crear `CalcularSoldaduraPlataUseCaseTest.kt` y
  `CalcularSoldaduraPlataInversaUseCaseTest.kt` (misma carpeta y convenciones). Directo:
  **TEST 4** (25 g muy floja → latón 18,75, total 43,75 — exactos con `compareTo == 0`, no
  hay división), **TEST 5** (25 g fuerte → 7,50 / 32,50), tabla completa de §4.2 (floja
  12,50/37,50; normal 10/35), linealidad y validaciones. Inverso: ejemplo de §4.3 (T=10 muy
  floja → plata 5,714285714285714 y latón 4,285714285714286, con `plata + laton == 10`
  exacto), las cuatro `p` recuperando el peso pedido, y validaciones. **Depende de T013**.
- [X] T019 [P] Crear `CalcularSoldaduraBaseUseCaseTest.kt` y
  `CalcularSoldaduraBaseInversaUseCaseTest.kt`. Directo: **TEST 6** (10 g de oro 24K →
  cobre 0,54, plata 0,80, zinc 0,92, cadmio 1,00, total 13,26 — los valores del documento,
  no los del mockup), escala con 7 g (0,378 / 0,56 / 0,644 / 0,7, total 9,282), invariante
  «no se corrige a 750»: el total es exactamente 13,26 × factor, y validaciones. Inverso:
  B=13,26 → oro 10 (deshace al directo), B=10 (división infinita 10÷13,26 → oro ≈
  7,541478129713424 y suma == 10 dentro de `TOLERANCIA`), y validaciones. **Depende de T014**.
- [X] T020 [P] Crear `CalcularSoldaduraLeyDesdeOroUseCaseTest.kt`,
  `CalcularSoldaduraLeyUseCaseTest.kt` y `CalcularSoldaduraLeyInversaUseCaseTest.kt`. Desde
  oro: caso del mockup (2 g muy floja → base ≈ 6,666666666666667, total ≈
  8,666666666666667), media (1 g → base 1, total 2), muy fuerte (3 g → base 1), y
  validaciones. Desde base: **TEST 7** (B=1 muy floja → oro 0,3, total 1,3), la tabla
  completa de §5.4 con B=1 (totales 1,3 / 1,5 / 2 / 3 / 4), **TEST 9** (color BLANCO, B=1,
  FLOJA → `color == BLANCO` y oro 0,5) y la propiedad «cambiar el color no cambia ningún
  peso» comparando los tres colores. Inverso: **TEST 8** (T=10 muy fuerte → base 2,5, oro
  7,5), muy floja con T=10 (base ≈ 7,692307692307692, oro ≈ 2,307692307692308, suma == 10),
  y validaciones. **Depende de T015**.
- [X] T021 [P] Añadir a `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/components/SelectorSegmentado.kt`
  el parámetro opcional `maxPorFila: Int = Int.MAX_VALUE`: con `opciones.size <= maxPorFila`
  el código sigue el camino actual sin cambios; con más, envuelve en una `Column` con
  `Arrangement.spacedBy` filas de hasta `maxPorFila` segmentos, **conservando los índices
  globales** en `onSeleccion` y el `selectableGroup` en el contenedor raíz para el lector
  de pantalla. KDoc del porqué (cinco durezas de §5.4 no caben legibles en una fila) y de
  que el valor por defecto garantiza que oro y plata no cambian ni un píxel.
- [X] T022 Puerta del motor y de la regresión:
  `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"` y
  comprobar que `./gradlew :app:testDebugUnitTest` (con los nueve tests nuevos del motor en
  verde) y `./gradlew :app:assembleDebug` pasan, y que las `@Preview` de oro y plata
  renderizan igual que antes de T021. **Depende de T001–T021**.

**Checkpoint**: motor de soldaduras completo y en verde en JVM (TEST 1–10 y propiedades de
§10 cubiertos), `SelectorSegmentado` ampliado sin regresión, oro y plata intactos.

---

## Fase 3 — User Story 1: elegir familia y calcular la soldadura de oro de ley (P1) 🎯 MVP

**Objetivo**: el módulo de soldaduras deja de ser andamiaje: selector de familia (solo eso
en la primera visita) y formulario de ORO LEY en modo directo — color, gramos de oro 18K,
cinco durezas, base necesaria y total.

**Test independiente**: abrir Home → «Soldaduras de Oro y Plata», ver solo el selector;
elegir ORO LEY, introducir 2 g con muy floja y comprobar base 6,667 g y total 8,667 g;
recorrer durezas y colores contra §5.4.

- [X] T023 [P] [US1] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/soldaduras/SoldadurasUiState.kt`:
  `enum class FamiliaSoldadura { ORO_LEY, CLASICA, PLATA }` y
  `enum class ModoEntradaSoldadura { DESDE_METAL, PESO_FINAL }` (ambos con `analyticsId`),
  `enum class IngredienteSoldadura { BASE, ORO_24K, ORO_18K, PLATA_FINA, LATON, COBRE, ZINC, CADMIO }`,
  `data class FilaSoldadura(val ingrediente: IngredienteSoldadura, val gramosFormateados: String)`,
  `data class ResultadoSoldaduras(val filas: List<FilaSoldadura>, val totalFormateado: String)` y
  `data class SoldadurasUiState(val familia: FamiliaSoldadura? = null, val modo: ModoEntradaSoldadura = DESDE_METAL, val cantidadTexto: String = "", val colorOro: ColorOroSoldadura = AMARILLO, val dureza: DurezaSoldaduraLey = MUY_FLOJA, val tipoClasica: TipoSoldaduraClasica = FLOJA, val tipoPlata: TipoSoldaduraPlata = MUY_FLOJA, val resultado: ResultadoSoldaduras? = null)`.
  KDoc: el constructor sin argumentos **es** la primera visita (`familia = null`, solo el
  selector, FR-002); `IngredienteSoldadura` existe porque la BASE no es un `MetalSoldadura`
  y la fila de oro 18K necesita el color para su etiqueta; las cifras viajan formateadas y
  el valor exacto vive solo en el motor. Son conceptos de UI, como `HomeModule`: ningún
  caso de uso los recibe. **Depende de T022**.
- [X] T024 [P] [US1] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/soldaduras/PresentacionSoldadura.kt`
  (interno al paquete): mapeos privados de presentación `IngredienteSoldadura → imagenRes /
  imagenDescripcionRes / nombreRes` — BASE → `granalla.png` + `soldadura_granalla_imagen` +
  `soldadura_fila_base`; ORO_24K → `modulo_oro` + `metal_oro_24k`; ORO_18K → `modulo_oro` +
  `soldadura_fila_oro18k` (con el color como argumento); PLATA_FINA → `modulo_plata` +
  `metal_plata_fina`; LATON → `laton.png`; COBRE → `cobre.png`; ZINC → `zinc.png`; CADMIO →
  `cadmio.png` — y `ColorOroSoldadura → etiquetaRes (oro_color_*) / acento
  (GoldPrimary/TealPrimary/RoseGold)`, el mismo mapeo de acento que usa la calculadora de
  oro. `domain/` no conoce Android: el mapeo vive aquí (patrón `LeyPlata.etiquetaRes`).
  **Depende de T001, T002 y T023**.
- [X] T025 [US1] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/soldaduras/SoldadurasViewModel.kt`:
  constructor con los seis casos de uso con UI (`CalcularSoldaduraClasicaUseCase`,
  `CalcularSoldaduraClasicaInversaUseCase`, `CalcularSoldaduraPlataUseCase`,
  `CalcularSoldaduraPlataInversaUseCase`, `CalcularSoldaduraLeyDesdeOroUseCase`,
  `CalcularSoldaduraLeyInversaUseCase`) y `AnalyticsRepository`;
  `MutableStateFlow(SoldadurasUiState())` expuesto con `asStateFlow()`;
  `init { analytics.logScreenView("soldaduras") }` — el mismo nombre que emitía el
  placeholder, para conservar la serie histórica (FR-027). En esta historia: 
  `onFamiliaSeleccionada(familia)` → `SoldadurasUiState(familia = nueva)` (estado limpio,
  FR-023), `onCantidadCambiada(texto)`, `onColorSeleccionado(color)` y
  `onDurezaSeleccionada(dureza)`; con `familia == null` no se calcula ni se registra nada.
  Parseo patrón plata: `trim().replace(',', '.').toBigDecimalOrNull()?.takeIf { it > ZERO }`
  (FR-004, FR-005). Cálculo de ORO LEY directo con `calcularLeyDesdeOro`: filas
  [BASE → `soldadura_fila_base_necesaria`] y total; formateo
  `setScale(3, RoundingMode.HALF_UP).toPlainString().replace('.', ',')` con comentario: sin
  ley de contraste que proteger, la cifra más cercana, a diferencia del DOWN de plata
  (FR-020). Telemetría `soldaduras_calculado` con
  `mapOf("familia" to ..., "modo" to ..., "tipo" to dureza.analyticsId, "color" to ...)`,
  deduplicada por combinación (data class privada, patrón `OroViewModel`), rearmada cuando
  la entrada pasa a inválida; nunca la cantidad (FR-027). Sin corrutinas: el cálculo es
  síncrono. **Depende de T023**.
- [X] T026 [US1] Registrar `viewModelOf(::SoldadurasViewModel)` en
  `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/core/di/ViewModelModule.kt`.
  **Depende de T025**.
- [X] T027 [US1] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/soldaduras/SoldadurasScreen.kt`
  con el contrato de pantalla: `SoldadurasScreen(onInfo, onBack, onSoldaduraBase, modifier, viewModel = koinViewModel())`
  que colecta con `collectAsStateWithLifecycle()`, y `SoldadurasContent(uiState, callbacks…, modifier)`
  sin estado con `@Preview(widthDp = 411, heightDp = 891)` privadas (primera visita y ORO
  LEY con resultado). Dentro de `JewelryScaffold(onInfo, title = stringResource(R.string.modulo_soldaduras_titulo), onBack)`,
  `Column` con `verticalScroll`, `imePadding()` y `spacedBy(JewelrySpacing.Md)` (FR-025,
  FR-026):
  1. `CabeceraSeccion(ic_lingotes, soldadura_seccion_familia)` y `SelectorSegmentado` de
     familia — ORO LEY y CLÁSICA en `GoldPrimary`, PLATA en `SilverPrimary`;
     `seleccionada = uiState.familia?.ordinal ?: -1` (con `-1` ningún segmento activo;
     el componente compara por índice y ya lo soporta) (FR-002).
  2. Si `familia == null`, nada más (primera visita).
  3. Con ORO_LEY: tarjeta `TarjetaAcento(TealPrimary)` con `soldadura_ley_base_titulo`, un
     botón «SOLDADURA BASE» (píldora con borde teal, `role = Role.Button`, altura mínima
     `MinTouchTarget`) que dispara `onSoldaduraBase`, y la nota `soldadura_ley_base_nota`
     en `bodySmall`/`TextMuted` (FR-011); `CabeceraSeccion(ic_paleta, oro_seccion_color)` +
     `SelectorSegmentado` de 3 colores con acento por opción (FR-009); tarjeta de entrada
     privada (patrón plata): `TarjetaAcento(acento del color)` con `Image(modulo_oro,
     oro_entrada_imagen)` a 96 dp, título `soldadura_entrada_oro_18k` y
     `CampoCantidad(acento del color)` (FR-004); `CabeceraSeccion(ic_lingotes,
     soldadura_seccion_tipo, tinte = TealPrimary)` + `SelectorSegmentado` de las **5
     durezas** con `maxPorFila = 3` y acento `TealPrimary` (FR-010); resultado:
     `TarjetaAcento(TealPrimary)` con `FilaMetal` por fila vía `PresentacionSoldadura`,
     `TarjetaTotal(soldadura_total, acento del color)`, la nota `soldadura_nota_redondeo`
     en `bodySmall`/`TextMuted` (FR-021) y el consejo `soldadura_ley_consejo_mezcla` (§5.6).
  4. En `AppNavHost` de esta tarea el callback `onSoldaduraBase` queda propagado pero la
     ruta destino llega en la US2 (aquí se pasa una lambda vacía).
  **Depende de T021, T024, T025 y T026**.
- [X] T028 [US1] Sustituir en `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/navigation/AppNavHost.kt`
  el `composable<Route.Soldaduras> { PlaceholderScreen(...) }` por
  `SoldadurasScreen(onInfo = onInfo, onBack = onBack, onSoldaduraBase = {})` (la ruta de la
  base se cablea en la US2). `Route.Soldaduras`, `HomeModule.SOLDADURAS` y la tarjeta de
  Home ya existen y no se tocan (FR-001, SC-011). **Depende de T027**.
- [X] T029 [P] [US1] Crear `app/src/test/java/com/jrblanco/calculadoradejoyeros2021/ui/soldaduras/SoldadurasViewModelTest.kt`
  (mockk relaxed **solo** para `AnalyticsRepository`, casos de uso reales, Turbine para el
  estado inicial): estado inicial = `SoldadurasUiState()` (familia null, sin resultado);
  `logScreenView("soldaduras")` en init; sin familia no hay cálculo ni evento aunque se
  teclee; elegir ORO_LEY deja formulario limpio; caso del mockup (2 g, muy floja → fila
  base «6,667», total «8,667»); dureza media con 5 g → «5,000»/«10,000»; cambiar color no
  cambia cifras pero estrena evento con su `color`; coma ≡ punto; inválidas
  `listOf("", "0", "-1", "abc", "1.2,3", "  ")` → sin resultado; una cantidad muy grande
  (100000 g) calcula y formatea sin perder precisión (edge case de la spec); deduplicación (teclear no
  duplica `soldaduras_calculado`; cambiar dureza sí, con
  `verify(exactly = …) { analytics.logEvent("soldaduras_calculado", mapOf("familia" to "oro_ley", "modo" to "desde_metal", "tipo" to …, "color" to …)) }`).
  **Depende de T025**.
- [X] T030 [P] [US1] Crear `app/src/androidTest/java/com/jrblanco/calculadoradejoyeros2021/ui/soldaduras/SoldadurasScreenTest.kt`
  (monta `SoldadurasContent` directo, sin Koin, patrón `PlataScreenTest`; nombres
  camelCase con guion bajo; helper `montar(uiState, callbacks…)`): primera visita → existe
  el selector de familia y NO existen campo de cantidad, durezas ni botones; ningún
  segmento de familia marcado como seleccionado; con ORO_LEY → las 5 durezas visibles, los
  3 colores visibles y «Rojo» no existe; pulsar familia/dureza/color propaga el valor
  esperado; el botón SOLDADURA BASE propaga su callback; estado precocinado con resultado
  pinta la fila de base, el total y la nota de redondeo. **Depende de T027**.

**Checkpoint**: el módulo ya es real — primera visita con solo el selector y ORO LEY
calculando en vivo. US1 verificable de forma independiente.

---

## Fase 4 — User Story 2: preparar la soldadura BASE (P2)

**Objetivo**: pantalla nueva de la base con advertencia de seguridad §9, proceso de taller
§5.3 y calculadora desde oro 24K.

**Test independiente**: desde ORO LEY pulsar «SOLDADURA BASE», comprobar aviso y proceso,
introducir 10 g y validar 0,540/0,800/0,920/1,000 con total 13,260.

- [X] T031 [P] [US2] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/soldaduras/SoldaduraBaseUiState.kt`:
  `data class SoldaduraBaseUiState(val modo: ModoEntradaSoldadura = DESDE_METAL, val cantidadTexto: String = "", val resultado: ResultadoSoldaduraBase? = null)`
  y `data class ResultadoSoldaduraBase(val filas: List<FilaSoldadura>, val totalFormateado: String)`.
  Los avisos no tienen campo: la base siempre lleva cadmio y zinc, el aviso es estático
  (FR-017). **Depende de T023**.
- [X] T032 [US2] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/soldaduras/SoldaduraBaseViewModel.kt`:
  constructor `(calcularBase: CalcularSoldaduraBaseUseCase, calcularBaseInversa: CalcularSoldaduraBaseInversaUseCase, analytics: AnalyticsRepository)`;
  `init { analytics.logScreenView("soldadura_base") }` (pantalla nueva, serie nueva,
  FR-027); `onCantidadCambiada`; en esta historia solo el modo directo: filas
  cobre/plata/zinc/cadmio en el orden de §5.2 (sin la fila del oro introducido) y total;
  mismo parseo y formateo que `SoldadurasViewModel`; telemetría `soldadura_base_calculado`
  con `mapOf("modo" to ...)`, deduplicada por modo, jamás la cantidad. **Depende de T031**.
- [X] T033 [US2] Registrar `viewModelOf(::SoldaduraBaseViewModel)` en
  `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/core/di/ViewModelModule.kt`.
  **Depende de T032**.
- [X] T034 [US2] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/soldaduras/SoldaduraBaseScreen.kt`
  (`SoldaduraBaseScreen` + `SoldaduraBaseContent` + `@Preview`):
  `JewelryScaffold(onInfo, title = stringResource(R.string.soldadura_base_titulo), onBack)`
  con, en orden: 1) `AvisoTecnico(soldadura_aviso_seguridad)` — **antes** del proceso,
  mandato literal de §9 (FR-017); 2) `TarjetaAcento(GoldPrimary)` «Proceso»:
  `CabeceraSeccion`-título `soldadura_base_proceso_titulo`, `Image(proceso.png,
  soldadura_base_proceso_imagen)` y las tres viñetas numeradas `soldadura_base_proceso_1/2/3`
  más `soldadura_base_masa_teorica` en `bodySmall` (§5.3, FR-013); sin milésimas de la base
  en ningún texto (FR-014); 3) tarjeta de entrada (imagen `modulo_oro`, título
  `soldadura_entrada_oro_24k`, `CampoCantidad` dorado); 4) resultado:
  `TarjetaAcento(TealPrimary)` con las filas vía `PresentacionSoldadura` +
  `TarjetaTotal(soldadura_base_total)` + nota `soldadura_nota_redondeo` (FR-021).
  **Depende de T024, T032 y T033**.
- [X] T035 [US2] Añadir `@Serializable data object SoldaduraBase : Route` en
  `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/navigation/Routes.kt`, y en
  `AppNavHost.kt` registrar `composable<Route.SoldaduraBase> { SoldaduraBaseScreen(onInfo = onInfo, onBack = onBack) }`
  y cablear el callback pendiente de T028:
  `SoldadurasScreen(..., onSoldaduraBase = { goTo(Route.SoldaduraBase) })` (FR-011, FR-025).
  **Depende de T034**.
- [X] T036 [P] [US2] Crear `app/src/test/java/com/jrblanco/calculadoradejoyeros2021/ui/soldaduras/SoldaduraBaseViewModelTest.kt`:
  `logScreenView("soldadura_base")` en init; **TEST 6 formateado** (10 → filas «0,540»,
  «0,800», «0,920», «1,000» en ese orden y total «13,260»); 7 g → «0,378»/«0,560»/«0,644»/
  «0,700» y «9,282»; coma ≡ punto; inválidas sin resultado; deduplicación del evento.
  **Depende de T032**.
- [X] T037 [P] [US2] Crear `app/src/androidTest/java/com/jrblanco/calculadoradejoyeros2021/ui/soldaduras/SoldaduraBaseScreenTest.kt`:
  el aviso de seguridad es visible siempre y aparece antes que la tarjeta de proceso; la
  tarjeta «Proceso» y sus tres pasos son visibles; con estado precocinado se pintan las
  cuatro filas y el total; escribir en el campo propaga `onCantidadCambiada`; en ningún
  nodo aparece «750» ni «754» (FR-014). **Depende de T034**.

**Checkpoint**: US1 y US2 completas — el método de ley entero, con sus dos fases.

---

## Fase 5 — User Story 3: soldaduras clásicas (P3)

**Objetivo**: la familia CLÁSICA con sus tres recetas, entrada por el oro de la receta y
advertencia de seguridad en la muy floja de ley.

**Test independiente**: elegir CLÁSICA y validar los tres tipos contra §3 (10 g → 4/2;
1/1/1; 1/1,6/1,8) y la aparición/desaparición del aviso.

- [X] T038 [US3] Ampliar `SoldadurasViewModel` con `onTipoClasicaSeleccionado(tipo)` y el
  cálculo de CLÁSICA en modo directo con `CalcularSoldaduraClasicaUseCase`: filas sin el
  oro introducido (FR-022) en el orden de la receta, total siempre; el `tipo` entra en la
  clave de deduplicación y en el param `tipo` del evento (sin `color`: las clásicas no lo
  tienen, FR-015). **Depende de T029 (US1 en verde)**.
- [X] T039 [US3] Ampliar `SoldadurasContent` con el formulario de CLÁSICA: tarjeta de
  entrada dorada (imagen `modulo_oro`; título `soldadura_entrada_oro_18k` u
  `soldadura_entrada_oro_24k` según `tipoClasica`), selector de 3 tipos en teal
  (`soldadura_clasica_*`), `AvisoTecnico(soldadura_aviso_seguridad)` visible solo si
  `tipoClasica.llevaCadmio` (FR-017), y resultado con filas + `TarjetaTotal(soldadura_total)`
  + nota de redondeo. Sin selector de color (FR-015). **Depende de T038**.
- [X] T040 [P] [US3] Ampliar `SoldadurasViewModelTest`: floja 10 g → filas plata «4,000» y
  latón «2,000» (sin fila de oro) y total «16,000»; fuerte 10 g → tres filas «1,000» y
  total «13,000»; muy floja de ley 10 g → «1,000»/«1,600»/«1,800» y total «14,400» (los
  tres casos de los mockups, SC-003); cambiar de tipo recalcula y estrena evento; cambiar
  de familia desde ORO_LEY limpia cantidad y resultado (FR-023). **Depende de T038**.
- [X] T041 [P] [US3] Ampliar `SoldadurasScreenTest`: con CLÁSICA y MUY_FLOJA_LEY el aviso
  de seguridad existe; con FLOJA no existe; no existe ningún selector de color en CLÁSICA;
  pulsar un tipo propaga el enum esperado. **Depende de T039**.

**Checkpoint**: tres recetas clásicas operativas con su advertencia.

---

## Fase 6 — User Story 4: soldaduras de plata (P4)

**Objetivo**: la familia PLATA con sus cuatro factores de latón.

**Test independiente**: elegir PLATA y validar 25 g en los cuatro tipos contra §4.2.

- [ ] T042 [US4] Ampliar `SoldadurasViewModel` con `onTipoPlataSeleccionado(tipo)` y el
  cálculo de PLATA en modo directo con `CalcularSoldaduraPlataUseCase`: fila única de latón
  (la plata introducida no se repite, FR-022) y total. **Depende de T040 (US3 en verde)**.
- [ ] T043 [US4] Ampliar `SoldadurasContent` con el formulario de PLATA: tarjeta de entrada
  plateada (imagen `modulo_plata`, título `plata_entrada_titulo` reutilizado,
  `CampoCantidad(acento = SilverPrimary, borde = SilverDark)`), selector de 4 tipos en teal
  (`soldadura_plata_*`) con la caption fija `soldadura_plata_nota_muy_floja` debajo
  (FR-016), y resultado: fila latón + `TarjetaTotal(soldadura_total, acento = SilverPrimary)`
  + nota de redondeo. **Depende de T042**.
- [ ] T044 [P] [US4] Ampliar `SoldadurasViewModelTest`: **TEST 4 formateado** (25 g muy
  floja → latón «18,750», total «43,750»), **TEST 5 formateado** (fuerte → «7,500» /
  «32,500»), floja y normal según §4.2. **Depende de T042**.
- [ ] T045 [P] [US4] Ampliar `SoldadurasScreenTest`: con PLATA los 4 tipos y la nota de
  composturas son visibles; estado precocinado pinta la fila de latón y el total.
  **Depende de T043**.

**Checkpoint**: las tres familias completas en modo directo.

---

## Fase 7 — User Story 5: calcular desde el peso final deseado (P5)

**Objetivo**: el conmutador de modo en las tres familias y en la base, con los repartos
inversos y el contrato de limpieza al cambiar de modo.

**Test independiente**: cambiar el modo en cada familia y validar §2.2, §4.3 y §5.4
(8 g flojos → 5/2/1; 10 g plata muy floja → 5,714/4,286; 10 g muy fuerte → 2,5/7,5;
base 13,26 → oro 10).

- [ ] T046 [US5] Ampliar `SoldadurasViewModel` con `onModoCambiado(modo)` —vacía
  `cantidadTexto` y `resultado`, conserva familia y selecciones (FR-023), rearma la
  deduplicación— y los cálculos inversos por familia (`CalcularSoldaduraClasicaInversaUseCase`,
  `CalcularSoldaduraPlataInversaUseCase`, `CalcularSoldaduraLeyInversaUseCase`): en
  inverso se pintan **todas** las filas (incluido el metal que en directo era la entrada;
  en ORO LEY: fila BASE con `soldadura_fila_base` y fila ORO_18K con el color) y el total
  recupera el peso pedido; el param `modo` del evento cambia (FR-027). **Depende de T044
  (US4 en verde)**.
- [ ] T047 [US5] Ampliar `SoldadurasContent` con el conmutador de modo bajo el selector de
  familia (`SelectorSegmentado` de 2 opciones en teal, cabecera `soldadura_seccion_modo`):
  etiqueta del modo directo por familia (`soldadura_modo_tengo_oro18k` /
  `soldadura_modo_tengo_oro` / `soldadura_modo_tengo_plata`) y
  `soldadura_modo_peso_final`; el título de la tarjeta de entrada pasa a
  `soldadura_entrada_peso_final` en modo inverso (FR-003). **Depende de T046**.
- [ ] T048 [US5] Ampliar `SoldaduraBaseViewModel` y `SoldaduraBaseContent` con su
  conmutador (`soldadura_base_modo_tengo_oro` / `soldadura_base_modo_peso`, título de
  entrada `soldadura_base_entrada_peso`) y el modo inverso con
  `CalcularSoldaduraBaseInversaUseCase`: fila de oro 24K primero + las cuatro de liga +
  total; mismo contrato de limpieza al cambiar de modo (FR-003, FR-012, FR-023).
  **Depende de T046**.
- [ ] T049 [P] [US5] Ampliar los dos tests de ViewModel: cambiar de modo vacía cantidad y
  resultado y conserva selecciones; ORO LEY inverso con 10 g media → base «5,000», oro
  «5,000», total «10,000»; clásica inversa **TEST 1 formateado** (8 → «5,000»/«2,000»/
  «1,000» y total «8,000»); clásica muy floja de ley con 10 → «6,944»/«0,694»/«1,111»/
  «1,250» (suma visible 9,999: documenta §8.3 y FR-021); plata inversa 10 muy floja →
  «5,714»/«4,286»; base inversa 10 → oro «7,541» + «0,407»/«0,603»/«0,694»/«0,754»; base
  inversa 13,26 → oro «10,000». **Depende de T046 y T048**.
- [ ] T050 [P] [US5] Ampliar los dos tests de pantalla: el conmutador existe y propaga; en
  inverso la fila del metal de entrada sí se pinta; la nota de redondeo es visible junto al
  total. **Depende de T047 y T048**.

**Checkpoint**: la spec entera de cálculo cubierta — ambos modos en todas las familias.

---

## Fase 8 — User Story 6: limpiar y guardar en favoritos (P6)

**Objetivo**: los dos botones de acción en ambas pantallas, con el contrato de Limpiar y
el aviso de «Próximamente».

**Test independiente**: completar un cálculo, pulsar «Limpiar» (queda el formulario
inicial de la familia) y «Guardar en favoritos» (Toast, sin cambios de estado).

- [ ] T051 [US6] Ampliar los dos ViewModels con `onLimpiar()` —en soldaduras
  `SoldadurasUiState(familia = actual)` (conserva la familia, FR-024) y en la base
  `SoldaduraBaseUiState()`; ambos rearman la deduplicación— y `onGuardarFavoritos()` —solo
  telemetría: `soldaduras_favoritos_proximamente` / `soldadura_base_favoritos_proximamente`.
  **Depende de T049 (US5 en verde)**.
- [ ] T052 [US6] Ampliar las dos pantallas con la fila de `BotonDorado` (`ic_refrescar` +
  `accion_limpiar`, `ic_estrella` + `accion_guardar_favoritos`, `weight(1f)` cada uno); en
  soldaduras solo visible con familia elegida (FR-024); el Toast
  `R.string.aviso_proximamente` lo lanza el composable con estado, patrón plata (el
  ViewModel no conoce Android). **Depende de T051**.
- [ ] T053 [P] [US6] Ampliar los tests: `onLimpiar` en soldaduras equivale a
  `SoldadurasUiState(familia = actual)` y en la base al estado inicial; tras limpiar, el
  mismo cálculo vuelve a emitir telemetría; favoritos no altera el estado y emite su
  evento; en pantalla, sin familia no existen los botones y con familia sí; pulsar cada
  botón propaga su callback. **Depende de T051 y T052**.

---

## Fase 9 — Pulido y verificación

- [ ] T054 [P] Actualizar `CLAUDE.md`: los cuatro destinos pendientes pasan a tres
  (Favoritos, Ajustes y Herramientas); documentar el tercer motor (`RecetasSoldadura` como
  única fuente de verdad de §7, `HALF_UP` interno y de vista frente al DOWN de plata, los
  tres casos de uso de ley y el que no tiene UI), `ui/soldaduras/` como quinto ejemplo de
  pantalla (dos pantallas, un paquete), el parámetro `maxPorFila` de `SelectorSegmentado` y
  las cinco imágenes nuevas de `drawable-nodpi/`. **Depende de T053**.
- [ ] T055 Puertas finales: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`
  y en verde `./gradlew :app:testDebugUnitTest` (motor + ViewModels + `KoinModulesTest`),
  `./gradlew :app:lint` y `./gradlew :app:assembleDebug`. **Depende de T054**.
- [ ] T056 Verificación manual en emulador contra los criterios de la spec: SC-001 y
  SC-003 (los ejemplos numéricos en pantalla), SC-005 (avisos de seguridad), SC-006
  (primera visita), SC-007 (coma/punto), SC-008 (nota de redondeo con 10 g muy floja de
  ley), SC-009 (fuente al doble, 5 durezas legibles), SC-011 (sin andamiaje) y SC-012
  (comparación con mockups); y FR-018: ningún texto visible afirma que las recetas estén
  certificadas o verificadas metalúrgicamente. Anotar el resultado y las desviaciones en la sección
  «Resultado de la verificación» al final de este fichero. **Depende de T055**.

---

## Dependencias y orden de ejecución

- **Fase 1 (Setup)**: sin dependencias; T001 y T002 en paralelo.
- **Fase 2 (Fundacional)**: T003–T007 y T021 en paralelo desde el inicio; T008 tras T003;
  T009 tras T005–T008; T010 tras T008; T011 tras T004/T007; T012–T015 en paralelo tras
  T009–T011; T016 tras los casos de uso; T017–T020 en paralelo tras su familia; T022
  cierra la fase. **Bloquea todas las historias.**
- **Historias**: US1 → US2 → US3 → US4 → US5 → US6, en orden de prioridad. US2 solo depende
  de US1 por el cableado del botón (T035 completa lo que T028 deja con lambda vacía);
  US3–US6 amplían ficheros de US1, así que se ejecutan en secuencia.
- **Fase 9 (Pulido)**: tras la última historia.

## Estrategia de implementación

MVP = Fases 1 + 2 + US1: el módulo deja de ser andamiaje y el método estrella (oro de ley)
calcula en vivo. Cada historia posterior es un incremento verificable por sí solo con su
checkpoint; se puede parar en cualquiera de ellos con la app en verde. Las ampliaciones de
US3–US6 tocan los mismos ficheros (`SoldadurasViewModel/Screen` y sus tests), por eso van
en secuencia y no en paralelo.

## Notas

- Los valores de la base salen de `RecetasSoldadura` (§7); el mockup los muestra
  intercambiados y **no** es fuente (§12). TEST 6 (T019, T036) lo blinda.
- Ningún ingrediente se ajusta jamás para cuadrar la suma mostrada (§8.3, FR-021): la
  respuesta es la nota de redondeo, y T049 documenta el caso 9,999.
- El redondeo de vista es `HALF_UP` a 3 decimales — deliberadamente distinto del `DOWN` de
  `PlataViewModel` (allí protege una ley legal; aquí no hay ley que proteger).
- `CalcularSoldaduraLeyUseCase` (desde la base) no tiene UI en v1: existe y se prueba
  (T015, T020), precedente de la 005.
- Commit tras cada tarea o grupo lógico con Conventional Commits en español
  (`feat(006): …`, `test(006): …`, `docs(006): …`).
