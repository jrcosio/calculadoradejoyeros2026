# Tasks: Calculadora de aleaciones de oro

**Feature**: `004-aleaciones-oro` | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **Modelo**: [data-model.md](./data-model.md)

## Formato: `[ID] [P?] [Story] Descripción`

- **[P]**: paralelizable — ficheros distintos, sin dependencia con tareas pendientes.
- **[Story]**: historia de usuario a la que sirve (US1, US2, US3, US4). Las fases de Setup,
  Fundacional y Pulido no llevan etiqueta.
- Rutas de fichero relativas a la raíz del repositorio.

**Tests**: sí. La constitución (principio IV) exige test unitario por ViewModel **y por caso
de uso**, y el propio documento técnico exige tests de las 16 combinaciones (§20) y sus 5
casos numéricos (§13). Precedente de test instrumentado por pantalla desde la 002.

**Fuente de verdad numérica**: `UI_Plantillas/Feature_Oro/ESPECIFICACION_CALCULADORA_ALEACIONES_ORO.md`
(§ citados). Sus presets se transcriben tal cual; prohibido normalizarlos (§24, §26).

---

## Fase 1 — Setup: recursos compartidos

**Propósito**: dejar en `res/` y en el tema todo lo que la pantalla necesita pintar.

- [X] T001 [P] Redimensionar a 512 px las dos imágenes nuevas de `UI_Plantillas/Feature_Oro/` hacia `app/src/main/res/drawable-nodpi/` con `sips -Z 512 <origen> --out <destino>`: `cobre.png` → `cobre.png` y `paladio.png` → `paladio.png`. Los originales se quedan donde están, como plantilla de diseño. `modulo_oro.png` y `modulo_plata.png` ya existen y se reutilizan tal cual.
- [X] T002 [P] Dibujar siete vector drawables monocromos en `app/src/main/res/drawable/`: `ic_check.xml` (marca de selección), `ic_aviso.xml` (triángulo de advertencia), `ic_refrescar.xml` (limpiar), `ic_estrella.xml` (favoritos), `ic_balanza.xml` (total), `ic_lingotes.xml` (sección de ley) e `ic_paleta.xml` (sección de color). Viewport 24×24, trazo 1.5–1.8 coherente con `ic_info` e `ic_chevron`, color en blanco puro: se tiñen con `Icon(tint = …)`.
- [X] T003 [P] Añadir el bloque `<!-- Calculadora de aleaciones de oro -->` en `app/src/main/res/values/strings.xml`: `oro_entrada_titulo` («Introduce el oro de 24K»), `oro_entrada_unidad` («gr»), `oro_entrada_imagen` (descripción de los lingotes), `oro_seccion_ley` («Tipo de oro»), `oro_seccion_color` («Color del oro»), `oro_ley_18k`/`oro_ley_14k`/`oro_ley_12k`/`oro_ley_9k`, `oro_color_amarillo`/`oro_color_blanco`/`oro_color_rosa`/`oro_color_rojo`, `oro_metal_plata` («Plata fina»)/`oro_metal_cobre` («Cobre»)/`oro_metal_paladio` («Paladio»), descripciones de imagen de los tres metales, `oro_total` («Total de oro %1$s:»), `oro_aviso_12k` (texto de §2: 500‰ solo referencia técnica de cálculo, no ley oficial de comercialización en España), `oro_limpiar` («Limpiar»), `oro_guardar_favoritos` («Guardar en favoritos»), `oro_proximamente` («Próximamente») (FR-002, FR-005, FR-006, FR-008, FR-009, FR-013, FR-014, FR-015, FR-021).
- [X] T004 [P] Añadir en `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/theme/Type.kt` el estilo de cifra grande **fuera** de `Typography` (mismo precedente que `TitleSerif`): `val CifraGrande = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp)`. Manrope, no Playfair: la serif sigue reservada a la portada.

---

## Fase 2 — Fundacional: el motor de cálculo (bloquea a todas las historias)

**⚠️ CRÍTICO**: ninguna historia puede empezar hasta que esta fase esté completa. Estrena
`domain/model/`, `domain/usecase/` y el `domainModule`. Todo Kotlin puro: sin `android.*`,
sin `androidx.*`, sin `R`, con `java.math.BigDecimal` construido desde literales `String`.

- [X] T005 [P] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/ColorOro.kt`: `enum class ColorOro { AMARILLO, BLANCO, ROSA, ROJO }` con `val analyticsId: String get() = name.lowercase()`.
- [X] T006 [P] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/MetalLiga.kt`: `enum class MetalLiga { PLATA_FINA, COBRE, PALADIO }` con `analyticsId`. El orden del enum es el orden de pintado de las filas de resultado.
- [X] T007 [P] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/LeyOro.kt`: `enum class LeyOro(val milesimas: Int, val esSoloTecnica: Boolean)` con `LEY_18K(750)`, `LEY_14K(585)`, `LEY_12K(500, esSoloTecnica = true)`, `LEY_9K(375)`; `val finura: BigDecimal` derivada de las milésimas (escala 3) y `val analyticsId` («18k», «14k», «12k», «9k»).
- [X] T008 Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/RecetaLiga.kt`: `data class RecetaLiga(val color: ColorOro, val ley: LeyOro, val proporciones: Map<MetalLiga, BigDecimal>)` que valida en su `init` que toda proporción es > 0 y que la suma dista de 1 menos de 1e-9 (los literales del blanco 750 suman 0,9999999999999999; §12 admite tolerancia computacional). **Depende de T005–T007**.
- [X] T009 Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/RecetasOro.kt`: `object RecetasOro` con las **16 recetas** transcritas literalmente de §7 del documento (los literales exactos están en la tabla de [data-model.md](./data-model.md)), como `BigDecimal("…")` desde `String`, y `fun receta(color: ColorOro, ley: LeyOro): RecetaLiga`. Única fuente de verdad (§15); comentario con `recipes_version 1.0` y la prohibición de normalizar (§24, §26). **Depende de T008**.
- [X] T010 [P] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/CalculoAleacion.kt`: `data class CalculoAleacion(val masaOrigen: BigDecimal, val oroPuro: BigDecimal, val ligaTotal: BigDecimal, val metales: Map<MetalLiga, BigDecimal>, val masaFinal: BigDecimal, val leyTeorica: BigDecimal)`.
- [X] T011 Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/usecase/CalcularAleacionOroUseCase.kt` (modo directo, §9): `operator fun invoke(masaOrigen: BigDecimal, color: ColorOro, ley: LeyOro): CalculoAleacion`. Constantes: `FINURA_ORIGEN = BigDecimal("0.999")` (§3.1), escala 15. `require(masaOrigen > 0)`; `oroPuro = masaOrigen × 0.999` (exacto); `masaFinal = oroPuro ÷ finura` con `RoundingMode.DOWN` a escala 15 — a la baja para que la ley real nunca quede por debajo de la objetivo (§12); `ligaTotal = masaFinal − masaOrigen`; cada metal `= ligaTotal × proporción` (multiplicación exacta, sin redondeo intermedio, §10); `leyTeorica = oroPuro ÷ (masaOrigen + Σ metales)` (escala 15, DOWN). Verificación final §12: `Σ metales` dista de `ligaTotal` menos de 1e-9 y `leyTeorica >= finura`; si falla, `error(...)` — no puede ocurrir, es la red de seguridad. **Depende de T009 y T010**.
- [X] T012 Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/usecase/CalcularAleacionInversaOroUseCase.kt` (modo inverso, §14, FR-016): `operator fun invoke(masaFinalDeseada: BigDecimal, color: ColorOro, ley: LeyOro): CalculoAleacion` donde `masaOrigen = masaFinalDeseada × finura ÷ 0.999` (escala 15, DOWN) y `ligaTotal = masaFinalDeseada − masaOrigen`, repartida con la misma receta y las mismas verificaciones. Sin UI en esta versión. **Depende de T009 y T010**.
- [X] T013 Registrar los dos casos de uso en `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/core/di/DomainModule.kt` con `factoryOf(::CalcularAleacionOroUseCase)` y `factoryOf(::CalcularAleacionInversaOroUseCase)` — primeros registros del módulo; `KoinModulesTest` los cubre solo. **Depende de T011 y T012**.
- [X] T014 [P] Crear `app/src/test/java/com/jrblanco/calculadoradejoyeros2021/domain/usecase/CalcularAleacionOroUseCaseTest.kt` (JUnit4, nombres en backticks y en español, sin mocks: el motor es puro): los **5 casos de §13** con sus valores exactos (comparaciones `BigDecimal` con tolerancia 1e-6); las **16 combinaciones** color×ley recorridas con `ColorOro.entries × LeyOro.entries` verificando los 7 puntos de §20 (liga > 0, metales ≥ 0, suma de proporciones ≈ 1, Σ metales ≈ liga, masa final = origen + liga, ley teórica ≥ objetivo, y que el redondeo de vista a 3 decimales no altera el valor interno); la tabla maestra de §8 por muestreo (amarillo 750, blanco 585, rosa 500, rojo 375 con 100 g); y las validaciones §16 (masa 0 y negativa lanzan `IllegalArgumentException`). **Depende de T011**.
- [X] T015 [P] Crear `app/src/test/java/com/jrblanco/calculadoradejoyeros2021/domain/usecase/CalcularAleacionInversaOroUseCaseTest.kt`: el ejemplo de §14 (20 g finales de amarillo 750 → Au999 15.015015015 g y liga 4.984984985 g), las 16 combinaciones con los mismos invariantes (masa final deseada recuperada, ley ≥ objetivo) y las validaciones de masa ≤ 0. **Depende de T012**.

**Checkpoint**: motor completo y en verde en JVM. `./gradlew :app:testDebugUnitTest` pasa
con los dos tests nuevos y `KoinModulesTest` verifica el `domainModule` estrenado.

---

## Fase 3 — User Story 1: calcular la liga de una aleación de oro (P1) 🎯 MVP

**Objetivo**: el módulo de oro deja de ser andamiaje y calcula en vivo los metales de liga
y el total para cualquier cantidad, ley y color.

**Test independiente**: abrir Home → «Aleaciones de ORO», introducir 10 g con 18K amarillo
y ver plata 2,191 / cobre 1,129 / total 13,320; cambiar ley, color y cantidad y ver el
recálculo inmediato; borrar la cantidad y ver desaparecer los resultados.

- [X] T016 [P] [US1] Promover la tarjeta del sistema a `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/components/Tarjetas.kt`: `@Composable fun TarjetaAcento(modifier: Modifier = Modifier, acento: Color = JewelryColors.GoldPrimary, contenido: @Composable ColumnScope.() -> Unit)` — mismo degradado horizontal `acento 0.14f → Surface`, borde 1 dp `acento 0.65f`, `RoundedCornerShape(JewelryRadius.Large)`, padding `JewelrySpacing.Md` que hoy tiene la `TarjetaDorada` privada de `ui/info/InfoScreen.kt`. Borrar la privada y hacer que Info use la compartida con el acento por defecto; la pantalla de información debe verse idéntica (mismo movimiento que `DiamondDivider` en la 003).
- [X] T017 [P] [US1] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/components/SelectorSegmentado.kt`: `@Composable fun SelectorSegmentado(opciones: List<String>, seleccionada: Int, onSeleccion: (Int) -> Unit, modifier: Modifier = Modifier, acento: Color = JewelryColors.GoldPrimary)`. Hecho a mano (sin `SegmentedButton` de Material, que impone geometría — precedente `JewelryBottomBar`): `Row` sobre `Surface` con borde `JewelryColors.Border` y `RoundedCornerShape(JewelryRadius.Small)`; cada opción `weight(1f)`, alto ≥ 48 dp (`JewelrySize.MinTouchTarget`), `clickable(role = Role.RadioButton)` sin onda y `selectableGroup()`/`selected` en semántica; la activa pinta píldora con degradado vertical del acento (`GoldSoft→GoldPrimary→GoldSecondary` si es dorado; aclarado→`acento`→oscurecido en general), `ic_check` en círculo y texto en `Background`; las inactivas texto `TextSecondary` (FR-005, FR-006, FR-020).
- [X] T018 [US1] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/oro/OroUiState.kt`: `data class OroUiState(val cantidadTexto: String = "", val ley: LeyOro = LeyOro.LEY_18K, val color: ColorOro = ColorOro.AMARILLO, val resultado: ResultadoOro? = null)` y `data class ResultadoOro(val metales: List<MetalCalculado>, val totalFormateado: String)` con `data class MetalCalculado(val metal: MetalLiga, val gramosFormateados: String)` — cifras **ya formateadas** (3 decimales, coma) en el orden del enum `MetalLiga`; el aviso de 12K se deriva de `ley.esSoloTecnica`, sin campo propio. Importa `domain.model`, jamás `androidx.compose.*`.
- [X] T019 [US1] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/oro/OroViewModel.kt`: `class OroViewModel(private val calcularAleacion: CalcularAleacionOroUseCase, private val analytics: AnalyticsRepository) : ViewModel()` con un único `StateFlow<OroUiState>` y `analytics.logScreenView("oro")` en el `init` — el mismo nombre que emitía el placeholder (FR-019). Métodos: `onCantidadCambiada(texto)`, `onLeySeleccionada(ley)`, `onColorSeleccionado(color)`; cada uno actualiza el estado y recalcula (FR-007): parseo normalizando coma→punto con `toBigDecimalOrNull()`, inválido o ≤ 0 → `resultado = null` sin error visible (FR-003, FR-004); válido → invocar el use case y formatear con `setScale(3, RoundingMode.HALF_UP)` + coma decimal (FR-012), sin realimentar jamás el cálculo con lo formateado (§21). Telemetría `oro_calculado` con `mapOf("ley" to ley.analyticsId, "color" to color.analyticsId)` **deduplicada**: solo cuando un cálculo válido estrena combinación ley×color o la entrada pasa de inválida a válida — nunca por cada tecla y nunca con la cantidad (FR-019). Cálculo síncrono: sin corrutinas ni `DispatcherProvider`.
- [X] T020 [US1] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/oro/OroScreen.kt` con los dos composables: `OroScreen(onInfo, onBack, modifier, viewModel = koinViewModel())` que resuelve el estado con `collectAsStateWithLifecycle()`, y `OroContent(uiState, onCantidadCambiada, onLeySeleccionada, onColorSeleccionado, …)` sin estado y con `@Preview(widthDp = 411, heightDp = 891)` privado mostrando un cálculo de blanco 18K. Chrome: `JewelryScaffold(onInfo = onInfo, title = stringResource(R.string.modulo_oro_titulo), onBack = onBack)` — sección de módulo: sin barra inferior (FR-017). Contenido en `Column` con `verticalScroll(rememberScrollState())` e `imePadding()` para que el teclado no tape los resultados (FR-018, con `adjustResize` ya en el manifest):
  1. `TarjetaAcento` de entrada: `modulo_oro.png` (112 dp, `contentDescription = oro_entrada_imagen`), `oro_entrada_titulo` en `titleMedium`/`GoldSoft`, y `BasicTextField` a mano — `CifraGrande` centrado en `TextPrimary`, cursor dorado, `KeyboardOptions(keyboardType = KeyboardType.Decimal)`, sufijo «gr» en `GoldPrimary`, caja con borde `BorderGold` y `RoundedCornerShape(JewelryRadius.Medium)` (FR-002, FR-003).
  2. Cabecera de sección `ic_lingotes` + `oro_seccion_ley` y `SelectorSegmentado` de leyes con acento dorado (FR-005).
  3. Cabecera `ic_paleta` + `oro_seccion_color` y `SelectorSegmentado` de colores con acento `TealPrimary` (FR-006).
  4. Si `resultado != null`: `TarjetaAcento(acento = TealPrimary)` con una `FilaMetal` privada por metal — imagen (`modulo_plata.png`/`cobre.png`/`paladio.png`, 44 dp, con descripción), nombre en `bodyLarge`/`TextPrimary`, línea de puntos `TealDark` con `weight(1f)`, cifra en `CifraGrande`/`TealPrimary` y «gr» en `GoldPrimary` (FR-008); debajo, `TarjetaAcento` de total: `ic_balanza` en círculo dorado, `oro_total` con el nombre del color y el total en `CifraGrande`/`GoldPrimary` (FR-009).
  El mapeo enum→recursos (`LeyOro.etiquetaRes`, `ColorOro.etiquetaRes`, `MetalLiga.imagenRes/nombreRes/descripcionRes`) vive aquí como funciones privadas, estilo `ModulePresentation` de Home. **Depende de T016–T019**.
- [X] T021 [US1] En `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/navigation/AppNavHost.kt`, sustituir el cuerpo de `composable<Route.Oro>` por `OroScreen(onInfo = onInfo, onBack = onBack)` (FR-001). `Routes.kt` no se toca; `PlaceholderScreen` sigue sirviendo a los demás destinos pendientes.
- [X] T022 [US1] Registrar `viewModelOf(::OroViewModel)` en `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/core/di/ViewModelModule.kt`. Sin módulo nuevo: `KoinModulesTest` lo cubre solo.
- [X] T023 [P] [US1] Crear `app/src/test/java/com/jrblanco/calculadoradejoyeros2021/ui/oro/OroViewModelTest.kt` con MockK (`relaxed`) + Turbine + `runTest`, usando el use case **real** (motor puro, sin mockearlo) y `AnalyticsRepository` mockeado: estado inicial (campo vacío, 18K, amarillo, sin resultado); `logScreenView("oro")` exactamente una vez al construirse; «10» con 18K amarillo produce plata «2,191», cobre «1,129» y total «13,320» en ese orden; «12,35» y «12.35» producen el mismo resultado (SC-009); vacío, «0», «-1» y «abc» dejan `resultado` a `null`; cambiar ley o color recalcula; `oro_calculado` se registra una vez por combinación estrenada y no una por tecla (teclear «1», «10», «100» con la misma combinación = 1 evento).
- [X] T024 [P] [US1] Crear `app/src/androidTest/java/com/jrblanco/calculadoradejoyeros2021/ui/oro/OroScreenTest.kt` montando `OroContent` directo dentro de `Calculadoradejoyeros2021Theme`, sin Koin ni NavHost: con un `OroUiState` de blanco 18K se muestran las tres filas de metal con sus nombres y cifras y el total con «BLANCO»; con `resultado = null` no hay filas; pulsar una opción de ley y una de color propaga el valor esperado al callback; escribir en el campo propaga el texto.

**Checkpoint**: US1 funciona sola — la calculadora completa es usable de punta a punta.

---

## Fase 4 — User Story 2: advertencia de 12K (P2)

**Objetivo**: con 500‰ seleccionado, el joyero ve siempre que no es ley oficial española.

**Test independiente**: seleccionar 12K y ver el aviso; seleccionar cualquier otra ley y
ver que desaparece.

- [X] T025 [US2] Añadir a `OroContent` en `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/oro/OroScreen.kt` el aviso privado `AvisoLeyTecnica`: visible solo cuando `uiState.ley.esSoloTecnica`, justo bajo el selector de leyes — `ic_aviso` y texto `oro_aviso_12k` en `bodyMedium`/`JewelryColors.Warning` sobre caja `SurfaceWarm` con borde `Warning 0.65f`, y `liveRegion = LiveRegionMode.Polite` en semántica para que el lector lo anuncie al aparecer (FR-013, FR-020). Añadir al `@Preview` una variante con 12K.
- [X] T026 [P] [US2] Ampliar `app/src/test/java/com/jrblanco/calculadoradejoyeros2021/ui/oro/OroViewModelTest.kt`: solo `LEY_12K` tiene `esSoloTecnica`, y seleccionarla no impide el cálculo (99,800 g de liga con 100 g).
- [X] T027 [P] [US2] Ampliar `app/src/androidTest/java/com/jrblanco/calculadoradejoyeros2021/ui/oro/OroScreenTest.kt`: con 12K el nodo del aviso existe con su texto; con 18K no existe (SC-004).

**Checkpoint**: US1 y US2 conviven; 500‰ jamás se presenta como oficial.

---

## Fase 5 — User Story 3: limpiar y empezar de nuevo (P3)

**Objetivo**: un toque devuelve la pantalla a su estado inicial.

**Test independiente**: completar un cálculo, pulsar «Limpiar» y comprobar que el campo
queda vacío, la selección vuelve a 18K amarillo y no hay resultados.

- [X] T028 [US3] Añadir `fun onLimpiar()` a `OroViewModel` — repone `OroUiState()` íntegro y resetea la deduplicación de `oro_calculado` (la siguiente combinación válida vuelve a registrarse) — y el botón «Limpiar» a `OroContent`: fila inferior con botón a mano (precedente del botón de portada: caja dorada `RoundedCornerShape(JewelryRadius.Medium)`, alto `JewelrySize.PrimaryButtonHeight`, `ic_refrescar` + `oro_limpiar` en `Background`, `clickable(role = Role.Button)`) (FR-014).
- [X] T029 [P] [US3] Ampliar los dos tests: en `OroViewModelTest`, tras un cálculo completo `onLimpiar()` devuelve exactamente el estado inicial y el siguiente cálculo vuelve a emitir telemetría; en `OroScreenTest`, el botón «Limpiar» existe y pulsar propaga el callback.

**Checkpoint**: US1–US3 conviven.

---

## Fase 6 — User Story 4: guardar en favoritos, «Próximamente» (P4)

**Objetivo**: el botón existe, avisa de que la función llegará y no rompe nada.

**Test independiente**: pulsar «Guardar en favoritos» y ver el aviso efímero sin que el
cálculo cambie.

- [X] T030 [US4] Añadir `fun onGuardarFavoritos()` a `OroViewModel` — solo registra `analytics.logEvent("oro_favoritos_proximamente")`, sin mutar estado — y el botón «Guardar en favoritos» a `OroContent`, junto a «Limpiar» (mismo estilo dorado, `ic_estrella`). En `OroScreen` (el composable con estado), al pulsarlo: `viewModel.onGuardarFavoritos()` y `Toast.makeText(context, R.string.oro_proximamente, Toast.LENGTH_SHORT).show()` con `LocalContext.current` — los `Toast` del sistema se reemplazan solos, no se acumulan (FR-015). El ViewModel no conoce el `Toast`.
- [X] T031 [P] [US4] Ampliar los dos tests: en `OroViewModelTest`, `onGuardarFavoritos()` registra el evento exactamente una vez por pulsación y el estado no cambia; en `OroScreenTest`, el botón existe y pulsar propaga el callback.

**Checkpoint**: todas las historias funcionan de forma independiente.

---

## Fase 7 — Pulido y verificación

- [X] T032 [P] Actualizar `CLAUDE.md`: «Pantallas aún sin desarrollar» pasa de seis destinos a cinco; documentar que `domain/model/` y `domain/usecase/` quedan estrenados (motor `BigDecimal`, recetas como única fuente de verdad, `domainModule` en uso) y añadir `SelectorSegmentado` y `TarjetaAcento` a la lista de componentes compartidos (con la nota de que `TarjetaAcento` sale de Info). Lo exige la constitución al cambiar la estructura.
- [X] T033 `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"` y `./gradlew :app:testDebugUnitTest` y `./gradlew :app:assembleDebug` en verde. `KoinModulesTest` debe verificar el `domainModule` estrenado; los tests de las features 001–003 siguen pasando.
- [X] T034 `./gradlew :app:lint` sin avisos nuevos (atención a `UnusedResources`: todos los iconos y strings de T002/T003 deben quedar en uso).
- [X] T035 Comprobación visual en emulador contra `UI_Plantillas/Feature_Oro/feature_oro_ejemplo.png` (SC-005): tarjeta de entrada con lingotes y cifra grande, selectores con check (dorado/teal), filas de metal con imagen + puntos + cifra teal + «gr» dorado, total con balanza, botones dorados. Recordatorio: las **cifras** del mockup no son referencia — la matemática la fija el documento técnico (con 50 g y blanco 18K: plata 6,564 / cobre 2,690 / paladio 7,346 / total 66,600). Recorrido: Home → Oro → atrás vuelve a Home; salir y volver deja la pantalla limpia.
- [X] T036 Comprobación de accesibilidad (SC-006, SC-007): con TalkBack, los selectores anuncian opción y estado de selección, el campo anuncia propósito y unidad, cada fila anuncia metal y gramos, y el aviso de 12K se anuncia al aparecer; con la fuente del sistema al doble, nada se recorta y todo se alcanza con desplazamiento; toques ≥ 48 dp.
- [X] T037 Verificar en Firebase DebugView que llegan `screen_view` con `oro`, `oro_calculado` con `ley` y `color` (y sin cantidad), y `oro_favoritos_proximamente`.
- [X] T038 `./gradlew :app:connectedDebugAndroidTest` en verde.
- [X] T039 `./gradlew :app:assembleRelease`: R8 no rompe el motor (`BigDecimal` es JDK; sin reglas keep nuevas). Anotar el crecimiento del APK por las dos imágenes.

---

## Dependencias y orden de ejecución

- **Fase 1 (Setup)**: sin dependencias; T001–T004 en paralelo.
- **Fase 2 (Fundacional)**: T005–T007 en paralelo → T008 → T009; T010 en paralelo con T008–T009; T011 y T012 tras T009+T010; T013 tras T011+T012; T014 ∥ T015 al final. **Bloquea todas las historias.**
- **US1 (Fase 3)**: T016 ∥ T017 ∥ T018 → T019 → T020 → T021 ∥ T022 → T023 ∥ T024. Depende solo de la fundacional.
- **US2 (Fase 4)**: depende de T020 (la pantalla existe). T026 ∥ T027 tras T025.
- **US3 (Fase 5)** y **US4 (Fase 6)**: dependen de T019+T020; entre sí independientes (T028/T030 tocan los mismos ficheros — hacerlas en orden, no en paralelo).
- **Fase 7**: tras todas las historias; T032 puede adelantarse en paralelo.

## Estrategia de implementación

**MVP = Fase 1 + Fase 2 + US1**: con eso la calculadora ya resuelve el trabajo del taller
de punta a punta y es validable contra los casos de §13. US2 (aviso legal) entra antes de
cualquier publicación; US3 y US4 son incrementos pequeños sobre la misma pantalla. Parar
en cada checkpoint y validar la historia sola.

## Notas

- [P] = ficheros distintos sin dependencias pendientes. US3 y US4 comparten ficheros: secuenciales.
- El motor no se mockea en ningún test: es puro y determinista; solo se mockea `AnalyticsRepository`.
- Prohibido redondear pasos intermedios (§10) y prohibido meter `String`/primitivos en el grafo de Koin (regla documentada en el proyecto).
- Commit por tarea o grupo lógico, Conventional Commits en español.

---

## Resultado de la verificación (2026-08-23, emulador Pixel_10 API 36)

- **Puertas de calidad**: `:app:testDebugUnitTest` en verde (44 tests: 15 del motor, 13 de
  `OroViewModel`, más los de las features 001–003 y `KoinModulesTest` verificando el
  `domainModule` estrenado); `:app:assembleDebug`, `:app:lint` (sin avisos nuevos; los 11
  que salen son preexistentes del proyecto: versiones de dependencias, iconos del
  launcher y manifiesto), `:app:connectedDebugAndroidTest` (22 tests,
  9 nuevos de `OroScreenTest`) y `:app:assembleRelease` con subida de mapping a
  Crashlytics, todo en verde.
- **T035 visual**: pantalla verificada contra el mockup en emulador — tarjeta de entrada
  con lingotes y cifra grande, selectores con píldora y check (dorado/teal), filas de
  metal con imagen + línea de puntos + cifra teal + «gr» dorado, total con balanza y
  botonera dorada. Con 50 g de blanco 18K: plata 6,564 / cobre 2,689 / paladio 7,346 /
  total 66,600 — los valores correctos del documento técnico (el mockup traía plata y
  cobre intercambiados, como recoge la spec). Recálculo reactivo comprobado al cambiar a
  12K (44,202 / 5,698 / 99,900, sin fila de cobre); «Limpiar» repone el estado inicial;
  «Guardar en favoritos» muestra el toast «Próximamente»; salir y volver arranca limpio.
- **T036 accesibilidad**: con la fuente del sistema al doble nada queda inaccesible; las
  etiquetas del selector se auto-ajustan (`TextAutoSize`, ajuste hecho durante esta
  verificación al detectar que «Amarillo» se recortaba). Objetivos táctiles ≥ 48 dp por
  construcción (`MinTouchTarget`, `PrimaryButtonHeight`). Semántica verificada por
  volcado: descripciones de imagen presentes, selectores como `RadioButton` con estado,
  filas de resultado fusionadas, aviso de 12K como región viva. Pendiente solo la pasada
  manual con TalkBack activo.
- **T037 telemetría**: verificada en logcat con `FA VERBOSE` (equivalente local a
  DebugView): llegan `screen_view` con `ga_screen=oro`, `oro_calculado` con `ley=18k` y
  `color=amarillo` (un solo evento pese a teclear la cantidad dígito a dígito) y
  `oro_favoritos_proximamente`. La cantidad introducida no viaja en ningún evento.
- **T039**: `app-release-unsigned.apk` = 7,3 MB (las dos imágenes nuevas suman ~460 KB
  en recursos). R8 no rompe el motor.

---

## Fase 8 — Enmienda: cada color de oro se elige en su tono

Retoque pedido por el autor tras la verificación: el selector de color pintaba las
cuatro opciones en teal; ahora cada una usa el tono de su oro.

- [X] T040 Añadir a `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/theme/Color.kt` los tokens `RoseGold` (`0xFFEB90A8`) y `RedGold` (`0xFFE85D45`), aclarados respecto al oro rosa y rojo reales para que el texto oscuro de la píldora siga contrastando. Amarillo reutiliza `GoldPrimary` y blanco se queda en `TealPrimary`, que es el que gustaba.
- [X] T041 Mover el acento de `SelectorSegmentado` de la fila a la opción: nuevo `data class OpcionSegmento(etiqueta, acento = GoldPrimary)` en `ui/components/SelectorSegmentado.kt`, y en `ui/oro/OroScreen.kt` mapear `ColorOro.acento` (amarillo → dorado, blanco → teal, rosa → `RoseGold`, rojo → `RedGold`). Las leyes siguen todas en dorado usando el valor por defecto: lo que distingue a una ley no es un color. La API de `OroContent` no cambia, así que los tests siguen valiendo tal cual.

- [X] T042 Extender el tono del oro elegido a la tarjeta de resultados en `ui/oro/OroScreen.kt`: `TarjetaAcento` toma `uiState.color.acento`, y `FilaMetal` recibe ese acento para la cifra y para la línea de puntos (al 55 % de opacidad, para que guíe sin competir con la cifra); `LineaPunteada` pasa a recibir su color en lugar de tenerlo cableado. La tarjeta de total, las unidades «gr» y los botones se quedan en dorado: es el color de «valor y acción principal» de la paleta, y con amarillo seleccionado las dos tarjetas se siguen distinguiendo por composición y borde.

**Verificado** (emulador Pixel_10): las cuatro píldoras se pintan en su tono con el
check y la etiqueta legibles en oscuro sobre cada uno, y la tarjeta de resultados
acompaña al color elegido (comprobado en amarillo, rosa y rojo, con el total siempre
en dorado); 44 tests unitarios y 22 instrumentados siguen en verde.
