package com.jrblanco.calculadoradejoyeros2021.ui.plata

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Monta [PlataContent] directo, sin Koin ni NavHost, igual que los tests de Home, Info y oro.
 */
class PlataScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private fun texto(id: Int, vararg args: Any) = contexto.getString(id, *args)

    private val estado925 = PlataUiState(
        cantidadTexto = "25",
        ley = LeyPlata.LEY_925,
        resultado = ResultadoPlata(
            cobreFormateado = "2,000",
            totalFormateado = "27,000",
        ),
    )

    private fun montar(
        uiState: PlataUiState,
        onCantidadCambiada: (String) -> Unit = {},
        onLeySeleccionada: (LeyPlata) -> Unit = {},
        onLimpiar: () -> Unit = {},
        onGuardarFavoritos: () -> Unit = {},
    ) {
        composeRule.setContent {
            Calculadoradejoyeros2021Theme {
                PlataContent(
                    uiState = uiState,
                    onCantidadCambiada = onCantidadCambiada,
                    onLeySeleccionada = onLeySeleccionada,
                    onLimpiar = onLimpiar,
                    onGuardarFavoritos = onGuardarFavoritos,
                    onInfo = {},
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun conResultado_seMuestraElCobreYElTotalConSuLey() {
        montar(estado925)

        composeRule.onNodeWithText(texto(R.string.metal_cobre)).assertExists()
        composeRule.onNodeWithText("2,000").assertExists()
        composeRule.onNodeWithText(texto(R.string.plata_total, "925")).assertExists()
        composeRule.onNodeWithText("27,000").assertExists()
    }

    @Test
    fun sinResultado_noHayFilaDeCobreNiTotal() {
        montar(PlataUiState())

        composeRule.onAllNodesWithText(texto(R.string.metal_cobre)).assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.plata_total, "925")).assertCountEquals(0)
    }

    @Test
    fun lasCuatroLeyesEstanEnElSelector() {
        montar(PlataUiState())

        composeRule.onNodeWithText(texto(R.string.plata_ley_950)).assertExists()
        composeRule.onNodeWithText(texto(R.string.plata_ley_925)).assertExists()
        composeRule.onNodeWithText(texto(R.string.plata_ley_900)).assertExists()
        composeRule.onNodeWithText(texto(R.string.plata_ley_800)).assertExists()
    }

    @Test
    fun pulsarUnaLey_propagaLaLeyEsperada() {
        var seleccionada: LeyPlata? = null
        montar(PlataUiState(), onLeySeleccionada = { seleccionada = it })

        composeRule.onNodeWithText(texto(R.string.plata_ley_800)).performClick()

        assertEquals(LeyPlata.LEY_800, seleccionada)
    }

    @Test
    fun escribirEnElCampo_propagaElTexto() {
        var recibido = ""
        montar(PlataUiState(), onCantidadCambiada = { recibido = it })

        composeRule.onNodeWithText(texto(R.string.unidad_gramos)).assertExists()
        composeRule.onNode(hasSetTextAction()).performTextInput("25")

        assertEquals("25", recibido)
    }

    @Test
    fun con950_elAvisoDeLeyTecnicaEsElDe950() {
        montar(PlataUiState(ley = LeyPlata.LEY_950))

        composeRule.onNodeWithText(texto(R.string.plata_aviso_950)).assertExists()
        composeRule.onAllNodesWithText(texto(R.string.plata_aviso_900)).assertCountEquals(0)
    }

    @Test
    fun con900_elAvisoDeLeyTecnicaEsElDe900() {
        montar(PlataUiState(ley = LeyPlata.LEY_900))

        composeRule.onNodeWithText(texto(R.string.plata_aviso_900)).assertExists()
        composeRule.onAllNodesWithText(texto(R.string.plata_aviso_950)).assertCountEquals(0)
    }

    // Una ley por test: `setContent` solo se puede llamar una vez por caso.

    @Test
    fun con925_noHayAvisoDeLeyTecnica() {
        montar(PlataUiState(ley = LeyPlata.LEY_925))

        composeRule.onAllNodesWithText(texto(R.string.plata_aviso_950)).assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.plata_aviso_900)).assertCountEquals(0)
    }

    @Test
    fun con800_noHayAvisoDeLeyTecnica() {
        montar(PlataUiState(ley = LeyPlata.LEY_800))

        composeRule.onAllNodesWithText(texto(R.string.plata_aviso_950)).assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.plata_aviso_900)).assertCountEquals(0)
    }

    @Test
    fun pulsarLimpiar_propagaElCallback() {
        var limpiado = false
        montar(estado925, onLimpiar = { limpiado = true })

        composeRule.onNodeWithText(texto(R.string.accion_limpiar)).performClick()

        assertEquals(true, limpiado)
    }

    @Test
    fun pulsarGuardarEnFavoritos_propagaElCallback() {
        var guardado = false
        montar(estado925, onGuardarFavoritos = { guardado = true })

        composeRule.onNodeWithText(texto(R.string.accion_guardar_favoritos)).performClick()

        assertEquals(true, guardado)
    }
}
