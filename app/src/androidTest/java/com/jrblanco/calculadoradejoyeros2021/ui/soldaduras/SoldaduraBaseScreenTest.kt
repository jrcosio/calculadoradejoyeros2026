package com.jrblanco.calculadoradejoyeros2021.ui.soldaduras

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.ModoEntradaSoldadura
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Monta [SoldaduraBaseContent] directo, sin Koin ni NavHost, como el resto de pantallas. */
class SoldaduraBaseScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private fun texto(id: Int, vararg args: Any) = contexto.getString(id, *args)

    private val estadoConResultado = SoldaduraBaseUiState(
        cantidadTexto = "10",
        resultado = ResultadoSoldaduraBase(
            filas = listOf(
                FilaSoldadura(IngredienteSoldadura.COBRE, "0,540"),
                FilaSoldadura(IngredienteSoldadura.PLATA_FINA, "0,800"),
                FilaSoldadura(IngredienteSoldadura.ZINC, "0,920"),
                FilaSoldadura(IngredienteSoldadura.CADMIO, "1,000"),
            ),
            totalFormateado = "13,260",
        ),
    )

    private fun montar(
        uiState: SoldaduraBaseUiState,
        onModoCambiado: (ModoEntradaSoldadura) -> Unit = {},
        onCantidadCambiada: (String) -> Unit = {},
        onLimpiar: () -> Unit = {},
        onGuardarFavoritos: () -> Unit = {},
    ) {
        composeRule.setContent {
            Calculadoradejoyeros2021Theme {
                SoldaduraBaseContent(
                    uiState = uiState,
                    onModoCambiado = onModoCambiado,
                    onCantidadCambiada = onCantidadCambiada,
                    onLimpiar = onLimpiar,
                    onGuardarFavoritos = onGuardarFavoritos,
                    onInfo = {},
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun elAvisoDeSeguridadEsSiempreVisible() {
        montar(SoldaduraBaseUiState())

        composeRule.onNodeWithText(texto(R.string.soldadura_aviso_seguridad)).assertExists()
    }

    @Test
    fun laTarjetaDeProcesoMuestraSusTresPasos() {
        montar(SoldaduraBaseUiState())

        composeRule.onNodeWithText(texto(R.string.soldadura_base_proceso_titulo)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_base_proceso_1)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_base_proceso_2)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_base_proceso_3)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_base_masa_teorica)).assertExists()
    }

    @Test
    fun conResultado_seMuestranLasCuatroFilasYElTotal() {
        montar(estadoConResultado)

        composeRule.onNodeWithText(texto(R.string.metal_cobre)).assertExists()
        composeRule.onNodeWithText(texto(R.string.metal_plata_fina)).assertExists()
        composeRule.onNodeWithText(texto(R.string.metal_zinc)).assertExists()
        composeRule.onNodeWithText(texto(R.string.metal_cadmio)).assertExists()
        composeRule.onNodeWithText("13,260").assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_base_total)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_nota_redondeo)).assertExists()
    }

    @Test
    fun sinResultado_noHayFilasNiTotal() {
        montar(SoldaduraBaseUiState())

        composeRule.onAllNodesWithText(texto(R.string.metal_cobre)).assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.soldadura_base_total)).assertCountEquals(0)
    }

    @Test
    fun escribirEnElCampo_propagaLaCantidad() {
        var cantidad: String? = null
        montar(SoldaduraBaseUiState(), onCantidadCambiada = { cantidad = it })

        composeRule.onNode(hasSetTextAction()).performTextInput("10")

        assertEquals("10", cantidad)
    }

    @Test
    fun elConmutadorDeModoExisteYPropaga() {
        var modo: ModoEntradaSoldadura? = null
        montar(SoldaduraBaseUiState(), onModoCambiado = { modo = it })

        composeRule.onNodeWithText(texto(R.string.soldadura_base_modo_tengo_oro)).assertExists()
        composeRule.onNodeWithText(texto(R.string.soldadura_base_modo_peso)).performClick()

        assertEquals(ModoEntradaSoldadura.PESO_FINAL, modo)
    }

    @Test
    fun laLeyRealDeLaBaseNoSeMuestraNunca() {
        montar(estadoConResultado)

        // §5.2: ni las milésimas reales (754) ni la corrección a 750.
        composeRule.onAllNodesWithText("754", substring = true).assertCountEquals(0)
        composeRule.onAllNodesWithText("750", substring = true).assertCountEquals(0)
    }
}
