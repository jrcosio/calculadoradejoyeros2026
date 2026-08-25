package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.core.util.IdiomaSistemaFalso
import com.jrblanco.calculadoradejoyeros2021.data.repository.FakePreferenciasRepository
import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservarIdiomaUseCaseTest {

    @Test
    fun `sin eleccion guardada el idioma efectivo es el del dispositivo`() = runTest {
        val preferencias = FakePreferenciasRepository()
        val observar = ObservarIdiomaUseCase(preferencias, IdiomaSistemaFalso(IdiomaApp.FRANCES))

        observar().test {
            val seleccion = awaitItem()
            assertTrue(seleccion.esAutomatico)
            assertEquals(IdiomaApp.FRANCES, seleccion.sistema)
            assertEquals(IdiomaApp.FRANCES, seleccion.efectivo)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `con eleccion guardada manda la eleccion y el sistema sigue visible`() = runTest {
        val preferencias = FakePreferenciasRepository(IdiomaApp.ALEMAN)
        val observar = ObservarIdiomaUseCase(preferencias, IdiomaSistemaFalso(IdiomaApp.INGLES))

        observar().test {
            val seleccion = awaitItem()
            assertFalse(seleccion.esAutomatico)
            assertEquals(IdiomaApp.ALEMAN, seleccion.efectivo)
            // El idioma del dispositivo se sigue exponiendo: Ajustes lo muestra en «Automático».
            assertEquals(IdiomaApp.INGLES, seleccion.sistema)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cada cambio de la preferencia produce una emision nueva`() = runTest {
        val preferencias = FakePreferenciasRepository()
        val observar = ObservarIdiomaUseCase(preferencias, IdiomaSistemaFalso(IdiomaApp.ESPANOL))

        observar().test {
            assertEquals(IdiomaApp.ESPANOL, awaitItem().efectivo)

            preferencias.flujo.value = IdiomaApp.ITALIANO
            assertEquals(IdiomaApp.ITALIANO, awaitItem().efectivo)

            preferencias.flujo.value = null
            val vuelta = awaitItem()
            assertTrue(vuelta.esAutomatico)
            assertEquals(IdiomaApp.ESPANOL, vuelta.efectivo)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `el idioma del dispositivo se relee en cada emision`() = runTest {
        val preferencias = FakePreferenciasRepository()
        val sistema = IdiomaSistemaFalso(IdiomaApp.ESPANOL)
        val observar = ObservarIdiomaUseCase(preferencias, sistema)

        observar().test {
            assertEquals(IdiomaApp.ESPANOL, awaitItem().efectivo)

            // El joyero cambia el idioma del móvil sin que muera el proceso.
            sistema.idioma = IdiomaApp.ITALIANO
            preferencias.flujo.value = null
            // Mismo valor de preferencia: el flujo de un StateFlow no reemite, así que se fuerza
            // un cambio real para observar la relectura.
            preferencias.flujo.value = IdiomaApp.INGLES
            assertEquals(IdiomaApp.INGLES, awaitItem().efectivo)

            preferencias.flujo.value = null
            val ultima = awaitItem()
            assertEquals(IdiomaApp.ITALIANO, ultima.sistema)
            assertEquals(IdiomaApp.ITALIANO, ultima.efectivo)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
