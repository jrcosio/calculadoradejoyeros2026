package com.jrblanco.calculadoradejoyeros2021.ui.idioma

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import org.junit.Rule
import org.junit.Test

/**
 * La prueba del mecanismo de la feature: un texto envuelto en [ProveedorIdioma] se lee en el
 * idioma indicado, sea cual sea el del dispositivo. Si este test pasa, cambiar de bandera en
 * Ajustes repinta la app entera.
 */
class ProveedorIdiomaTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun envueltoEnAleman_elTextoSaleEnAleman() {
        composeRule.setContent {
            ProveedorIdioma(IdiomaApp.ALEMAN) {
                Text(stringResource(R.string.nav_ajustes))
            }
        }

        composeRule.onNodeWithText("Einstellungen").assertIsDisplayed()
    }

    @Test
    fun cadaIdiomaPintaSuPropioTexto() {
        val esperados = mapOf(
            IdiomaApp.ESPANOL to "Ajustes",
            IdiomaApp.INGLES to "Settings",
            IdiomaApp.FRANCES to "Réglages",
            IdiomaApp.ALEMAN to "Einstellungen",
            IdiomaApp.ITALIANO to "Impostazioni",
        )

        composeRule.setContent {
            esperados.keys.forEach { idioma ->
                ProveedorIdioma(idioma) {
                    Text(stringResource(R.string.nav_ajustes))
                }
            }
        }

        esperados.values.forEach { texto ->
            composeRule.onNodeWithText(texto).assertExists()
        }
    }

    @Test
    fun elCambioDeIdiomaRepintaElMismoArbol() {
        val idioma = mutableStateOf(IdiomaApp.ESPANOL)
        composeRule.setContent {
            ProveedorIdioma(idioma.value) {
                Text(stringResource(R.string.nav_ajustes))
            }
        }
        composeRule.onNodeWithText("Ajustes").assertExists()

        // Sin navegar y sin recrear la Activity: solo cambia el idioma del proveedor.
        composeRule.runOnIdle { idioma.value = IdiomaApp.ITALIANO }

        composeRule.onNodeWithText("Impostazioni").assertExists()
        composeRule.onNodeWithText("Ajustes").assertDoesNotExist()
    }
}
