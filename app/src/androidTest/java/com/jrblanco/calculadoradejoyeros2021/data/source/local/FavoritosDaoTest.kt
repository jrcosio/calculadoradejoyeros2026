package com.jrblanco.calculadoradejoyeros2021.data.source.local

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * El primer test instrumentado del proyecto que no es de Compose, y existe por un motivo concreto:
 * **el índice único sólo se puede probar de verdad contra SQLite**. De él cuelgan FR-006 (no
 * duplicar) y FR-009 (la doble pulsación), así que no vale simularlo.
 *
 * Base en memoria: rápida, aislada y sin tocar el fichero real de la app.
 */
class FavoritosDaoTest {

    private lateinit var baseDatos: FavoritosDatabase
    private lateinit var dao: FavoritosDao

    @Before
    fun crearBase() {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        baseDatos = Room.inMemoryDatabaseBuilder(contexto, FavoritosDatabase::class.java).build()
        dao = baseDatos.favoritosDao()
    }

    @After
    fun cerrarBase() {
        baseDatos.close()
    }

    @Test
    fun insertar_devuelve_el_id_nuevo() = runTest {
        val id = dao.insertar(fila(firma = "oro|v1|masa=30"))

        assertEquals(1L, id)
        assertNotNull(dao.porId(id))
    }

    @Test
    fun la_segunda_insercion_con_la_misma_firma_devuelve_menos_uno_y_deja_una_sola_fila() = runTest {
        val primera = dao.insertar(fila(firma = "oro|v1|masa=30"))
        val segunda = dao.insertar(fila(firma = "oro|v1|masa=30"))

        assertEquals(1L, primera)
        assertEquals("El índice único rechaza la fila repetida", -1L, segunda)
        dao.observar().test { assertEquals(1, awaitItem().size) }
    }

    @Test
    fun idPorFirma_encuentra_la_fila_que_el_indice_rechazo() = runTest {
        val id = dao.insertar(fila(firma = "plata|v1|masa=100|ley=LEY_925"))
        dao.insertar(fila(firma = "plata|v1|masa=100|ley=LEY_925"))

        assertEquals(id, dao.idPorFirma("plata|v1|masa=100|ley=LEY_925"))
    }

    @Test
    fun idPorFirma_de_algo_que_no_esta_es_null() = runTest {
        assertNull(dao.idPorFirma("chapa|v1|ancho=1|largo=1|espesor=1|material=ORO_9K"))
    }

    @Test
    fun dos_firmas_distintas_conviven() = runTest {
        dao.insertar(fila(firma = "oro|v1|masa=30"))
        dao.insertar(fila(firma = "oro|v1|masa=40"))

        dao.observar().test { assertEquals(2, awaitItem().size) }
    }

    @Test
    fun observar_emite_el_mas_reciente_primero() = runTest {
        dao.insertar(fila(firma = "a", guardadoEn = 3_000L))
        dao.insertar(fila(firma = "b", guardadoEn = 1_000L))
        dao.insertar(fila(firma = "c", guardadoEn = 2_000L))

        dao.observar().test {
            // Por id descendente, no por la fecha: el orden es el de inserción real.
            assertEquals(listOf("c", "b", "a"), awaitItem().map { it.firma })
        }
    }

    @Test
    fun observar_reemite_al_borrar() = runTest {
        val id = dao.insertar(fila(firma = "oro|v1|masa=30"))

        dao.observar().test {
            assertEquals(1, awaitItem().size)

            dao.borrar(id)

            assertEquals(0, awaitItem().size)
        }
    }

    @Test
    fun borrar_un_id_inexistente_afecta_a_cero_filas() = runTest {
        assertEquals(0, dao.borrar(99L))
    }

    private fun fila(
        firma: String,
        tipo: String = "oro",
        datosJson: String = """{"version":1,"cantidad":"30","color":"BLANCO","ley":"LEY_18K"}""",
        guardadoEn: Long = 1_787_670_000_000L,
    ) = FavoritoEntity(
        tipo = tipo,
        firma = firma,
        datosJson = datosJson,
        guardadoEnEpochMillis = guardadoEn,
    )
}
