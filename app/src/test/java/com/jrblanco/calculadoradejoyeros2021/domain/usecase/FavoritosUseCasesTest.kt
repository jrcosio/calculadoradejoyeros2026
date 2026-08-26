package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.data.repository.FakeFavoritosRepository
import com.jrblanco.calculadoradejoyeros2021.domain.model.FavoritosDePrueba
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Los cuatro casos de uso finos de favoritos. Son de una línea, como `GuardarIdiomaUseCase`, y lo
 * único que hay que fijar es que delegan sin añadir ni quitar nada: la capa existe para que `ui/`
 * no hable con un repositorio.
 */
class FavoritosUseCasesTest {

    private val repositorio = FakeFavoritosRepository()

    @Test
    fun `observar reemite la lista del repositorio`() = runTest {
        val favorito = FavoritosDePrueba.favorito()
        repositorio.flujo.value = listOf(favorito)

        ObservarFavoritosUseCase(repositorio)().test {
            assertEquals(listOf(favorito), awaitItem())
        }
    }

    @Test
    fun `observar propaga los cambios`() = runTest {
        ObservarFavoritosUseCase(repositorio)().test {
            assertEquals(emptyList<Any>(), awaitItem())

            repositorio.flujo.value = listOf(FavoritosDePrueba.favorito())

            assertEquals(1, awaitItem().size)
        }
    }

    @Test
    fun `guardar entrega las entradas al repositorio y devuelve su resultado`() = runTest {
        val entradas = FavoritosDePrueba.chapa()
        repositorio.resultadoGuardar = ResultadoGuardado.Guardado(7L)

        val resultado = GuardarFavoritoUseCase(repositorio)(entradas)

        assertEquals(listOf(entradas), repositorio.guardados)
        assertEquals(ResultadoGuardado.Guardado(7L), resultado)
    }

    @Test
    fun `guardar propaga el duplicado tal cual`() = runTest {
        repositorio.resultadoGuardar = ResultadoGuardado.YaExistia(3L)

        val resultado = GuardarFavoritoUseCase(repositorio)(FavoritosDePrueba.oro())

        assertEquals(ResultadoGuardado.YaExistia(3L), resultado)
    }

    @Test
    fun `borrar pasa el id al repositorio`() = runTest {
        repositorio.flujo.value = listOf(FavoritosDePrueba.favorito(id = 5L))

        BorrarFavoritoUseCase(repositorio)(5L)

        assertEquals(listOf(5L), repositorio.borrados)
        assertEquals(emptyList<Any>(), repositorio.flujo.value)
    }

    @Test
    fun `obtener devuelve el favorito del repositorio`() = runTest {
        val favorito = FavoritosDePrueba.favorito(id = 9L)
        repositorio.flujo.value = listOf(favorito)

        assertEquals(favorito, ObtenerFavoritoUseCase(repositorio)(9L))
    }

    @Test
    fun `obtener un id que no esta devuelve null`() = runTest {
        assertNull(ObtenerFavoritoUseCase(repositorio)(9L))
    }
}
