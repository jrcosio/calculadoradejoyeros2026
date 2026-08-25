package com.jrblanco.calculadoradejoyeros2021.ui.idioma

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.core.util.IdiomaSistemaFalso
import com.jrblanco.calculadoradejoyeros2021.core.util.TestDispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.data.repository.FakePreferenciasRepository
import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ObservarIdiomaUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class IdiomaAppViewModelTest {

    private val preferencias = FakePreferenciasRepository()
    private val sistema = IdiomaSistemaFalso(IdiomaApp.ESPANOL)

    private fun crearViewModel() = IdiomaAppViewModel(
        observarIdioma = ObservarIdiomaUseCase(preferencias, sistema),
        dispatchers = TestDispatcherProvider(),
    )

    @Test
    fun `sin eleccion guardada pinta en el idioma del dispositivo`() = runTest {
        sistema.idioma = IdiomaApp.FRANCES

        crearViewModel().uiState.test {
            assertEquals(IdiomaApp.FRANCES, awaitItem().idioma)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `la eleccion guardada manda sobre el dispositivo`() = runTest {
        sistema.idioma = IdiomaApp.INGLES
        preferencias.flujo.value = IdiomaApp.ALEMAN

        crearViewModel().uiState.test {
            assertEquals(IdiomaApp.ALEMAN, awaitItem().idioma)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cambiar la preferencia repinta la app y volver a Automatico devuelve el control`() = runTest {
        sistema.idioma = IdiomaApp.ESPANOL
        val viewModel = crearViewModel()

        viewModel.uiState.test {
            assertEquals(IdiomaApp.ESPANOL, awaitItem().idioma)

            preferencias.flujo.value = IdiomaApp.ITALIANO
            assertEquals(IdiomaApp.ITALIANO, awaitItem().idioma)

            preferencias.flujo.value = null
            assertEquals(IdiomaApp.ESPANOL, awaitItem().idioma)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `elegir el mismo idioma que el del dispositivo no produce un estado nuevo`() = runTest {
        sistema.idioma = IdiomaApp.ITALIANO
        val viewModel = crearViewModel()

        viewModel.uiState.test {
            assertEquals(IdiomaApp.ITALIANO, awaitItem().idioma)

            // El joyero elige explícitamente el italiano, que ya era el efectivo.
            preferencias.flujo.value = IdiomaApp.ITALIANO
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }
}
