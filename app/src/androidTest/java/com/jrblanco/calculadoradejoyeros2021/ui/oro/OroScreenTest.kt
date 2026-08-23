package com.jrblanco.calculadoradejoyeros2021.ui.oro

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalLiga
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Monta [OroContent] directo, sin Koin ni NavHost, igual que los tests de Home e Info.
 */
class OroScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private fun texto(id: Int, vararg args: Any) = contexto.getString(id, *args)

    private val estadoBlanco18K = OroUiState(
        cantidadTexto = "50",
        ley = LeyOro.LEY_18K,
        color = ColorOro.BLANCO,
        resultado = ResultadoOro(
            metales = listOf(
                MetalCalculado(MetalLiga.PLATA_FINA, "6,564"),
                MetalCalculado(MetalLiga.COBRE, "2,690"),
                MetalCalculado(MetalLiga.PALADIO, "7,346"),
            ),
            totalFormateado = "66,600",
        ),
    )

    private fun montar(
        uiState: OroUiState,
        onCantidadCambiada: (String) -> Unit = {},
        onLeySeleccionada: (LeyOro) -> Unit = {},
        onColorSeleccionado: (ColorOro) -> Unit = {},
        onLimpiar: () -> Unit = {},
        onGuardarFavoritos: () -> Unit = {},
    ) {
        composeRule.setContent {
            Calculadoradejoyeros2021Theme {
                OroContent(
                    uiState = uiState,
                    onCantidadCambiada = onCantidadCambiada,
                    onLeySeleccionada = onLeySeleccionada,
                    onColorSeleccionado = onColorSeleccionado,
                    onLimpiar = onLimpiar,
                    onGuardarFavoritos = onGuardarFavoritos,
                    onInfo = {},
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun conResultadoDeBlanco18K_seMuestranLasTresFilasYElTotal() {
        montar(estadoBlanco18K)

        composeRule.onNodeWithText(texto(R.string.oro_metal_plata)).assertExists()
        composeRule.onNodeWithText(texto(R.string.oro_metal_cobre)).assertExists()
        composeRule.onNodeWithText(texto(R.string.oro_metal_paladio)).assertExists()
        composeRule.onNodeWithText("6,564").assertExists()
        composeRule.onNodeWithText("2,690").assertExists()
        composeRule.onNodeWithText("7,346").assertExists()
        composeRule.onNodeWithText(
            texto(R.string.oro_total, texto(R.string.oro_color_blanco).uppercase()),
        ).assertExists()
        composeRule.onNodeWithText("66,600").assertExists()
    }

    @Test
    fun sinResultado_noHayFilasDeMetal() {
        montar(OroUiState())

        composeRule.onAllNodesWithText(texto(R.string.oro_metal_plata)).assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.oro_metal_cobre)).assertCountEquals(0)
    }

    @Test
    fun pulsarUnaLey_propagaLaLeyEsperada() {
        var seleccionada: LeyOro? = null
        montar(OroUiState(), onLeySeleccionada = { seleccionada = it })

        composeRule.onNodeWithText(texto(R.string.oro_ley_14k)).performClick()

        assertEquals(LeyOro.LEY_14K, seleccionada)
    }

    @Test
    fun pulsarUnColor_propagaElColorEsperado() {
        var seleccionado: ColorOro? = null
        montar(OroUiState(), onColorSeleccionado = { seleccionado = it })

        composeRule.onNodeWithText(texto(R.string.oro_color_rojo)).performClick()

        assertEquals(ColorOro.ROJO, seleccionado)
    }

    @Test
    fun escribirEnElCampo_propagaElTexto() {
        var recibido = ""
        montar(OroUiState(), onCantidadCambiada = { recibido = it })

        composeRule.onNodeWithText(texto(R.string.oro_entrada_unidad)).assertExists()
        composeRule.onNode(
            androidx.compose.ui.test.hasSetTextAction(),
        ).performTextInput("50")

        assertEquals("50", recibido)
    }

    @Test
    fun con12K_elAvisoDeLeyTecnicaExiste() {
        montar(OroUiState(ley = LeyOro.LEY_12K))

        composeRule.onNodeWithText(texto(R.string.oro_aviso_12k)).assertExists()
    }

    @Test
    fun con18K_noHayAvisoDeLeyTecnica() {
        montar(OroUiState(ley = LeyOro.LEY_18K))

        composeRule.onAllNodesWithText(texto(R.string.oro_aviso_12k)).assertCountEquals(0)
    }

    @Test
    fun pulsarLimpiar_propagaElCallback() {
        var limpiado = false
        montar(estadoBlanco18K, onLimpiar = { limpiado = true })

        composeRule.onNodeWithText(texto(R.string.oro_limpiar)).performClick()

        assertEquals(true, limpiado)
    }

    @Test
    fun pulsarGuardarEnFavoritos_propagaElCallback() {
        var guardado = false
        montar(estadoBlanco18K, onGuardarFavoritos = { guardado = true })

        composeRule.onNodeWithText(texto(R.string.oro_guardar_favoritos)).performClick()

        assertEquals(true, guardado)
    }
}
