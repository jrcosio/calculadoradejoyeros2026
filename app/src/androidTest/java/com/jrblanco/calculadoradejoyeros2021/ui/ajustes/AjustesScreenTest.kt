package com.jrblanco.calculadoradejoyeros2021.ui.ajustes

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Monta [AjustesContent] sin Koin ni NavHost, como los demás tests de pantalla. */
class AjustesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private fun texto(id: Int, vararg args: Any) = contexto.getString(id, *args)

    private fun montar(
        uiState: AjustesUiState,
        onIdiomaSeleccionado: (IdiomaApp) -> Unit = {},
        onAutomaticoSeleccionado: () -> Unit = {},
    ) {
        composeRule.setContent {
            Calculadoradejoyeros2021Theme {
                AjustesContent(
                    uiState = uiState,
                    onIdiomaSeleccionado = onIdiomaSeleccionado,
                    onAutomaticoSeleccionado = onAutomaticoSeleccionado,
                    onTabSelect = {},
                    onInfo = {},
                )
            }
        }
    }

    @Test
    fun seVenLasSeisOpciones_conLosNombresEnSuPropioIdioma() {
        montar(AjustesUiState())

        composeRule.onNodeWithText(texto(R.string.ajustes_idioma_automatico)).assertExists()
        composeRule.onNodeWithText("Español").assertExists()
        composeRule.onNodeWithText("English").assertExists()
        composeRule.onNodeWithText("Français").assertExists()
        composeRule.onNodeWithText("Deutsch").assertExists()
        composeRule.onNodeWithText("Italiano").assertExists()
    }

    @Test
    fun primeraVisita_automaticoMarcadoConElIdiomaDetectado() {
        montar(AjustesUiState(elegido = null, sistema = IdiomaApp.FRANCES))

        // Una sola fila seleccionada entre las seis, y es la de «Automático».
        composeRule.onAllNodes(isSelected()).assertCountEquals(1)
        composeRule.onNodeWithText(
            texto(R.string.ajustes_idioma_automatico_detalle, texto(R.string.idioma_fr)),
        ).assertExists()
        composeRule.onNodeWithText(texto(R.string.ajustes_idioma_automatico)).assertIsSelected()
    }

    @Test
    fun conIdiomaElegido_soloEseAparaceMarcado() {
        montar(AjustesUiState(elegido = IdiomaApp.ITALIANO, sistema = IdiomaApp.ESPANOL))

        composeRule.onAllNodes(isSelected()).assertCountEquals(1)
        composeRule.onNodeWithText("Italiano").assertIsSelected()
    }

    @Test
    fun tocarUnaBandera_avisaConEseIdioma() {
        val elegidos = mutableListOf<IdiomaApp>()
        montar(AjustesUiState(), onIdiomaSeleccionado = { elegidos += it })

        composeRule.onNodeWithText("Deutsch").performClick()

        assertEquals(listOf(IdiomaApp.ALEMAN), elegidos)
    }

    @Test
    fun tocarAutomatico_avisaDeLaVuelta() {
        var vueltas = 0
        montar(
            AjustesUiState(elegido = IdiomaApp.ALEMAN, sistema = IdiomaApp.ESPANOL),
            onAutomaticoSeleccionado = { vueltas++ },
        )

        composeRule.onNodeWithText(texto(R.string.ajustes_idioma_automatico)).performClick()

        assertTrue(vueltas == 1)
    }

    @Test
    fun laCabeceraYLaDescripcionEstanPresentes() {
        montar(AjustesUiState())

        composeRule.onNodeWithText(texto(R.string.ajustes_seccion_idioma)).assertExists()
        composeRule.onNodeWithText(texto(R.string.ajustes_idioma_descripcion)).assertExists()
        // «Ajustes» sale dos veces y así debe ser: el título de la barra superior y la pestaña
        // activa de la barra inferior.
        composeRule.onAllNodesWithText(texto(R.string.nav_ajustes)).assertCountEquals(2)
    }
}
