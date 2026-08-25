package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.data.repository.FakeCotizacionesRepository
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.instantaneaCompleta
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ObtenerCotizacionesUseCaseTest {

    @Test
    fun `devuelve exactamente la instantanea del repositorio con una sola llamada`() = runTest {
        val repositorio = FakeCotizacionesRepository(respuesta = instantaneaCompleta(obtenidoEn = 1_000))
        val obtener = ObtenerCotizacionesUseCase(repositorio)

        val resultado = obtener()

        assertSame(repositorio.respuesta, resultado)
        assertEquals(1, repositorio.llamadas)
    }

    @Test
    fun `una excepcion del repositorio se propaga sin envolver`() = runTest {
        val repositorio = FakeCotizacionesRepository().apply { excepcion = IllegalStateException("bug") }
        val obtener = ObtenerCotizacionesUseCase(repositorio)

        val lanzada = try {
            obtener()
            null
        } catch (e: IllegalStateException) {
            e
        }
        assertEquals("bug", lanzada?.message)
    }
}
