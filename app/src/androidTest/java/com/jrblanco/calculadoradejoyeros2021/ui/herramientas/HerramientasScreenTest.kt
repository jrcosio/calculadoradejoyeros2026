package com.jrblanco.calculadoradejoyeros2021.ui.herramientas

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.EnIdiomaDeTest
import com.jrblanco.calculadoradejoyeros2021.ui.contextoDeTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Monta [HerramientasContent] con marcadores en los slots, sin Koin ni NavHost. */
class HerramientasScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexto = contextoDeTest()
    private fun texto(id: Int) = contexto.getString(id)
    private fun texto(id: Int, vararg args: Any) = contexto.getString(id, *args)

    private fun montar(
        uiState: HerramientasUiState,
        onSubherramientaSeleccionada: (Subherramienta) -> Unit = {},
    ) {
        composeRule.setContent {
            EnIdiomaDeTest {
                HerramientasContent(
                    uiState = uiState,
                    onSubherramientaSeleccionada = onSubherramientaSeleccionada,
                    onInfo = {},
                    onBack = {},
                    precios = { Text("marcador-precios") },
                    chapas = { Text("marcador-chapas") },
                )
            }
        }
    }

    @Test
    fun primeraVisita_soloSelectorEInvitacion() {
        montar(HerramientasUiState())

        composeRule.onNodeWithText(texto(R.string.herramientas_subherramienta_precios)).assertExists()
        composeRule.onNodeWithText(texto(R.string.herramientas_subherramienta_chapas)).assertExists()
        composeRule.onNodeWithText(texto(R.string.herramientas_primera_visita_titulo)).assertExists()
        composeRule.onAllNodesWithText("marcador-precios").assertCountEquals(0)
        composeRule.onAllNodesWithText("marcador-chapas").assertCountEquals(0)
        composeRule.onAllNodes(isSelected()).assertCountEquals(0)
    }

    @Test
    fun primeraVisita_noHayBotonesDeAccion() {
        montar(HerramientasUiState())

        composeRule.onAllNodesWithText(texto(R.string.accion_limpiar)).assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.accion_guardar_favoritos)).assertCountEquals(0)
    }

    @Test
    fun pulsarPesoDeChapas_propagaLaSubherramienta() {
        var elegida: Subherramienta? = null
        montar(HerramientasUiState(), onSubherramientaSeleccionada = { elegida = it })

        composeRule.onNodeWithText(texto(R.string.herramientas_subherramienta_chapas)).performClick()

        assertEquals(Subherramienta.CHAPAS, elegida)
    }

    @Test
    fun conPrecios_seMuestraSuSeccionYNoLaOtra() {
        montar(HerramientasUiState(subherramienta = Subherramienta.PRECIOS))

        composeRule.onNodeWithText("marcador-precios").assertExists()
        composeRule.onAllNodesWithText("marcador-chapas").assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.herramientas_primera_visita_titulo)).assertCountEquals(0)
        composeRule.onAllNodes(isSelected()).assertCountEquals(1)
    }

    @Test
    fun conChapas_seMuestraSuSeccion() {
        montar(HerramientasUiState(subherramienta = Subherramienta.CHAPAS))

        composeRule.onNodeWithText("marcador-chapas").assertExists()
        composeRule.onAllNodesWithText("marcador-precios").assertCountEquals(0)
    }
}
