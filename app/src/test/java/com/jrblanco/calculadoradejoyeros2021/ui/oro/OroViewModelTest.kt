package com.jrblanco.calculadoradejoyeros2021.ui.oro

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalLiga
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularAleacionOroUseCase
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El caso de uso entra real, sin mock: el motor es puro y determinista, y así el test
 * verifica de paso que ViewModel y motor hablan el mismo idioma. Solo se mockea la
 * telemetría.
 */
class OroViewModelTest {

    private val analytics = mockk<AnalyticsRepository>(relaxed = true)

    private fun crearViewModel() = OroViewModel(CalcularAleacionOroUseCase(), analytics)

    @Test
    fun `el estado inicial es campo vacio con 18K amarillo y sin resultado`() = runTest {
        crearViewModel().uiState.test {
            val estado = awaitItem()
            assertEquals("", estado.cantidadTexto)
            assertEquals(LeyOro.LEY_18K, estado.ley)
            assertEquals(ColorOro.AMARILLO, estado.color)
            assertNull(estado.resultado)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `registra la vista de pantalla al construirse con el nombre del placeholder`() {
        crearViewModel()
        verify(exactly = 1) { analytics.logScreenView("oro") }
    }

    @Test
    fun `10 gramos de amarillo 18K reproducen el caso 1 del documento`() {
        val viewModel = crearViewModel()

        viewModel.onCantidadCambiada("10")

        val resultado = viewModel.uiState.value.resultado
        assertEquals(
            listOf(
                MetalCalculado(MetalLiga.PLATA_FINA, "2,191"),
                MetalCalculado(MetalLiga.COBRE, "1,129"),
            ),
            resultado?.metales,
        )
        assertEquals("13,320", resultado?.totalFormateado)
    }

    @Test
    fun `el blanco 18K muestra sus tres metales en el orden plata, cobre, paladio`() {
        val viewModel = crearViewModel()

        viewModel.onCantidadCambiada("10")
        viewModel.onColorSeleccionado(ColorOro.BLANCO)

        val resultado = viewModel.uiState.value.resultado
        assertEquals(
            listOf(
                MetalCalculado(MetalLiga.PLATA_FINA, "1,313"),
                MetalCalculado(MetalLiga.COBRE, "0,538"),
                MetalCalculado(MetalLiga.PALADIO, "1,469"),
            ),
            resultado?.metales,
        )
    }

    @Test
    fun `la coma y el punto decimal producen el mismo resultado`() {
        val conComa = crearViewModel().apply { onCantidadCambiada("12,35") }
        val conPunto = crearViewModel().apply { onCantidadCambiada("12.35") }

        assertEquals(
            conComa.uiState.value.resultado,
            conPunto.uiState.value.resultado,
        )
        assertEquals("16,450", conComa.uiState.value.resultado?.totalFormateado)
    }

    @Test
    fun `las entradas invalidas no producen resultado`() {
        val viewModel = crearViewModel()

        listOf("", "0", "-1", "abc", "1.2,3", "  ").forEach { texto ->
            viewModel.onCantidadCambiada(texto)
            assertNull("«$texto» no debería producir resultado", viewModel.uiState.value.resultado)
        }
    }

    @Test
    fun `cambiar la ley o el color recalcula sin tocar la cantidad`() {
        val viewModel = crearViewModel()
        viewModel.onCantidadCambiada("10")

        viewModel.onLeySeleccionada(LeyOro.LEY_9K)
        assertEquals("26,640", viewModel.uiState.value.resultado?.totalFormateado)

        viewModel.onColorSeleccionado(ColorOro.ROJO)
        assertEquals(
            listOf(MetalCalculado(MetalLiga.COBRE, "16,640")),
            viewModel.uiState.value.resultado?.metales,
        )
    }

    @Test
    fun `teclear la cantidad no duplica la telemetria de calculo`() {
        val viewModel = crearViewModel()

        viewModel.onCantidadCambiada("1")
        viewModel.onCantidadCambiada("10")
        viewModel.onCantidadCambiada("100")

        verify(exactly = 1) {
            analytics.logEvent("oro_calculado", mapOf("ley" to "18k", "color" to "amarillo"))
        }
    }

    @Test
    fun `cada combinacion nueva de ley y color registra su propio evento`() {
        val viewModel = crearViewModel()

        viewModel.onCantidadCambiada("10")
        viewModel.onLeySeleccionada(LeyOro.LEY_14K)
        viewModel.onColorSeleccionado(ColorOro.ROSA)

        verify(exactly = 1) {
            analytics.logEvent("oro_calculado", mapOf("ley" to "18k", "color" to "amarillo"))
        }
        verify(exactly = 1) {
            analytics.logEvent("oro_calculado", mapOf("ley" to "14k", "color" to "amarillo"))
        }
        verify(exactly = 1) {
            analytics.logEvent("oro_calculado", mapOf("ley" to "14k", "color" to "rosa"))
        }
    }

    @Test
    fun `volver a una entrada valida vuelve a registrar el calculo`() {
        val viewModel = crearViewModel()

        viewModel.onCantidadCambiada("10")
        viewModel.onCantidadCambiada("")
        viewModel.onCantidadCambiada("10")

        verify(exactly = 2) {
            analytics.logEvent("oro_calculado", mapOf("ley" to "18k", "color" to "amarillo"))
        }
    }

    @Test
    fun `solo 12K es ley tecnica y calcula con normalidad`() {
        assertTrue(LeyOro.LEY_12K.esSoloTecnica)
        LeyOro.entries.filter { it != LeyOro.LEY_12K }.forEach { assertFalse(it.esSoloTecnica) }

        val viewModel = crearViewModel()
        viewModel.onCantidadCambiada("100")
        viewModel.onLeySeleccionada(LeyOro.LEY_12K)

        assertEquals("199,800", viewModel.uiState.value.resultado?.totalFormateado)
    }

    @Test
    fun `limpiar devuelve el estado inicial y rearma la telemetria`() {
        val viewModel = crearViewModel()
        viewModel.onCantidadCambiada("10")
        viewModel.onLeySeleccionada(LeyOro.LEY_14K)

        viewModel.onLimpiar()

        assertEquals(OroUiState(), viewModel.uiState.value)

        viewModel.onCantidadCambiada("10")
        verify(exactly = 2) {
            // 1º al teclear 10 con 18K amarillo, 2º tras limpiar: misma combinación.
            analytics.logEvent("oro_calculado", mapOf("ley" to "18k", "color" to "amarillo"))
        }
    }

    @Test
    fun `guardar en favoritos solo registra telemetria y no toca el estado`() {
        val viewModel = crearViewModel()
        viewModel.onCantidadCambiada("10")
        val estadoAntes = viewModel.uiState.value

        viewModel.onGuardarFavoritos()
        viewModel.onGuardarFavoritos()

        assertEquals(estadoAntes, viewModel.uiState.value)
        verify(exactly = 2) { analytics.logEvent("oro_favoritos_proximamente") }
    }
}
