package com.jrblanco.calculadoradejoyeros2021.ui.welcome

import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class WelcomeViewModelTest {

    private val analytics = mockk<AnalyticsRepository>(relaxed = true)

    @Test
    fun `registra la vista de pantalla al construirse`() {
        WelcomeViewModel(analytics)

        verify(exactly = 1) { analytics.logScreenView("welcome") }
    }

    @Test
    fun `registra el evento al pulsar comenzar`() {
        val viewModel = WelcomeViewModel(analytics)

        viewModel.onStartClicked()

        verify(exactly = 1) { analytics.logEvent("welcome_comenzar") }
    }

    @Test
    fun `no registra el evento de comenzar si no se pulsa`() {
        WelcomeViewModel(analytics)

        verify(exactly = 0) { analytics.logEvent(any(), any()) }
    }
}
