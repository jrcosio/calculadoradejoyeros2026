package com.jrblanco.calculadoradejoyeros2021.ui.welcome

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.EnIdiomaDeTest
import com.jrblanco.calculadoradejoyeros2021.ui.contextoDeTest
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

    private val context = contextoDeTest()
    private fun string(id: Int) = context.getString(id)
    private fun string(id: Int, vararg args: Any) = context.getString(id, *args)

    @Test
    fun muestra_titulo_subtitulo_boton_y_credito() {
        composeRule.setContent {
            EnIdiomaDeTest { WelcomeContent(onStart = {}) }
        }

        composeRule.onNodeWithText(string(R.string.welcome_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.welcome_subtitle)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.welcome_start)).assertIsDisplayed()
        // El crédito lleva el nombre por parámetro y no se traduce (feature 008): hay que pasarle
        // el mismo argumento que la pantalla, o se compararía contra la plantilla «… %1$s».
        composeRule.onNodeWithText(
            string(R.string.welcome_developer, string(R.string.info_perfil_nombre)),
        ).assertIsDisplayed()
    }

    @Test
    fun el_boton_comenzar_dispara_la_accion() {
        var clicks = 0
        composeRule.setContent {
            EnIdiomaDeTest { WelcomeContent(onStart = { clicks++ }) }
        }

        composeRule.onNodeWithText(string(R.string.welcome_start))
            .assertHasClickAction()
            .performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun los_textos_con_tilde_se_muestran_completos() {
        composeRule.setContent {
            EnIdiomaDeTest { WelcomeContent(onStart = {}) }
        }

        // Literales en español a propósito, y legítimos porque el árbol está anclado a
        // `IDIOMA_DE_TEST`: lo que se prueba es que la fuente empaquetada cubre el latín
        // extendido, así que leerlos de recursos dejaría de probar nada en un emulador en inglés.
        composeRule.onNodeWithText("Precisión y cálculo para tu taller").assertIsDisplayed()
        composeRule.onNodeWithText("Desarrollado por José Ramón Blanco").assertIsDisplayed()
    }
}
