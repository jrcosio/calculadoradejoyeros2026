package com.jrblanco.calculadoradejoyeros2021.data.source.local

import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El data source real, sobre el fichero real. **Un solo `@Test` a propósito**: la base se crea
 * `by lazy` dentro del data source, así que dos tests serían dos instancias de `RoomDatabase` sobre
 * el mismo fichero en el mismo proceso — exactamente lo que el `single` de Koin evita en producción.
 * El recorrido completo va en un único método.
 */
class RoomFavoritosLocalDataSourceTest {

    @Test
    fun guardar_observar_obtener_duplicar_y_borrar_sobre_el_fichero_real() = runTest {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        contexto.deleteDatabase(RoomFavoritosLocalDataSource.FICHERO)
        val fuente = RoomFavoritosLocalDataSource(contexto)
        val entradas = EntradasFavorito.Plata(BigDecimal("100"), LeyPlata.LEY_950)

        // Guardar
        val guardado = fuente.guardar(entradas, guardadoEnEpochMillis = 1_787_670_000_000L)
        assertTrue(guardado is ResultadoGuardado.Guardado)

        // Observar
        fuente.favoritos.test {
            val lista = awaitItem()
            assertEquals(1, lista.size)
            assertEquals(entradas, lista.single().entradas)
            assertEquals(1_787_670_000_000L, lista.single().guardadoEnEpochMillis)
        }

        // Obtener
        assertEquals(entradas, fuente.obtener(guardado.id)?.entradas)

        // Duplicar: mismo id, sin fila nueva
        val repetido = fuente.guardar(entradas, guardadoEnEpochMillis = 1_787_680_000_000L)
        assertTrue(repetido is ResultadoGuardado.YaExistia)
        assertEquals(guardado.id, repetido.id)
        fuente.favoritos.test { assertEquals(1, awaitItem().size) }

        // Borrar
        fuente.borrar(guardado.id)
        assertNull(fuente.obtener(guardado.id))
        fuente.favoritos.test { assertTrue(awaitItem().isEmpty()) }

        contexto.deleteDatabase(RoomFavoritosLocalDataSource.FICHERO)
    }
}
