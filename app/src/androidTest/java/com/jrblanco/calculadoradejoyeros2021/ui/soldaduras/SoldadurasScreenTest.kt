package com.jrblanco.calculadoradejoyeros2021.ui.soldaduras

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraClasica
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraPlata
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Monta [SoldadurasContent] directo, sin Koin ni NavHost, igual que los tests de Home,
 * Info, oro y plata.
 */
class SoldadurasScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private fun texto(id: Int, vararg args: Any) = contexto.getString(id, *args)

    private val estadoOroLey = SoldadurasUiState(
        familia = FamiliaSoldadura.ORO_LEY,
        cantidadTexto = "2",
        resultado = ResultadoSoldaduras(
            filas = listOf(FilaSoldadura(IngredienteSoldadura.BASE, "6,667")),
            totalFormateado = "8,667",
        ),
    )

    private fun montar(
        uiState: SoldadurasUiState,
        onFamiliaSeleccionada: (FamiliaSoldadura) -> Unit = {},
        onModoCambiado: (ModoEntradaSoldadura) -> Unit = {},
        onCantidadCambiada: (String) -> Unit = {},
        onColorSeleccionado: (ColorOroSoldadura) -> Unit = {},
        onDurezaSeleccionada: (DurezaSoldaduraLey) -> Unit = {},
        onTipoClasicaSeleccionado: (TipoSoldaduraClasica) -> Unit = {},
        onTipoPlataSeleccionado: (TipoSoldaduraPlata) -> Unit = {},
        onSoldaduraBase: () -> Unit = {},
        onLimpiar: () -> Unit = {},
        onGuardarFavoritos: () -> Unit = {},
    ) {
        composeRule.setContent {
            Calculadoradejoyeros2021Theme {
                SoldadurasContent(
                    uiState = uiState,
                    onFamiliaSeleccionada = onFamiliaSeleccionada,
                    onModoCambiado = onModoCambiado,
                    onCantidadCambiada = onCantidadCambiada,
                    onColorSeleccionado = onColorSeleccionado,
                    onDurezaSeleccionada = onDurezaSeleccionada,
                    onTipoClasicaSeleccionado = onTipoClasicaSeleccionado,
                    onTipoPlataSeleccionado = onTipoPlataSeleccionado,
                    onSoldaduraBase = onSoldaduraBase,
                    onLimpiar = onLimpiar,
                    onGuardarFavoritos = onGuardarFavoritos,
                    onInfo = {},
                    onBack = {},
                )
            }
        }
    }

    // --- Primera visita (FR-002, SC-006) ---

    @Test
    fun primeraVisita_soloExisteElSelectorDeFamilias() {
        montar(SoldadurasUiState())

        composeRule.onNodeWithText(texto(R.string.soldadura_familia_oro_ley)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_familia_clasica)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_familia_plata)).assertExists()

        // Sin familia: ni campo, ni durezas, ni botón de la base.
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.soldadura_dureza_muy_floja))
            .assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.soldadura_ley_base_boton))
            .assertCountEquals(0)
    }

    @Test
    fun primeraVisita_ningunSegmentoDeFamiliaEstaSeleccionado() {
        montar(SoldadurasUiState())

        composeRule.onAllNodes(isSelected()).assertCountEquals(0)
    }

    @Test
    fun pulsarUnaFamilia_propagaLaFamiliaEsperada() {
        var seleccionada: FamiliaSoldadura? = null
        montar(SoldadurasUiState(), onFamiliaSeleccionada = { seleccionada = it })

        composeRule.onNodeWithText(texto(R.string.soldadura_familia_oro_ley)).performClick()

        assertEquals(FamiliaSoldadura.ORO_LEY, seleccionada)
    }

    // --- ORO LEY (FR-009, FR-010, FR-011) ---

    @Test
    fun conOroLey_lasCincoDurezasEstanVisibles() {
        montar(estadoOroLey)

        composeRule.onNodeWithText(texto(R.string.soldadura_dureza_muy_floja)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_dureza_floja)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_dureza_media)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_dureza_fuerte)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_dureza_muy_fuerte)).assertExists()
    }

    @Test
    fun conOroLey_hayTresColoresYNoExisteElRojo() {
        montar(estadoOroLey)

        composeRule.onNodeWithText(texto(R.string.oro_color_amarillo)).assertExists()
        composeRule.onNodeWithText(texto(R.string.oro_color_blanco)).assertExists()
        composeRule.onNodeWithText(texto(R.string.oro_color_rosa)).assertExists()
        composeRule.onAllNodesWithText(texto(R.string.oro_color_rojo)).assertCountEquals(0)
    }

    @Test
    fun pulsarUnaDureza_propagaLaDurezaEsperada() {
        var seleccionada: DurezaSoldaduraLey? = null
        montar(estadoOroLey, onDurezaSeleccionada = { seleccionada = it })

        composeRule.onNodeWithText(texto(R.string.soldadura_dureza_muy_fuerte)).performClick()

        assertEquals(DurezaSoldaduraLey.MUY_FUERTE, seleccionada)
    }

    @Test
    fun pulsarUnColor_propagaElColorEsperado() {
        var seleccionado: ColorOroSoldadura? = null
        montar(estadoOroLey, onColorSeleccionado = { seleccionado = it })

        composeRule.onNodeWithText(texto(R.string.oro_color_rosa)).performClick()

        assertEquals(ColorOroSoldadura.ROSA, seleccionado)
    }

    @Test
    fun pulsarSoldaduraBase_propagaSuCallback() {
        var pulsado = false
        montar(estadoOroLey, onSoldaduraBase = { pulsado = true })

        composeRule.onNodeWithText(texto(R.string.soldadura_ley_base_boton)).performClick()

        assertEquals(true, pulsado)
    }

    @Test
    fun conResultado_seMuestranLaBaseNecesariaElTotalYLaNotaDeRedondeo() {
        montar(estadoOroLey)

        composeRule.onNodeWithText(texto(R.string.soldadura_fila_base_necesaria)).assertExists()
        composeRule.onNodeWithText("6,667").assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_total)).assertExists()
        composeRule.onNodeWithText("8,667").assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_nota_redondeo)).assertExists()
    }

    // --- CLÁSICA (FR-015, FR-017) ---

    @Test
    fun conClasicaMuyFlojaDeLey_elAvisoDeSeguridadEsVisible() {
        montar(
            SoldadurasUiState(
                familia = FamiliaSoldadura.CLASICA,
                tipoClasica = TipoSoldaduraClasica.MUY_FLOJA_LEY,
            ),
        )

        composeRule.onNodeWithText(texto(R.string.soldadura_aviso_seguridad)).assertExists()
    }

    @Test
    fun conClasicaFloja_noHayAvisoDeSeguridad() {
        montar(SoldadurasUiState(familia = FamiliaSoldadura.CLASICA))

        composeRule.onAllNodesWithText(texto(R.string.soldadura_aviso_seguridad))
            .assertCountEquals(0)
    }

    @Test
    fun enClasica_noExisteNingunSelectorDeColor() {
        montar(SoldadurasUiState(familia = FamiliaSoldadura.CLASICA))

        composeRule.onAllNodesWithText(texto(R.string.oro_color_amarillo)).assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.oro_color_blanco)).assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.oro_color_rosa)).assertCountEquals(0)
    }

    @Test
    fun pulsarUnTipoClasico_propagaElTipoEsperado() {
        var seleccionado: TipoSoldaduraClasica? = null
        montar(
            SoldadurasUiState(familia = FamiliaSoldadura.CLASICA),
            onTipoClasicaSeleccionado = { seleccionado = it },
        )

        composeRule.onNodeWithText(texto(R.string.soldadura_clasica_muy_floja_ley)).performClick()

        assertEquals(TipoSoldaduraClasica.MUY_FLOJA_LEY, seleccionado)
    }

    // --- PLATA (FR-016) ---

    @Test
    fun conPlata_losCuatroTiposYLaNotaDeComposturasEstanVisibles() {
        // Con la muy floja seleccionada (el valor por defecto), la recomendación se ve.
        montar(SoldadurasUiState(familia = FamiliaSoldadura.PLATA))

        composeRule.onNodeWithText(texto(R.string.soldadura_plata_muy_floja)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_plata_floja)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_plata_normal)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_plata_fuerte)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_plata_nota_muy_floja)).assertExists()
    }

    @Test
    fun conOtroTipoDePlata_laNotaDeComposturasDesaparece() {
        montar(
            SoldadurasUiState(
                familia = FamiliaSoldadura.PLATA,
                tipoPlata = TipoSoldaduraPlata.FLOJA,
            ),
        )

        composeRule.onAllNodesWithText(texto(R.string.soldadura_plata_nota_muy_floja))
            .assertCountEquals(0)
    }

    @Test
    fun conPlataYResultado_seMuestraElLatonYElTotal() {
        montar(
            SoldadurasUiState(
                familia = FamiliaSoldadura.PLATA,
                cantidadTexto = "25",
                resultado = ResultadoSoldaduras(
                    filas = listOf(FilaSoldadura(IngredienteSoldadura.LATON, "18,750")),
                    totalFormateado = "43,750",
                ),
            ),
        )

        composeRule.onNodeWithText(texto(R.string.metal_laton)).assertExists()
        composeRule.onNodeWithText("18,750").assertExists()
        composeRule.onNodeWithText("43,750").assertExists()
    }

    @Test
    fun pulsarUnTipoDePlata_propagaElTipoEsperado() {
        var seleccionado: TipoSoldaduraPlata? = null
        montar(
            SoldadurasUiState(familia = FamiliaSoldadura.PLATA),
            onTipoPlataSeleccionado = { seleccionado = it },
        )

        composeRule.onNodeWithText(texto(R.string.soldadura_plata_normal)).performClick()

        assertEquals(TipoSoldaduraPlata.NORMAL, seleccionado)
    }

    // --- Conmutador de modo (FR-003) ---

    @Test
    fun conFamilia_elConmutadorDeModoExisteYPropaga() {
        var modo: ModoEntradaSoldadura? = null
        montar(estadoOroLey, onModoCambiado = { modo = it })

        composeRule.onNodeWithText(texto(R.string.soldadura_modo_tengo_oro18k)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_modo_peso_final)).performClick()

        assertEquals(ModoEntradaSoldadura.PESO_FINAL, modo)
    }

    @Test
    fun primeraVisita_noHayConmutadorDeModo() {
        montar(SoldadurasUiState())

        composeRule.onAllNodesWithText(texto(R.string.soldadura_modo_peso_final))
            .assertCountEquals(0)
    }

    @Test
    fun enModoPesoFinal_laFilaDelMetalDeEntradaSiSePinta() {
        montar(
            estadoOroLey.copy(
                modo = ModoEntradaSoldadura.PESO_FINAL,
                resultado = ResultadoSoldaduras(
                    filas = listOf(
                        FilaSoldadura(IngredienteSoldadura.BASE, "5,000"),
                        FilaSoldadura(IngredienteSoldadura.ORO_18K, "5,000"),
                    ),
                    totalFormateado = "10,000",
                ),
            ),
        )

        // La fila de la base con su nombre normal (no «necesaria») y la del oro.
        composeRule.onNodeWithText(texto(R.string.soldadura_fila_base)).assertExists()
        composeRule.onNodeWithText(
            texto(R.string.soldadura_fila_oro18k, texto(R.string.oro_color_amarillo)),
        ).assertExists()
    }

    // --- Limpiar y favoritos (FR-024) ---

    @Test
    fun primeraVisita_noHayBotonesDeAccion() {
        montar(SoldadurasUiState())

        composeRule.onAllNodesWithText(texto(R.string.accion_limpiar)).assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.accion_guardar_favoritos))
            .assertCountEquals(0)
    }

    @Test
    fun conFamilia_losBotonesExistenYPropagan() {
        var limpiado = false
        var guardado = false
        montar(
            estadoOroLey,
            onLimpiar = { limpiado = true },
            onGuardarFavoritos = { guardado = true },
        )

        composeRule.onNodeWithText(texto(R.string.accion_limpiar)).performClick()
        composeRule.onNodeWithText(texto(R.string.accion_guardar_favoritos)).performClick()

        assertEquals(true, limpiado)
        assertEquals(true, guardado)
    }

    @Test
    fun sinResultado_noHayFilaDeBaseNiTotal() {
        montar(estadoOroLey.copy(cantidadTexto = "", resultado = null))

        composeRule.onAllNodesWithText(texto(R.string.soldadura_fila_base_necesaria))
            .assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.soldadura_total)).assertCountEquals(0)
    }
}
