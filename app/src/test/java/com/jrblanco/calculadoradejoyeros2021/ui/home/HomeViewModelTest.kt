package com.jrblanco.calculadoradejoyeros2021.ui.home

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val analytics = mockk<AnalyticsRepository>(relaxed = true)

    private val dispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = testDispatcher
        override val io: CoroutineDispatcher = testDispatcher
        override val default: CoroutineDispatcher = testDispatcher
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `arranca en estado no listo y pasa a listo con el titulo`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(analytics, dispatchers)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue("El estado inicial no debe estar listo", !initial.isReady)

            testDispatcher.scheduler.advanceUntilIdle()

            val ready = awaitItem()
            assertTrue(ready.isReady)
            assertEquals("Calculadora de Joyeros", ready.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `registra la vista de pantalla al abrirse`() = runTest(testDispatcher) {
        HomeViewModel(analytics, dispatchers)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 1) { analytics.logScreenView("home") }
    }
}
