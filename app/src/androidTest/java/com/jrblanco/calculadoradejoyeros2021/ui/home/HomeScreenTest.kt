package com.jrblanco.calculadoradejoyeros2021.ui.home

import androidx.compose.ui.test.assertHasClickAction
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
 * Comprueba el contenido sin estado del menú, sin Koin ni navegación.
 */
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun string(id: Int) = context.getString(id)

    private fun setContent(onModuleClick: (HomeModule) -> Unit = {}) {
        composeRule.setContent {
            Calculadoradejoyeros2021Theme {
                HomeContent(
                    uiState = HomeUiState(modules = HomeModule.entries),
                    onModuleClick = onModuleClick,
                    onTabSelect = {},
                    onInfo = {},
                )
            }
        }
    }

    @Test
    fun muestra_las_cuatro_tarjetas_de_modulo() {
        setContent()

        listOf(
            R.string.modulo_oro_titulo,
            R.string.modulo_plata_titulo,
            R.string.modulo_soldaduras_titulo,
            R.string.modulo_herramientas_titulo,
        ).forEach { titulo ->
            composeRule.onNodeWithText(string(titulo)).assertHasClickAction()
        }
    }

    @Test
    fun muestra_los_tres_destinos_de_la_barra_inferior() {
        setContent()

        composeRule.onNodeWithText(string(R.string.nav_home)).assertHasClickAction()
        composeRule.onNodeWithText(string(R.string.nav_favoritos)).assertHasClickAction()
        composeRule.onNodeWithText(string(R.string.nav_ajustes)).assertHasClickAction()
    }

    @Test
    fun pulsar_una_tarjeta_devuelve_su_modulo() {
        val pulsados = mutableListOf<HomeModule>()
        setContent(onModuleClick = { pulsados += it })

        composeRule.onNodeWithText(string(R.string.modulo_herramientas_titulo)).performClick()

        assertEquals(listOf(HomeModule.HERRAMIENTAS), pulsados)
    }

    @Test
    fun cada_tarjeta_devuelve_su_propio_modulo() {
        val pulsados = mutableListOf<HomeModule>()
        setContent(onModuleClick = { pulsados += it })

        composeRule.onNodeWithText(string(R.string.modulo_oro_titulo)).performClick()
        composeRule.onNodeWithText(string(R.string.modulo_plata_titulo)).performClick()

        assertEquals(listOf(HomeModule.ORO, HomeModule.PLATA), pulsados)
    }
}
