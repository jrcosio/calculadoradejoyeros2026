package com.jrblanco.calculadoradejoyeros2021.ui.home

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {

    private val analytics = mockk<AnalyticsRepository>(relaxed = true)

    @Test
    fun `expone los cuatro modulos en el orden del menu`() = runTest {
        val viewModel = HomeViewModel(analytics)

        viewModel.uiState.test {
            assertEquals(
                listOf(
                    HomeModule.ORO,
                    HomeModule.PLATA,
                    HomeModule.SOLDADURAS,
                    HomeModule.HERRAMIENTAS,
                ),
                awaitItem().modules,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `registra la vista de pantalla al construirse`() {
        HomeViewModel(analytics)

        verify(exactly = 1) { analytics.logScreenView("home") }
    }

    @Test
    fun `registra el modulo elegido con su identificador`() {
        val viewModel = HomeViewModel(analytics)

        viewModel.onModuleClicked(HomeModule.SOLDADURAS)

        verify(exactly = 1) {
            analytics.logEvent("home_modulo_abierto", mapOf("modulo" to "soldaduras"))
        }
    }

    @Test
    fun `cada modulo se registra con un identificador distinto`() {
        val viewModel = HomeViewModel(analytics)

        HomeModule.entries.forEach { viewModel.onModuleClicked(it) }

        HomeModule.entries.forEach { module ->
            verify(exactly = 1) {
                analytics.logEvent("home_modulo_abierto", mapOf("modulo" to module.analyticsId))
            }
        }
        assertEquals(4, HomeModule.entries.map { it.analyticsId }.distinct().size)
    }
}
