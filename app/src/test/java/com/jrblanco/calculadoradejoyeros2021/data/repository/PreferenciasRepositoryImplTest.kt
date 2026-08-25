package com.jrblanco.calculadoradejoyeros2021.data.repository

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.data.source.local.FakeAjustesLocalDataSource
import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PreferenciasRepositoryImplTest {

    @Test
    fun `sin nada guardado emite null, que es Automatico`() = runTest {
        val repositorio = PreferenciasRepositoryImpl(FakeAjustesLocalDataSource())

        repositorio.idioma.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `lo guardado se emite tal cual`() = runTest {
        val repositorio = PreferenciasRepositoryImpl(FakeAjustesLocalDataSource(IdiomaApp.ALEMAN))

        repositorio.idioma.test {
            assertEquals(IdiomaApp.ALEMAN, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `guardar un idioma llega al almacen y se emite`() = runTest {
        val local = FakeAjustesLocalDataSource()
        val repositorio = PreferenciasRepositoryImpl(local)

        repositorio.idioma.test {
            assertNull(awaitItem())

            repositorio.guardarIdioma(IdiomaApp.ITALIANO)
            assertEquals(IdiomaApp.ITALIANO, awaitItem())

            // Volver a Automático: el null viaja entero, no se pierde por el camino.
            repositorio.guardarIdioma(null)
            assertNull(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(2, local.escrituras)
    }
}
