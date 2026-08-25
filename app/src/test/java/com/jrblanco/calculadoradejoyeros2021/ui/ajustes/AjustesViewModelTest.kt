package com.jrblanco.calculadoradejoyeros2021.ui.ajustes

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.core.util.IdiomaSistemaFalso
import com.jrblanco.calculadoradejoyeros2021.core.util.TestDispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.data.repository.FakePreferenciasRepository
import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.GuardarIdiomaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ObservarIdiomaUseCase
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AjustesViewModelTest {

    private val analytics = mockk<AnalyticsRepository>(relaxed = true)
    private val preferencias = FakePreferenciasRepository()
    private val sistema = IdiomaSistemaFalso(IdiomaApp.ESPANOL)

    private fun crearViewModel() = AjustesViewModel(
        observarIdioma = ObservarIdiomaUseCase(preferencias, sistema),
        guardarIdioma = GuardarIdiomaUseCase(preferencias),
        analytics = analytics,
        dispatchers = TestDispatcherProvider(),
    )

    @Test
    fun `registra la vista de pantalla con el nombre del placeholder`() {
        crearViewModel()
        verify(exactly = 1) { analytics.logScreenView("ajustes") }
    }

    @Test
    fun `la primera visita marca Automatico y muestra el idioma del dispositivo`() = runTest {
        sistema.idioma = IdiomaApp.FRANCES

        crearViewModel().uiState.test {
            val estado = awaitItem()
            assertNull(estado.elegido)
            assertEquals(IdiomaApp.FRANCES, estado.sistema)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `con el dispositivo en un idioma no soportado, el detectado es el espanol`() = runTest {
        // IdiomaSistemaJvm ya reduce lo no soportado al predeterminado; aquí se comprueba que la
        // pantalla muestra eso y no un hueco.
        sistema.idioma = IdiomaApp.PREDETERMINADO

        crearViewModel().uiState.test {
            assertEquals(IdiomaApp.ESPANOL, awaitItem().sistema)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `elegir un idioma lo guarda, lo marca y lo registra`() = runTest {
        val viewModel = crearViewModel()

        viewModel.onIdiomaSeleccionado(IdiomaApp.ALEMAN)

        assertEquals(listOf<IdiomaApp?>(IdiomaApp.ALEMAN), preferencias.guardados)
        assertEquals(IdiomaApp.ALEMAN, viewModel.uiState.value.elegido)
        verify(exactly = 1) { analytics.logEvent("ajustes_idioma", mapOf("idioma" to "de")) }
    }

    @Test
    fun `elegir el idioma que ya estaba elegido no guarda ni registra`() = runTest {
        val viewModel = crearViewModel()
        viewModel.onIdiomaSeleccionado(IdiomaApp.ALEMAN)

        viewModel.onIdiomaSeleccionado(IdiomaApp.ALEMAN)

        assertEquals(1, preferencias.guardados.size)
        verify(exactly = 1) { analytics.logEvent("ajustes_idioma", mapOf("idioma" to "de")) }
    }

    @Test
    fun `volver a Automatico borra la eleccion y se registra como automatico`() = runTest {
        sistema.idioma = IdiomaApp.INGLES
        val viewModel = crearViewModel()
        viewModel.onIdiomaSeleccionado(IdiomaApp.ALEMAN)

        viewModel.onAutomaticoSeleccionado()

        assertEquals(listOf<IdiomaApp?>(IdiomaApp.ALEMAN, null), preferencias.guardados)
        assertNull(viewModel.uiState.value.elegido)
        assertEquals(IdiomaApp.INGLES, viewModel.uiState.value.sistema)
        verify(exactly = 1) { analytics.logEvent("ajustes_idioma", mapOf("idioma" to "automatico")) }
    }

    @Test
    fun `pulsar Automatico estando ya en automatico no guarda ni registra`() = runTest {
        val viewModel = crearViewModel()

        viewModel.onAutomaticoSeleccionado()

        assertEquals(emptyList<IdiomaApp?>(), preferencias.guardados)
        verify(exactly = 0) { analytics.logEvent("ajustes_idioma", any()) }
    }

    @Test
    fun `el estado sigue al flujo, tambien cuando la eleccion viene de fuera`() = runTest {
        val viewModel = crearViewModel()

        viewModel.uiState.test {
            assertNull(awaitItem().elegido)

            // Otra pantalla (o una restauración) cambia la preferencia: Ajustes se entera igual.
            preferencias.flujo.value = IdiomaApp.ITALIANO
            assertEquals(IdiomaApp.ITALIANO, awaitItem().elegido)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
