package com.jrblanco.calculadoradejoyeros2021.ui.welcome

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Comprueba el contenido sin estado de la portada.
 *
 * Monta [WelcomeContent] directamente, sin Koin ni navegación: lo que se verifica aquí
 * es que la pantalla pinta lo que debe y que el botón está realmente cableado.
 */
class WelcomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun string(id: Int) = context.getString(id)

    @Test
    fun muestra_titulo_subtitulo_boton_y_credito() {
        composeRule.setContent {
            Calculadoradejoyeros2021Theme { WelcomeContent(onStart = {}) }
        }

        composeRule.onNodeWithText(string(R.string.welcome_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.welcome_subtitle)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.welcome_start)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.welcome_developer)).assertIsDisplayed()
    }

    @Test
    fun el_boton_comenzar_dispara_la_accion() {
        var clicks = 0
        composeRule.setContent {
            Calculadoradejoyeros2021Theme { WelcomeContent(onStart = { clicks++ }) }
        }

        composeRule.onNodeWithText(string(R.string.welcome_start))
            .assertHasClickAction()
            .performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun los_textos_con_tilde_se_muestran_completos() {
        composeRule.setContent {
            Calculadoradejoyeros2021Theme { WelcomeContent(onStart = {}) }
        }

        // Si la fuente empaquetada no cubriera el latín extendido, estos nodos no
        // coincidirían por texto.
        composeRule.onNodeWithText("Precisión y cálculo para tu taller").assertIsDisplayed()
        composeRule.onNodeWithText("Desarrollado por José Ramón Blanco").assertIsDisplayed()
    }
}
