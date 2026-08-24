# Tasks: Calculadora de aleaciones de plata

**Feature**: `005-aleaciones-plata` | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **Modelo**: [data-model.md](./data-model.md)

## Formato: `[ID] [P?] [Story] Descripción`

- **[P]**: paralelizable — ficheros distintos, sin dependencia con tareas pendientes.
- **[Story]**: historia de usuario a la que sirve (US1, US2, US3, US4). Las fases de Setup,
  Fundacional y Pulido no llevan etiqueta.
- Rutas de fichero relativas a la raíz del repositorio.

**Tests**: sí. La constitución (principio IV) exige test unitario por ViewModel **y por caso
de uso**, el documento técnico exige sus 4 casos numéricos (§21) y la verificación de la ley
práctica (§20), y hay precedente de test instrumentado por pantalla desde la 002.

**Fuente de verdad numérica**: `UI_Plantillas/Feature_plata/ESPECIFICACION_CALCULADORA_ALEACIONES_PLATA.md`
(§ citados). Los valores exactos ya están calculados en [data-model.md](./data-model.md).

**Aviso de alcance**: esta feature toca la 004. La fase 2 saca de `ui/oro/OroScreen.kt`
siete composables privados y renombra siete strings del namespace `oro_`. Es extracción
pura, sin cambio de comportamiento, y T016 es la puerta que lo verifica antes de que
empiece cualquier historia.

---

## Fase 1 — Setup: strings

**Propósito**: dejar en `res/values/strings.xml` el vocabulario compartido y el de la feature.

- [ ] T001 Sacar del namespace `oro_` los strings que van a consumir componentes compartidos, en `app/src/main/res/values/strings.xml`, y actualizar sus usos en `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/oro/OroScreen.kt` y `app/src/androidTest/java/com/jrblanco/calculadoradejoyeros2021/ui/oro/OroScreenTest.kt` (son los únicos tres ficheros que los referencian): `oro_entrada_unidad` → `unidad_gramos`; `oro_metal_plata` → `metal_plata_fina`, `oro_metal_cobre` → `metal_cobre`, `oro_metal_paladio` → `metal_paladio` y sus tres `_imagen` a `metal_*_imagen`; `oro_limpiar` → `accion_limpiar`, `oro_guardar_favoritos` → `accion_guardar_favoritos`, `oro_proximamente` → `aviso_proximamente`. Los que siguen siendo propios de oro (`oro_entrada_titulo`, `oro_seccion_*`, `oro_ley_*`, `oro_color_*`, `oro_total`, `oro_aviso_12k`) no se tocan. Colocar los renombrados en un bloque `<!-- Compartido: unidades, metales y acciones -->` antes del bloque de oro.
- [ ] T002 Añadir el bloque `<!-- Calculadora de aleaciones de plata -->` en `app/src/main/res/values/strings.xml`: `plata_entrada_titulo` («Introduce la plata fina 999‰»), `plata_entrada_imagen` («Dos lingotes de plata fina de 999 milésimas»), `plata_seccion_ley` («Milésimas de plata a obtener»), `plata_ley_950` («950»), `plata_ley_925` («925 (ley)»), `plata_ley_900` («900»), `plata_ley_800` («800 (ley)»), `plata_total` («Total de plata %1$s:»), `plata_aviso_950` y `plata_aviso_900` (los dos textos **literales** de §3 del documento técnico). El nombre del cobre y la unidad se toman de los compartidos de T001, no se duplican (FR-002, FR-005, FR-008, FR-013). **Depende de T001**.

---

## Fase 2 — Fundacional: el motor de plata y los componentes compartidos

**⚠️ CRÍTICO**: ninguna historia puede empezar hasta que esta fase esté completa. Son dos
bloques independientes entre sí —el motor (T003–T009) y los componentes (T010–T015)— que se
pueden atacar en paralelo y confluyen en T016.

Todo el motor es Kotlin puro: sin `android.*`, sin `androidx.*`, sin `R`, con
`java.math.BigDecimal` construido desde literales `String`.

- [ ] T003 [P] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/LeyPlata.kt`: `enum class LeyPlata(val milesimas: Int, val esSoloTecnica: Boolean = false)` con `LEY_950(950, esSoloTecnica = true)`, `LEY_925(925)`, `LEY_900(900, esSoloTecnica = true)`, `LEY_800(800)`; `val finura: BigDecimal get() = BigDecimal(milesimas).movePointLeft(3)` y `val analyticsId: String get() = milesimas.toString()`. KDoc citando §3 y §31: 925 y 800 son leyes oficiales de la Ley 17/1985 y 950 y 900 son presets técnicos que la interfaz debe advertir. El orden del enum es el orden de pintado del selector, de mayor a menor finura.
- [ ] T004 Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/model/CalculoPlata.kt`: `data class CalculoPlata(val masaOrigen: BigDecimal, val plataPura: BigDecimal, val cobre: BigDecimal, val masaFinal: BigDecimal, val leyTeorica: BigDecimal)`. En su `companion object`, las constantes propias `FINURA_ORIGEN = BigDecimal("0.999")` (§4: Ag999 nunca es Ag1000), `const val ESCALA = 15` y `TOLERANCIA = BigDecimal("1E-9")` —con comentario de que se repiten a propósito respecto a `CalculoAleacion` porque son dos documentos técnicos distintos— y la fábrica `internal fun de(masaOrigen: BigDecimal, masaFinal: BigDecimal, ley: LeyPlata): CalculoPlata` que calcula `plataPura = masaOrigen × 0.999` (exacto), `cobre = masaFinal − masaOrigen` y `leyTeorica = plataPura ÷ masaFinal` (escala 15, `DOWN`), y ejecuta las verificaciones obligatorias de §20 como `check`: `cobre > 0` y `leyTeorica >= ley.finura`, con mensaje que incluya los valores. Red de seguridad, no lógica de negocio. **Depende de T003**.
- [ ] T005 Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/usecase/CalcularAleacionPlataUseCase.kt` (modo directo, §9): `operator fun invoke(masaOrigen: BigDecimal, ley: LeyPlata): CalculoPlata`. `require(masaOrigen > BigDecimal.ZERO)` con mensaje (§26); `plataPura = masaOrigen × FINURA_ORIGEN`; `masaFinal = plataPura ÷ ley.finura` con `RoundingMode.DOWN` a escala 15 — comentario obligatorio de por qué a la baja: menos cobre deja la ley igual o por encima de la objetivo y la norma no admite tolerancia en menos (§16); delegar en `CalculoPlata.de(...)`. **Depende de T003 y T004**.
- [ ] T006 Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/domain/usecase/CalcularAleacionInversaPlataUseCase.kt` (modo inverso, §22, FR-016): `operator fun invoke(masaFinalDeseada: BigDecimal, ley: LeyPlata): CalculoPlata` donde `masaOrigen = masaFinalDeseada × ley.finura ÷ FINURA_ORIGEN` con `RoundingMode.UP` a escala 15 — comentario de por qué al alza: aquí lo que protege la ley es poner una pizca **más** de plata fina, no menos; delegar en `CalculoPlata.de(...)`. Sin UI en esta versión. **Depende de T003 y T004**.
- [ ] T007 Registrar los dos casos de uso en `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/core/di/DomainModule.kt` con `factoryOf(::CalcularAleacionPlataUseCase)` y `factoryOf(::CalcularAleacionInversaPlataUseCase)`; sin módulo nuevo, así que `core/di/AppModule.kt` no se toca y `KoinModulesTest` los cubre solo. **Depende de T005 y T006**.
- [ ] T008 [P] Crear `app/src/test/java/com/jrblanco/calculadoradejoyeros2021/domain/usecase/CalcularAleacionPlataUseCaseTest.kt` (JUnit4, nombres en backticks y en español, sin mocks: el motor es puro), con helper privado `assertCerca(esperado: String, real: BigDecimal)` de tolerancia `1E-6` como en los tests de oro: los **4 casos de §21** con 10 g (cobre 0,515789473684210 / 0,8 / 1,1 / 2,4875 y masas finales 10,515789473684210 / 10,8 / 11,1 / 12,4875); la **tabla de taller de §7** con 100 g (5,157894736842105 / 8 / 11 / 24,875); los **coeficientes de §6** comprobados como `cobre ÷ masaOrigen`; recorrido de `LeyPlata.entries` verificando los invariantes de §20 (`cobre > 0`, `masaFinal.compareTo(masaOrigen + cobre) == 0`, `leyTeorica >= ley.finura`, y que el redondeo de vista no muta el valor interno); el caso del mockup (25 g → 925‰ = 2 g de cobre exactos); una masa minúscula (0,001 g) que conserva precisión; y las validaciones de §26 (masa cero y negativa lanzan `IllegalArgumentException`). **Depende de T005**.
- [ ] T009 [P] Crear `app/src/test/java/com/jrblanco/calculadoradejoyeros2021/domain/usecase/CalcularAleacionInversaPlataUseCaseTest.kt`: el ejemplo de §23 (100 g finales de 925‰ → 92,592592592592593 g de plata fina y 7,407407407407407 g de cobre); las cuatro leyes con 100 g finales según la tabla de [data-model.md](./data-model.md), comprobando que la masa final deseada se recupera exacta (`compareTo == 0`), que `masaOrigen + cobre == masaFinalDeseada`, que hace falta menos plata fina que aleación final y que `leyTeorica >= ley.finura`; y las validaciones de masa ≤ 0. **Depende de T006**.
- [ ] T010 [P] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/components/Formularios.kt` moviendo desde `ui/oro/OroScreen.kt` los privados `CampoCantidad` y `CabeceraSeccion`, ya públicos y parametrizados: `CampoCantidad(valor, onCambio, modifier, acento: Color = JewelryColors.GoldPrimary, borde: Color = JewelryColors.BorderGold)` —el acento tiñe el cursor y el sufijo, que pasa a leer `R.string.unidad_gramos`— y `CabeceraSeccion(iconRes, titulo, modifier, tinte: Color = JewelryColors.GoldPrimary)`. Conservar los KDoc que explican por qué son `BasicTextField` y no `OutlinedTextField`, y el `semantics { heading() }`. **Depende de T001**.
- [ ] T011 [P] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/components/Botones.kt` moviendo desde `ui/oro/OroScreen.kt` el privado `BotonDorado(iconRes, texto, onClick, modifier)`, público y sin cambios de firma ni de aspecto: `Row` a mano con degradado `GoldSoft → GoldPrimary → GoldSecondary`, altura mínima `JewelrySize.PrimaryButtonHeight` y `role = Role.Button`. Conservar el KDoc de por qué no usa `Button` de Material.
- [ ] T012 [P] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/components/Avisos.kt` moviendo desde `ui/oro/OroScreen.kt` el privado `AvisoLeyTecnica`, ahora público y genérico: `AvisoTecnico(texto: String, modifier: Modifier = Modifier)`, con el mismo aspecto (`SurfaceWarm`, filete `Warning` al 65 %, `ic_aviso` de 22 dp) y la misma región viva `liveRegion = LiveRegionMode.Polite` para que el lector de pantalla la anuncie al aparecer. El texto sale por parámetro porque plata tiene dos avisos distintos y oro uno.
- [ ] T013 [P] Añadir a `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/components/Ornamentos.kt` el `LineaPunteada(color: Color, modifier: Modifier = Modifier)` que hoy es privado en `ui/oro/OroScreen.kt`, público y sin cambios: `Canvas` de 2 dp con `PathEffect.dashPathEffect`. Es un ornamento y este es el fichero donde viven, junto a `DiamondDivider` y `GoldHairline`.
- [ ] T014 Añadir a `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/components/Tarjetas.kt`, junto a `TarjetaAcento`, las dos versiones genéricas de lo que hoy es privado en oro: `FilaMetal(imagenRes: Int, imagenDescripcion: String, nombre: String, valorFormateado: String, acento: Color, modifier: Modifier = Modifier)` —imagen 44 dp, nombre, `LineaPunteada` al 55 % del acento, cifra en `CifraGrande` a 26 sp y «gr» de `R.string.unidad_gramos` en dorado, con `semantics(mergeDescendants = true)` para un solo anuncio por fila— y `TarjetaTotal(etiqueta: String, totalFormateado: String, modifier: Modifier = Modifier, acento: Color = JewelryColors.GoldPrimary)` —`TarjetaAcento` con `ic_balanza` en círculo, etiqueta, cifra y unidad—. **El acento tiñe el icono, su círculo, la cifra y la unidad; la etiqueta se queda en `JewelryColors.TextPrimary`**, que es como está hoy en oro: teñirla también cambiaría el aspecto de la pantalla de oro y haría fallar T016. Ninguna de las dos conoce `MetalLiga`, `ColorOro` ni `LeyPlata`: es cada pantalla la que mapea sus enums, como ya hace `MetalLiga.presentacion()`. **Depende de T001 y T013**.
- [ ] T015 Reescribir `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/oro/OroScreen.kt` para consumir los siete compartidos y **borrar sus copias privadas** (`CampoCantidad`, `CabeceraSeccion`, `BotonDorado`, `AvisoLeyTecnica`, `FilaMetal`, `LineaPunteada`, `TarjetaTotal`): la fila de metal pasa por `MetalLiga.presentacion()` y llama a `FilaMetal(...)` con los `stringResource` resueltos; `TarjetaTotal` recibe `stringResource(R.string.oro_total, stringResource(color.etiquetaRes).uppercase())` como etiqueta; el aviso de 12 K pasa a `AvisoTecnico(stringResource(R.string.oro_aviso_12k))`. El fichero se queda con `OroScreen`, `OroContent`, `TarjetaEntrada`, los mapeos privados de enums y las dos `@Preview`. La firma pública de `OroContent` **no cambia**, así que `OroScreenTest` sigue valiendo con solo el renombrado de T001. **Depende de T001, T010, T011, T012, T013 y T014**.
- [ ] T016 Puerta del refactor: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"` y comprobar que `./gradlew :app:testDebugUnitTest` y `./gradlew :app:assembleDebug` siguen en verde, y que las dos `@Preview` de oro renderizan igual. La 004 no puede quedar tocada en su comportamiento; si algo cambia visualmente, se corrige aquí y no más adelante. **Depende de T015**.

**Checkpoint**: motor de plata completo y en verde en JVM, `ui/components/` consolidado y la
calculadora de oro intacta. `KoinModulesTest` verifica los dos registros nuevos del
`domainModule`.

---

## Fase 3 — User Story 1: calcular el cobre de una aleación de plata (P1) 🎯 MVP

**Objetivo**: el módulo de plata deja de ser andamiaje y calcula en vivo el cobre a añadir y
el peso final para cualquier cantidad y cualquiera de las cuatro leyes.

**Test independiente**: abrir Home → «Aleaciones de PLATA», introducir 10 g con 925‰ y
comprobar 0,800 g de cobre y 10,800 g de total; recorrer las otras tres leyes contra §21.

- [ ] T017 [P] [US1] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/plata/PlataUiState.kt`: `data class PlataUiState(val cantidadTexto: String = "", val ley: LeyPlata = LeyPlata.LEY_925, val resultado: ResultadoPlata? = null)` y `data class ResultadoPlata(val cobreFormateado: String, val totalFormateado: String)`. KDoc: el constructor sin argumentos **es** el estado inicial (campo vacío, 925‰ por ser la Sterling y la del mockup, sin resultados); el aviso de ley técnica no tiene campo propio, se deriva de `LeyPlata.esSoloTecnica`; las cifras viajan ya formateadas y el valor exacto vive solo en el motor (§14, §21). **Depende de T003**.
- [ ] T018 [US1] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/plata/PlataViewModel.kt`: constructor `(private val calcularAleacion: CalcularAleacionPlataUseCase, private val analytics: AnalyticsRepository)`, `MutableStateFlow(PlataUiState())` privado expuesto con `asStateFlow()`, `init { analytics.logScreenView("plata") }` —el mismo nombre que emitía el placeholder, para conservar la serie histórica—, `onCantidadCambiada(texto)` y `onLeySeleccionada(ley)` que recalculan, `parsearCantidad` que normaliza coma a punto y descarta `null` o `<= 0` (§26, FR-003, FR-004), y `formatearGramos(valor) = valor.setScale(3, RoundingMode.DOWN).toPlainString().replace('.', ',')`. **El `DOWN` es obligatorio y va comentado**: es lo que hace que pesar la cifra mostrada nunca deje la aleación por debajo de la ley (§16-§17, FR-011); con `HALF_UP` fallarían dos de los cuatro casos de §21. Deduplicación de telemetría por última ley registrada: `plata_calculado` con `mapOf("ley" to ley.analyticsId)` se emite al estrenar ley o al volver la entrada a válida, nunca por tecla, y nunca con la cantidad (FR-019). Sin corrutinas: el cálculo es síncrono. **Depende de T005 y T017**.
- [ ] T019 [US1] Registrar `viewModelOf(::PlataViewModel)` en `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/core/di/ViewModelModule.kt`. **Depende de T018**.
- [ ] T020 [US1] Crear `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/plata/PlataScreen.kt` con los dos composables del contrato de pantalla: `PlataScreen(onInfo, onBack, modifier, viewModel = koinViewModel())` que resuelve el estado con `collectAsStateWithLifecycle()`, y `PlataContent(uiState, onCantidadCambiada, onLeySeleccionada, onLimpiar, onGuardarFavoritos, onInfo, onBack, modifier)` sin estado y con `@Preview(widthDp = 411, heightDp = 891)` privada. Dentro de `JewelryScaffold(onInfo, title = stringResource(R.string.modulo_plata_titulo), onBack)`, una `Column` con `verticalScroll(rememberScrollState())`, `imePadding()` y `Arrangement.spacedBy(JewelrySpacing.Md)` (FR-017, FR-018):
  1. `TarjetaEntrada` privada: `TarjetaAcento(acento = JewelryColors.SilverPrimary)` con `Image(modulo_plata, contentDescription = plata_entrada_imagen)` de 96 dp, título `plata_entrada_titulo` en `SilverPrimary` y `CampoCantidad(acento = SilverPrimary, borde = SilverDark)` (FR-002, FR-003).
  2. `CabeceraSeccion(ic_lingotes, plata_seccion_ley, tinte = SilverPrimary)` y `SelectorSegmentado` con las cuatro `LeyPlata.entries` mapeadas a `OpcionSegmento(stringResource(it.etiquetaRes), JewelryColors.TealPrimary)`, `seleccionada = uiState.ley.ordinal` (FR-005).
  3. Si `resultado != null`: `TarjetaAcento(acento = TealPrimary)` con una única `FilaMetal(R.drawable.cobre, stringResource(R.string.metal_cobre_imagen), stringResource(R.string.metal_cobre), resultado.cobreFormateado, TealPrimary)` (FR-006, FR-007); debajo, `TarjetaTotal(stringResource(R.string.plata_total, uiState.ley.milesimas.toString()), resultado.totalFormateado, acento = SilverPrimary)` (FR-008).
  4. `Row` con `Arrangement.spacedBy(JewelrySpacing.Md)` y los dos `BotonDorado` a `weight(1f)`: `ic_refrescar` + `accion_limpiar` y `ic_estrella` + `accion_guardar_favoritos`.

  **Sobre los dos últimos callbacks**: `onLimpiar` y `onGuardarFavoritos` son de US3 y US4, y sus métodos del ViewModel no existen todavía. `PlataContent` los declara en su firma desde ya y los botones quedan cableados a ellos, pero `PlataScreen` les pasa **lambdas vacías** hasta que T027 y T029 las sustituyan por las llamadas al ViewModel. Así US1 compila y se entrega sola, y las dos historias siguientes son un cambio de una línea cada una en el resolutor.

  El mapeo `LeyPlata.etiquetaRes` vive aquí como extension privada, para que `domain/` siga libre de Android, igual que `LeyOro.etiquetaRes` en oro. **Depende de T002, T010, T011, T014 y T017**.
- [ ] T021 [US1] Sustituir en `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/navigation/AppNavHost.kt` el `composable<Route.Plata> { PlaceholderScreen(...) }` por `composable<Route.Plata> { PlataScreen(onInfo = onInfo, onBack = onBack) }` y añadir el import. `Route.Plata` ya existe: no se toca `Routes.kt` (FR-001, FR-017). **Depende de T019 y T020**.
- [ ] T022 [P] [US1] Crear `app/src/test/java/com/jrblanco/calculadoradejoyeros2021/ui/plata/PlataViewModelTest.kt` con MockK (`relaxed`) + Turbine + `runTest`, usando el caso de uso **real** (motor puro, sin mockearlo) y `AnalyticsRepository` mockeado: estado inicial (campo vacío, 925‰, sin resultado); `logScreenView("plata")` exactamente una vez al construirse; los 4 casos de §21 con 10 g formateados («0,515»/«10,515», «0,800»/«10,800», «1,100»/«11,100», «2,487»/«12,487»); el caso del mockup (25 g → 925‰ = «2,000»/«27,000»); **el test de la desviación**: 100 g hacia 950‰ muestra «5,157» y no «5,158», y para cada ley y varias masas, reconstruir `plataPura ÷ (masaOrigen + cobreMostrado)` a partir de la **cadena formateada** y comprobar `>= ley.finura` (§20, FR-011, SC-003); el caso minúsculo del que habla la spec en sus casos límite: 0,001 g con 925‰ da `cobreFormateado == "0,000"` —el cobre exacto, 0,00008 g, queda por debajo de lo que pesa una balanza de milésimas— y la propiedad de ley práctica sigue cumpliéndose; «12,35» y «12.35» dan el mismo resultado (SC-010); vacío, «0», «-1», «abc», «1.2,3» y «  » dejan `resultado` a `null`; cambiar de ley recalcula sin tocar la cantidad; `plata_calculado` se registra una vez por ley estrenada y no una por tecla (teclear «1», «10», «100» con la misma ley = 1 evento) y vuelve a registrarse cuando la entrada pasa de inválida a válida. **Depende de T018**.
- [ ] T023 [P] [US1] Crear `app/src/androidTest/java/com/jrblanco/calculadoradejoyeros2021/ui/plata/PlataScreenTest.kt` montando `PlataContent` directo con `createComposeRule()`, sin Koin ni NavHost, con el helper `texto(id, vararg args)` de `InstrumentationRegistry`, igual que `OroScreenTest`: con resultado se pintan el nombre del cobre, su cifra, la unidad y el total con la ley; sin resultado no se pinta ninguna de las dos tarjetas; escribir en el campo propaga `onCantidadCambiada`; pulsar un segmento propaga `onLeySeleccionada` con la ley correcta. **Depende de T020**.

**Checkpoint**: US1 funciona sola — la calculadora de plata ya resuelve el trabajo de taller
de punta a punta y es validable contra los casos numéricos del documento.

---

## Fase 4 — User Story 2: advertencia de que 950 y 900 no son leyes oficiales españolas (P2)

**Objetivo**: ninguna de las dos leyes técnicas se puede elegir sin que la pantalla avise.

**Test independiente**: seleccionar 950‰ y comprobar su texto; seleccionar 900‰ y comprobar
que el texto es otro y menciona el 800‰; seleccionar 925‰ y 800‰ y comprobar que no aparece
ninguna advertencia.

- [ ] T024 [US2] Añadir a `PlataContent` en `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/plata/PlataScreen.kt`, justo debajo del selector de ley, `uiState.ley.avisoRes?.let { AvisoTecnico(stringResource(it)) }`, con una extension privada `private val LeyPlata.avisoRes: Int?` cuyo `when` exhaustivo devuelva `R.string.plata_aviso_950` para `LEY_950`, `R.string.plata_aviso_900` para `LEY_900` y `null` para las dos oficiales. Nulable y no `Int`: así el `when` cubre las cuatro ramas sin inventarles un aviso a 925 y 800, y el `?.let` sustituye al `if (esSoloTecnica)` en lugar de duplicar la condición. Un texto por ley, no uno genérico: cada uno sitúa su milésima respecto de las oficiales y esa es justo la información que evita contrastar mal (FR-013). **Depende de T012, T020**.
- [ ] T025 [P] [US2] Añadir a `ui/plata/PlataScreen.kt` una segunda `@Preview` privada con `LeyPlata.LEY_950` seleccionada y resultado presente, para ver el aviso en el render de diseño. Mismo precedente que las dos previews de oro (caso normal y caso con aviso). **Depende de T024**.
- [ ] T026 [P] [US2] Cubrir el aviso con tests: en `app/src/test/java/com/jrblanco/calculadoradejoyeros2021/ui/plata/PlataViewModelTest.kt`, que `esSoloTecnica` es cierto exactamente en `LEY_950` y `LEY_900` y falso en las dos oficiales, y que las dos técnicas calculan con normalidad; en `app/src/androidTest/java/com/jrblanco/calculadoradejoyeros2021/ui/plata/PlataScreenTest.kt`, que con 950‰ se pinta `plata_aviso_950`, con 900‰ se pinta `plata_aviso_900`, que los dos textos son distintos y que con 925‰ y 800‰ no se pinta ninguno de los dos. **Depende de T024**.

**Checkpoint**: US1 y US2 funcionan de forma independiente. La feature ya es publicable en
lo legal.

---

## Fase 5 — User Story 3: limpiar y empezar un cálculo nuevo (P3)

**Objetivo**: «Limpiar» devuelve la pantalla al estado de recién abierta.

**Test independiente**: completar un cálculo, pulsar «Limpiar» y comprobar campo vacío,
925‰ y sin resultados; volver a teclear y comprobar que calcula con normalidad.

- [ ] T027 [US3] Añadir `fun onLimpiar()` a `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/ui/plata/PlataViewModel.kt`, que repone `_uiState.value = PlataUiState()` y **rearma la deduplicación** poniendo a `null` la última ley registrada, y cablearlo desde `PlataScreen` al `BotonDorado` de `accion_limpiar` (FR-014). **Depende de T018 y T020**.
- [ ] T028 [US3] Cubrir limpiar con tests: en `PlataViewModelTest`, que tras un cálculo y un cambio de ley `onLimpiar()` deja el estado igual a `PlataUiState()` y que el siguiente cálculo válido vuelve a registrar telemetría; en `PlataScreenTest`, que pulsar el botón propaga `onLimpiar`. **Depende de T027**.

---

## Fase 6 — User Story 4: intentar guardar en favoritos (P4)

**Objetivo**: el botón existe, avisa de que la función llegará y no toca el cálculo.

**Test independiente**: pulsar «Guardar en favoritos», comprobar el aviso efímero y que la
pantalla no cambia de estado.

- [ ] T029 [US4] Añadir `fun onGuardarFavoritos()` a `ui/plata/PlataViewModel.kt`, que **solo** registra `analytics.logEvent("plata_favoritos_proximamente")` y no toca el estado, y en `PlataScreen` (el composable con estado, no `PlataContent`) disparar `Toast.makeText(context, R.string.aviso_proximamente, Toast.LENGTH_SHORT).show()` con `LocalContext.current` después de llamar al ViewModel. El ViewModel no conoce Android; los Toast se reemplazan solos y no se acumulan por muchas pulsaciones (FR-015). **Depende de T018 y T020**.
- [ ] T030 [US4] Cubrir favoritos con tests: en `PlataViewModelTest`, que dos pulsaciones registran dos eventos y el estado no cambia; en `PlataScreenTest`, que pulsar el botón propaga `onGuardarFavoritos`. El Toast no se testea: es del sistema. **Depende de T029**.

**Checkpoint**: las cuatro historias funcionan de forma independiente.

---

## Fase 7 — Pulido y verificación

- [ ] T031 `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"` y `./gradlew :app:testDebugUnitTest` y `./gradlew :app:assembleDebug` en verde. Los XML quedan en `app/build/test-results/testDebugUnitTest/`. Puerta de calidad de la constitución.
- [ ] T032 `./gradlew :app:lint` sin avisos nuevos respecto a los 11 preexistentes del proyecto. Atención especial a `UnusedResources` tras el renombrado de strings de T001: no debe quedar ningún `oro_*` huérfano ni ningún `plata_*` sin usar.
- [ ] T033 Verificación visual en emulador contra `UI_Plantillas/Feature_plata/ejemplo_feature_plata.png`: tarjeta de entrada con filete plateado, lingotes de plata y cifra grande; selector con la píldora teal, el check y «925 (ley)» / «800 (ley)» legibles; tarjeta de cobre en teal con imagen, línea de puntos y cifra; tarjeta de total en plateado con la balanza; botonera dorada. Recálculo reactivo al cambiar cantidad y ley. Comprobar también que salir del módulo y volver a entrar arranca limpio, sin memoria del cálculo anterior.
- [ ] T034 Verificación del truncado **en el dispositivo**, que es el criterio SC-003 y el corazón de FR-011: con 100 g y 950‰ la pantalla muestra **5,157 gr** de cobre y **105,157 gr** de total, no 5,158 / 105,158; con 10 g y 800‰ muestra **2,487** y no 2,488. Anotar el resultado en este fichero.
- [ ] T035 Accesibilidad: con la fuente del sistema al doble ningún texto se recorta y todo el contenido sigue alcanzable (las etiquetas del selector encogen solas por el `TextAutoSize` que ya trae `SelectorSegmentado`); objetivos táctiles ≥ 48 dp; volcado de semántica comprobando descripciones de imagen, selector como `RadioButton` con estado, fila de cobre y total fusionados en un solo anuncio, y el aviso como región viva (FR-020, SC-007, SC-008).
- [ ] T036 Telemetría en logcat con `FA VERBOSE`: llega `screen_view` con `ga_screen=plata`, `plata_calculado` con `ley=925` una sola vez al teclear la cantidad dígito a dígito, un evento nuevo al estrenar otra ley, y `plata_favoritos_proximamente` al pulsar el botón. La cantidad introducida **no** viaja en ningún evento (FR-019).
- [ ] T037 `./gradlew :app:connectedDebugAndroidTest` en verde, incluidos los tests nuevos de `PlataScreenTest` y los de `OroScreenTest` con los strings renombrados.
- [ ] T038 Actualizar `CLAUDE.md`: los cinco destinos pendientes pasan a cuatro (`ui/placeholder/` ya solo sirve a Favoritos, Ajustes, Soldaduras y Herramientas); documentar el motor de plata en `domain/` y que **no** tiene tabla de recetas porque §28 exige la fórmula general; documentar los siete composables que han subido a `ui/components/` y en qué fichero ha quedado cada uno; añadir `ui/plata/` como cuarta pantalla de referencia; y dejar escrita la asimetría deliberada de redondeo — **oro formatea con `HALF_UP` y plata trunca con `DOWN`**, porque en plata la Ley 17/1985 no admite tolerancia en menos y la cifra mostrada es la que se pesa.
- [ ] T039 `./gradlew :app:assembleRelease` en verde y anotar el tamaño del APK. No debería crecer apenas: la feature no añade ni un recurso. Comprobar que R8 no rompe el motor de plata.

---

## Dependencias y orden de ejecución

- **Fase 1 (Setup)**: T001 → T002. Bloquea a T010, T014, T015 y T020.
- **Fase 2 (Fundacional)**: dos ramas paralelas que confluyen en T016.
  - Motor: T003 → T004 → (T005 ∥ T006) → T007; T008 tras T005 y T009 tras T006, ambos en paralelo.
  - Componentes: (T010 ∥ T011 ∥ T012 ∥ T013) → T014 → T015 → T016.
- **Fase 3 (US1)**: T017 → T018 → (T019 ∥ T022); T020 → (T021 ∥ T023). T021 necesita además T019.
- **Fase 4 (US2)**: T024 → (T025 ∥ T026).
- **Fase 5 (US3)**: T027 → T028.
- **Fase 6 (US4)**: T029 → T030.
- **Fase 7 (Pulido)**: T031 y T032 tras las historias que se quieran entregar; T033–T037 sobre emulador; T038 y T039 al cierre.

US3 y US4 tocan los mismos dos ficheros que US1 (`PlataViewModel.kt` y `PlataScreen.kt`):
son secuenciales entre sí, no paralelizables.

## Estrategia de implementación

**MVP = Fase 1 + Fase 2 + US1**: con eso la calculadora ya resuelve el cálculo de taller y
es validable contra los cuatro casos de §21. US2 (aviso legal) entra antes de cualquier
publicación. US3 y US4 son incrementos pequeños sobre la misma pantalla.

La fase 2 es la más delicada de la feature, y no por el motor —que es más simple que el de
oro— sino por el refactor: T016 existe para no arrastrar una regresión de la 004 dentro de
las historias de plata. Parar ahí y validar antes de seguir.

## Notas

- [P] = ficheros distintos sin dependencias pendientes.
- El motor no se mockea en ningún test: es puro y determinista; solo se mockea `AnalyticsRepository`.
- Prohibido redondear pasos intermedios (§14) y prohibido meter `String`/primitivos en el grafo de Koin.
- El formateo de vista de plata trunca (`DOWN`); el de oro redondea a la media (`HALF_UP`). No unificarlos: la asimetría es deliberada y está justificada en `plan.md`.
- Ni un recurso gráfico nuevo: `modulo_plata.png`, `cobre.png` y los siete iconos ya existen.
- Commit por tarea o grupo lógico, Conventional Commits en español.
