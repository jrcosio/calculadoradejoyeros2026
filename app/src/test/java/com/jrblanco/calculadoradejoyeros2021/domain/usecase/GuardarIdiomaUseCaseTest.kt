package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.data.repository.FakePreferenciasRepository
import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GuardarIdiomaUseCaseTest {

    @Test
    fun `guarda el idioma elegido en el repositorio`() = runTest {
        val preferencias = FakePreferenciasRepository()
        val guardar = GuardarIdiomaUseCase(preferencias)

        guardar(IdiomaApp.ALEMAN)

        assertEquals(listOf<IdiomaApp?>(IdiomaApp.ALEMAN), preferencias.guardados)
    }

    @Test
    fun `guardar null es volver a Automatico`() = runTest {
        val preferencias = FakePreferenciasRepository(IdiomaApp.ALEMAN)
        val guardar = GuardarIdiomaUseCase(preferencias)

        guardar(null)

        assertEquals(listOf<IdiomaApp?>(null), preferencias.guardados)
        assertEquals(null, preferencias.flujo.value)
    }
}
