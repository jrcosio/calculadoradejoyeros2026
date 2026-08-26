package com.jrblanco.calculadoradejoyeros2021.data.repository

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.core.util.RelojFalso
import com.jrblanco.calculadoradejoyeros2021.data.source.local.FakeFavoritosLocalDataSource
import com.jrblanco.calculadoradejoyeros2021.domain.model.FavoritosDePrueba
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El repositorio no decide casi nada: lo que se prueba aquí es que **sella la hora** con el reloj
 * inyectado y que no se inventa comportamiento por encima del data source.
 */
class FavoritosRepositoryImplTest {

    private val t0 = 1_787_670_000_000L
    private val reloj = RelojFalso(t0)
    private val local = FakeFavoritosLocalDataSource()

    private val repositorio = FavoritosRepositoryImpl(local, reloj)

    @Test
    fun `guardar sella la hora del reloj y no la que traiga nadie`() = runTest {
        repositorio.guardar(FavoritosDePrueba.oro())

        assertEquals(t0, repositorio.obtener(1L)?.guardadoEnEpochMillis)
    }

    @Test
    fun `dos guardados en momentos distintos llevan su propia hora`() = runTest {
        repositorio.guardar(FavoritosDePrueba.oro(masaOrigen = "30"))
        reloj.avanzar(60_000)
        repositorio.guardar(FavoritosDePrueba.oro(masaOrigen = "40"))

        assertEquals(t0, repositorio.obtener(1L)?.guardadoEnEpochMillis)
        assertEquals(t0 + 60_000, repositorio.obtener(2L)?.guardadoEnEpochMillis)
    }

    @Test
    fun `guardar algo nuevo devuelve Guardado con su id`() = runTest {
        val resultado = repositorio.guardar(FavoritosDePrueba.oro())

        assertTrue(resultado is ResultadoGuardado.Guardado)
        assertEquals(1L, resultado.id)
    }

    @Test
    fun `guardar lo mismo otra vez devuelve YaExistia con el mismo id y no duplica`() = runTest {
        val primero = repositorio.guardar(FavoritosDePrueba.oro())
        val segundo = repositorio.guardar(FavoritosDePrueba.oro())

        assertTrue(segundo is ResultadoGuardado.YaExistia)
        assertEquals(primero.id, segundo.id)
        repositorio.favoritos.test {
            assertEquals(1, awaitItem().size)
        }
    }

    @Test
    fun `la escala de la cantidad no crea un favorito nuevo`() = runTest {
        repositorio.guardar(FavoritosDePrueba.oro(masaOrigen = "30"))
        val segundo = repositorio.guardar(FavoritosDePrueba.oro(masaOrigen = "30.000"))

        assertTrue("«30» y «30.000» son el mismo favorito", segundo is ResultadoGuardado.YaExistia)
    }

    @Test
    fun `la lista emite el mas reciente primero`() = runTest {
        repositorio.guardar(FavoritosDePrueba.oro(masaOrigen = "10"))
        repositorio.guardar(FavoritosDePrueba.oro(masaOrigen = "20"))
        repositorio.guardar(FavoritosDePrueba.oro(masaOrigen = "30"))

        repositorio.favoritos.test {
            assertEquals(listOf(3L, 2L, 1L), awaitItem().map { it.id })
        }
    }

    @Test
    fun `borrar quita ese favorito y deja los demas`() = runTest {
        repositorio.guardar(FavoritosDePrueba.oro(masaOrigen = "10"))
        repositorio.guardar(FavoritosDePrueba.plata())

        repositorio.borrar(1L)

        repositorio.favoritos.test {
            assertEquals(listOf(2L), awaitItem().map { it.id })
        }
        assertNull(repositorio.obtener(1L))
    }

    @Test
    fun `borrar un id que ya no existe no es un error`() = runTest {
        repositorio.borrar(99L)

        repositorio.favoritos.test { assertTrue(awaitItem().isEmpty()) }
    }

    @Test
    fun `un favorito borrado se puede volver a guardar`() = runTest {
        repositorio.guardar(FavoritosDePrueba.oro())
        repositorio.borrar(1L)

        val resultado = repositorio.guardar(FavoritosDePrueba.oro())

        assertTrue("Vuelve a entrar como nuevo", resultado is ResultadoGuardado.Guardado)
    }

    @Test
    fun `obtener devuelve las entradas guardadas`() = runTest {
        val entradas = FavoritosDePrueba.soldaduraLey(cantidad = "7")
        repositorio.guardar(entradas)

        assertEquals(entradas, repositorio.obtener(1L)?.entradas)
    }

    @Test
    fun `obtener un id inexistente devuelve null`() = runTest {
        assertNull(repositorio.obtener(42L))
    }
}
