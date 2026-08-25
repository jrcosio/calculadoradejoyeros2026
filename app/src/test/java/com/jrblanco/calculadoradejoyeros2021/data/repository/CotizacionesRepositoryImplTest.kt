package com.jrblanco.calculadoradejoyeros2021.data.repository

import com.jrblanco.calculadoradejoyeros2021.core.util.RelojFalso
import com.jrblanco.calculadoradejoyeros2021.data.source.local.FakeCotizacionesLocalDataSource
import com.jrblanco.calculadoradejoyeros2021.data.source.remote.FakeCotizacionesRemoteDataSource
import com.jrblanco.calculadoradejoyeros2021.data.source.remote.MetalSentinelException
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MotivoErrorCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.OrigenDatos
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoCotizacion
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El repositorio con fakes y reloj congelado: aquí se demuestra que la red solo se toca
 * cuando la política lo dice (SC-001, SC-013).
 */
class CotizacionesRepositoryImplTest {

    private val t0 = 1_787_670_000_000L
    private val minuto = 60_000L
    private val hora = 3_600_000L

    private val reloj = RelojFalso(t0)
    private val remoto = FakeCotizacionesRemoteDataSource(reloj).apply { programarTodos() }
    private val local = FakeCotizacionesLocalDataSource()

    private fun crear() = CotizacionesRepositoryImpl(remoto, local, reloj)

    @Test
    fun `la primera carga consulta los cinco metales y persiste`() = runTest {
        val instantanea = crear().obtenerCotizaciones()

        assertEquals(5, remoto.totalLlamadas)
        assertTrue(instantanea.estaCompleta)
        assertEquals(OrigenDatos.RED, instantanea.origen)
        assertEquals(t0, instantanea.instanteIntentoEpochMillis)
        assertEquals(1, local.escrituras)
    }

    @Test
    fun `dentro de la hora se sirve de cache sin tocar la red`() = runTest {
        val repositorio = crear()
        repositorio.obtenerCotizaciones()
        reloj.avanzar(59 * minuto)

        val segunda = repositorio.obtenerCotizaciones()

        assertEquals(5, remoto.totalLlamadas)
        assertEquals(OrigenDatos.CACHE, segunda.origen)
        assertEquals(1, local.escrituras)
    }

    @Test
    fun `pasada la hora se vuelve a consultar todo`() = runTest {
        val repositorio = crear()
        repositorio.obtenerCotizaciones()
        reloj.avanzar(61 * minuto)

        val tercera = repositorio.obtenerCotizaciones()

        assertEquals(10, remoto.totalLlamadas)
        assertEquals(OrigenDatos.RED, tercera.origen)
        assertEquals(2, local.escrituras)
    }

    @Test
    fun `un metal que falla no tumba a los demas`() = runTest {
        remoto.fallos[MetalCotizado.RODIO] = MetalSentinelException(MotivoErrorCotizacion.SIN_CONEXION, "sin red")

        val instantanea = crear().obtenerCotizaciones()

        assertFalse(instantanea.estaCompleta)
        assertEquals(4, instantanea.resultados.values.count { it is ResultadoCotizacion.Exito })
        val rodio = instantanea.resultados[MetalCotizado.RODIO] as ResultadoCotizacion.Error
        assertEquals(MotivoErrorCotizacion.SIN_CONEXION, rodio.motivo)
        assertEquals(OrigenDatos.RED, instantanea.origen)
    }

    @Test
    fun `reintentar antes del minuto no consulta y lo dice`() = runTest {
        remoto.fallos[MetalCotizado.RODIO] = MetalSentinelException(MotivoErrorCotizacion.SIN_CONEXION, "sin red")
        val repositorio = crear()
        repositorio.obtenerCotizaciones()
        reloj.avanzar(30_000)

        val reintento = repositorio.obtenerCotizaciones()

        assertEquals(5, remoto.totalLlamadas)
        assertEquals(OrigenDatos.CACHE_EN_ESPERA, reintento.origen)
    }

    @Test
    fun `pasado el minuto solo se reintenta el fallido`() = runTest {
        remoto.fallos[MetalCotizado.RODIO] = MetalSentinelException(MotivoErrorCotizacion.SIN_CONEXION, "sin red")
        val repositorio = crear()
        repositorio.obtenerCotizaciones()
        reloj.avanzar(61_000)
        remoto.fallos.remove(MetalCotizado.RODIO)

        val reintento = repositorio.obtenerCotizaciones()

        assertEquals(2, remoto.llamadas[MetalCotizado.RODIO])
        assertEquals(1, remoto.llamadas[MetalCotizado.ORO])
        assertEquals(6, remoto.totalLlamadas)
        assertTrue(reintento.estaCompleta)
        assertEquals(OrigenDatos.RED, reintento.origen)
    }

    @Test
    fun `un exito caducado que ahora falla conserva la ultima conocida`() = runTest {
        val repositorio = crear()
        repositorio.obtenerCotizaciones()
        reloj.avanzar(61 * minuto)
        remoto.fallos[MetalCotizado.ORO] = MetalSentinelException(MotivoErrorCotizacion.SERVIDOR, "500")

        val instantanea = repositorio.obtenerCotizaciones()

        val oro = instantanea.resultados[MetalCotizado.ORO] as ResultadoCotizacion.Error
        assertEquals(MotivoErrorCotizacion.SERVIDOR, oro.motivo)
        assertEquals(t0, oro.ultimaConocida?.obtenidoEnEpochMillis)
    }

    @Test
    fun `tras morir el proceso la cache persistida evita la red`() = runTest {
        crear().obtenerCotizaciones()
        reloj.avanzar(10 * minuto)

        val renacido = CotizacionesRepositoryImpl(remoto, local, reloj)
        val instantanea = renacido.obtenerCotizaciones()

        assertEquals(5, remoto.totalLlamadas)
        assertEquals(OrigenDatos.CACHE, instantanea.origen)
        assertTrue(instantanea.estaCompleta)
    }

    @Test
    fun `sin nada guardado se parte de la instantanea vacia`() = runTest {
        local.guardada = null
        val instantanea = crear().obtenerCotizaciones()
        assertEquals(1, local.lecturas)
        assertTrue(instantanea.estaCompleta)
    }

    @Test
    fun `dos llamadas concurrentes solo gastan una ronda de red`() = runTest {
        val puerta = CompletableDeferred<Unit>()
        remoto.puerta = puerta
        val repositorio = crear()

        val primera = async { repositorio.obtenerCotizaciones() }
        val segunda = async { repositorio.obtenerCotizaciones() }
        puerta.complete(Unit)

        assertEquals(OrigenDatos.RED, primera.await().origen)
        assertEquals(OrigenDatos.CACHE, segunda.await().origen)
        assertEquals(5, remoto.totalLlamadas)
    }

    @Test
    fun `una excepcion que no es del proveedor se propaga`() = runTest {
        remoto.fallos[MetalCotizado.ORO] = IllegalStateException("bug")
        val lanzada = try {
            crear().obtenerCotizaciones()
            null
        } catch (e: IllegalStateException) {
            e
        }
        assertEquals("bug", lanzada?.message)
    }
}
