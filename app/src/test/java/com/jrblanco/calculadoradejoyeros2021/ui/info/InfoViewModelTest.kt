package com.jrblanco.calculadoradejoyeros2021.ui.info

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InfoViewModelTest {

    private val analytics = mockk<AnalyticsRepository>(relaxed = true)

    @Test
    fun `expone los dos enlaces en el orden de la pantalla`() = runTest {
        val viewModel = InfoViewModel(analytics)

        viewModel.uiState.test {
            assertEquals(
                listOf(InfoEnlace.LINKEDIN, InfoEnlace.INSTAGRAM),
                awaitItem().enlaces,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `registra la vista de pantalla al construirse`() {
        InfoViewModel(analytics)

        verify(exactly = 1) { analytics.logScreenView("acerca_de") }
    }

    @Test
    fun `registra el enlace abierto con su identificador`() {
        val viewModel = InfoViewModel(analytics)

        val abre = viewModel.onEnlacePulsado(InfoEnlace.LINKEDIN)

        assertTrue(abre)
        verify(exactly = 1) {
            analytics.logEvent("acerca_de_enlace_abierto", mapOf("enlace" to "linkedin"))
        }
    }

    @Test
    fun `una segunda pulsacion con la apertura en curso no abre ni registra otra vez`() {
        val viewModel = InfoViewModel(analytics)

        viewModel.onEnlacePulsado(InfoEnlace.INSTAGRAM)
        val segunda = viewModel.onEnlacePulsado(InfoEnlace.INSTAGRAM)

        assertFalse(segunda)
        verify(exactly = 1) {
            analytics.logEvent("acerca_de_enlace_abierto", mapOf("enlace" to "instagram"))
        }
    }

    @Test
    fun `al volver a la pantalla los accesos se rehabilitan`() {
        val viewModel = InfoViewModel(analytics)

        viewModel.onEnlacePulsado(InfoEnlace.LINKEDIN)
        viewModel.onPantallaVisible()
        val segunda = viewModel.onEnlacePulsado(InfoEnlace.LINKEDIN)

        assertTrue(segunda)
        verify(exactly = 2) {
            analytics.logEvent("acerca_de_enlace_abierto", mapOf("enlace" to "linkedin"))
        }
    }

    @Test
    fun `un fallo al abrir se registra como error y devuelve el acceso`() {
        val viewModel = InfoViewModel(analytics)
        val error = IllegalArgumentException("sin navegador")

        viewModel.onEnlacePulsado(InfoEnlace.LINKEDIN)
        viewModel.onEnlaceFallido(error)

        verify(exactly = 1) { analytics.recordError(error) }
        assertTrue(viewModel.onEnlacePulsado(InfoEnlace.LINKEDIN))
    }

    @Test
    fun `cada enlace tiene un identificador de telemetria distinto`() {
        assertEquals(
            InfoEnlace.entries.size,
            InfoEnlace.entries.map { it.analyticsId }.distinct().size,
        )
    }
}
