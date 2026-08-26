package com.jrblanco.calculadoradejoyeros2021.ui.info

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.EnIdiomaDeTest
import com.jrblanco.calculadoradejoyeros2021.ui.contextoDeTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Comprueba el contenido sin estado de la pantalla de información, sin Koin ni navegación.
 */
class InfoScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = contextoDeTest()
    private fun string(id: Int) = context.getString(id)
    private fun string(id: Int, vararg args: Any) = context.getString(id, *args)

    private fun setContent(
        onEnlaceClick: (InfoEnlace) -> Unit = {},
        versionName: String = VERSION_DE_PRUEBA,
    ) {
        composeRule.setContent {
            EnIdiomaDeTest {
                InfoContent(
                    uiState = InfoUiState(enlaces = InfoEnlace.entries),
                    onEnlaceClick = onEnlaceClick,
                    onBack = {},
                    versionName = versionName,
                )
            }
        }
    }

    /** La dirección tal y como la pinta la tarjeta, derivada de la real. */
    private fun InfoEnlace.direccionVisible() = url.removePrefix("https://www.")

    @Test
    fun muestra_el_titulo_y_el_perfil_del_autor() {
        setContent()

        composeRule.onNodeWithText(string(R.string.info_titulo)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.info_perfil_nombre)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.info_perfil_descripcion)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.info_perfil_etiquetas)).assertIsDisplayed()
    }

    @Test
    fun la_tarjeta_de_blanco_joyeros_es_informativa_y_no_se_pulsa() {
        setContent()

        composeRule.onNodeWithText(string(R.string.info_blanco_joyeros_titulo))
            .assertIsDisplayed()
            .assertHasNoClickAction()
        composeRule.onNodeWithText(string(R.string.info_blanco_joyeros_descripcion))
            .assertHasNoClickAction()
    }

    @Test
    fun muestra_los_dos_accesos_con_su_direccion() {
        setContent()

        composeRule.onNodeWithText(string(R.string.info_linkedin_titulo)).assertHasClickAction()
        composeRule.onNodeWithText(InfoEnlace.LINKEDIN.direccionVisible()).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.info_instagram_titulo)).assertHasClickAction()
        composeRule.onNodeWithText(InfoEnlace.INSTAGRAM.direccionVisible()).assertIsDisplayed()
    }

    @Test
    fun la_barra_superior_no_ofrece_acceso_a_la_propia_informacion() {
        setContent()

        composeRule.onNodeWithContentDescription(string(R.string.topbar_info)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(string(R.string.topbar_atras)).assertHasClickAction()
    }

    @Test
    fun muestra_al_pie_la_version_de_la_app() {
        setContent()

        composeRule.onNodeWithText(string(R.string.info_version, VERSION_DE_PRUEBA))
            .assertIsDisplayed()
    }

    @Test
    fun cada_acceso_devuelve_su_propio_enlace() {
        val pulsados = mutableListOf<InfoEnlace>()
        setContent(onEnlaceClick = { pulsados += it })

        composeRule.onNodeWithText(string(R.string.info_linkedin_titulo)).performClick()
        composeRule.onNodeWithText(string(R.string.info_instagram_titulo)).performClick()

        assertEquals(listOf(InfoEnlace.LINKEDIN, InfoEnlace.INSTAGRAM), pulsados)
    }

    private companion object {
        // Fija, y distinta de la real, para que el test no se caiga en cada subida de versión.
        const val VERSION_DE_PRUEBA = "9.9.9"
    }
}
