package com.jrblanco.calculadoradejoyeros2021.ui.herramientas

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HerramientasViewModelTest {

    private val analytics = mockk<AnalyticsRepository>(relaxed = true)

    private fun crearViewModel() = HerramientasViewModel(analytics)

    @Test
    fun `la primera visita no tiene sub-herramienta elegida`() = runTest {
        crearViewModel().uiState.test {
            assertNull(awaitItem().subherramienta)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `registra la vista de pantalla con el nombre del placeholder`() {
        crearViewModel()
        verify(exactly = 1) { analytics.logScreenView("herramientas") }
    }

    @Test
    fun `elegir una sub-herramienta la marca y la registra`() {
        val viewModel = crearViewModel()

        viewModel.onSubherramientaSeleccionada(Subherramienta.CHAPAS)

        assertEquals(Subherramienta.CHAPAS, viewModel.uiState.value.subherramienta)
        verify(exactly = 1) {
            analytics.logEvent("herramientas_subherramienta", mapOf("subherramienta" to "chapas"))
        }
    }

    @Test
    fun `repetir la misma no re-registra y cambiar si`() {
        val viewModel = crearViewModel()

        viewModel.onSubherramientaSeleccionada(Subherramienta.CHAPAS)
        viewModel.onSubherramientaSeleccionada(Subherramienta.CHAPAS)
        viewModel.onSubherramientaSeleccionada(Subherramienta.PRECIOS)

        assertEquals(Subherramienta.PRECIOS, viewModel.uiState.value.subherramienta)
        verify(exactly = 1) {
            analytics.logEvent("herramientas_subherramienta", mapOf("subherramienta" to "chapas"))
        }
        verify(exactly = 1) {
            analytics.logEvent("herramientas_subherramienta", mapOf("subherramienta" to "precios"))
        }
    }
}
