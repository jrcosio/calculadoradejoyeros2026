package com.jrblanco.calculadoradejoyeros2021.data.source.remote

import com.jrblanco.calculadoradejoyeros2021.core.util.RelojFalso
import com.jrblanco.calculadoradejoyeros2021.core.util.TestDispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MotivoErrorCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.UnidadPrecio
import java.io.IOException
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** El parser se prueba contra la muestra real del proveedor (contracts/metal-quote.md). */
class MetalSentinelDataSourceTest {

    private val cliente = FakeClienteHttp()
    private val reloj = RelojFalso(1_787_670_000_000L)

    private fun crear(credencial: String = "clave-de-prueba") =
        MetalSentinelDataSource(cliente, TestDispatcherProvider(), reloj, credencial)

    @Test
    fun `parsea la muestra real con los importes exactos`() = runTest {
        cliente.responder(200, MuestrasMetalSentinel.AU_EUR)

        val cotizacion = crear().obtener(MetalCotizado.ORO)

        assertEquals(MetalCotizado.ORO, cotizacion.metal)
        assertEquals("EUR", cotizacion.moneda)
        assertEquals("4606.4", cotizacion.mid.toPlainString())
        assertEquals("4607.4", cotizacion.ask.toPlainString())
        assertEquals("4605.4", cotizacion.bid.toPlainString())
        assertEquals("4697.5", cotizacion.maximo.toPlainString())
        assertEquals("4604.6", cotizacion.minimo.toPlainString())
        assertEquals("-45.30000000000018", cotizacion.variacion.toPlainString())
        assertEquals("-0.974046917668312", cotizacion.variacionPorcentaje.toPlainString())
        assertEquals(UnidadPrecio.ONZA_TROY, cotizacion.unidadOrigen)
        assertEquals("OUNCE", cotizacion.etiquetaUnidadOrigen)
        assertEquals(1_787_665_680_000L, cotizacion.instanteMercadoEpochMillis)
        assertEquals(1_787_670_000_000L, cotizacion.obtenidoEnEpochMillis)
    }

    @Test
    fun `pide la url y las cabeceras del contrato`() = runTest {
        cliente.responder(200, MuestrasMetalSentinel.AU_EUR)

        crear().obtener(MetalCotizado.ORO)

        assertEquals("https://metal-sentinel.p.rapidapi.com/api/metal-quote?metal=AU&currency=EUR", cliente.ultimaUrl)
        assertEquals("metal-sentinel.p.rapidapi.com", cliente.ultimasCabeceras["x-rapidapi-host"])
        assertEquals("clave-de-prueba", cliente.ultimasCabeceras["x-rapidapi-key"])
        assertEquals("application/json", cliente.ultimasCabeceras["Accept"])
    }

    @Test
    fun `cada metal pide su simbolo`() = runTest {
        MetalCotizado.entries.forEach { metal ->
            cliente.responder(200, MuestrasMetalSentinel.conSimbolo(metal.simboloApi))
            val cotizacion = crear().obtener(metal)
            assertEquals(metal, cotizacion.metal)
            assertEquals(true, cliente.ultimaUrl!!.contains("metal=${metal.simboloApi}&"))
        }
    }

    @Test
    fun `un timestamp en milisegundos no se multiplica`() = runTest {
        cliente.responder(200, MuestrasMetalSentinel.AU_EUR.replace("\"timestamp\":1787665680", "\"timestamp\":1787665680000"))
        assertEquals(1_787_665_680_000L, crear().obtener(MetalCotizado.ORO).instanteMercadoEpochMillis)
    }

    @Test
    fun `una unidad desconocida se conserva sin convertir`() = runTest {
        cliente.responder(200, MuestrasMetalSentinel.conUnidad("LB"))
        val cotizacion = crear().obtener(MetalCotizado.ORO)
        assertNull(cotizacion.unidadOrigen)
        assertEquals("LB", cotizacion.etiquetaUnidadOrigen)
    }

    @Test
    fun `las variantes de gramo y kilo se reconocen`() = runTest {
        cliente.responder(200, MuestrasMetalSentinel.conUnidad("GRAM"))
        assertEquals(UnidadPrecio.GRAMO, crear().obtener(MetalCotizado.ORO).unidadOrigen)
        cliente.responder(200, MuestrasMetalSentinel.conUnidad("kg"))
        assertEquals(UnidadPrecio.KILO, crear().obtener(MetalCotizado.ORO).unidadOrigen)
        cliente.responder(200, MuestrasMetalSentinel.conUnidad("troy ounce"))
        assertEquals(UnidadPrecio.ONZA_TROY, crear().obtener(MetalCotizado.ORO).unidadOrigen)
    }

    @Test
    fun `con varios resultados elige el del simbolo pedido`() = runTest {
        val platino = """{"symbol":"PT","currency":"EUR","ask":1,"mid":1,"bid":1,"high":1,"low":1,"open":0,"close":0,"timestamp":1,"change":0,"changePercentage":0,"unit":"OUNCE","originalTime":""},"""
        cliente.responder(200, MuestrasMetalSentinel.AU_EUR.replace("\"results\":[", "\"results\":[$platino"))
        assertEquals(BigDecimal("4606.4"), crear().obtener(MetalCotizado.ORO).mid)
    }

    @Test
    fun `moneda distinta, simbolo ausente o json roto son respuesta invalida`() = runTest {
        cliente.responder(200, MuestrasMetalSentinel.AU_USD)
        assertMotivo(MotivoErrorCotizacion.RESPUESTA_INVALIDA) { crear().obtener(MetalCotizado.ORO) }

        cliente.responder(200, MuestrasMetalSentinel.AU_EUR)
        assertMotivo(MotivoErrorCotizacion.RESPUESTA_INVALIDA) { crear().obtener(MetalCotizado.PLATA) }

        cliente.responder(200, "{\"ID\":1,\"results\":[{\"symbol\":\"AU\"")
        assertMotivo(MotivoErrorCotizacion.RESPUESTA_INVALIDA) { crear().obtener(MetalCotizado.ORO) }

        cliente.responder(200, "no es json")
        assertMotivo(MotivoErrorCotizacion.RESPUESTA_INVALIDA) { crear().obtener(MetalCotizado.ORO) }
    }

    @Test
    fun `los codigos http se traducen a su motivo`() = runTest {
        mapOf(
            401 to MotivoErrorCotizacion.CREDENCIAL_RECHAZADA,
            403 to MotivoErrorCotizacion.CREDENCIAL_RECHAZADA,
            404 to MotivoErrorCotizacion.NO_ENCONTRADO,
            429 to MotivoErrorCotizacion.LIMITE_ALCANZADO,
            500 to MotivoErrorCotizacion.SERVIDOR,
            503 to MotivoErrorCotizacion.SERVIDOR,
            418 to MotivoErrorCotizacion.DESCONOCIDO,
        ).forEach { (codigo, motivo) ->
            cliente.responder(codigo, "{\"message\":\"error\"}")
            assertMotivo(motivo) { crear().obtener(MetalCotizado.ORO) }
        }
    }

    @Test
    fun `sin red es sin conexion`() = runTest {
        cliente.fallar(IOException("timeout"))
        assertMotivo(MotivoErrorCotizacion.SIN_CONEXION) { crear().obtener(MetalCotizado.ORO) }
    }

    @Test
    fun `sin credencial no se toca la red`() = runTest {
        assertMotivo(MotivoErrorCotizacion.SIN_CREDENCIAL) { crear(credencial = "").obtener(MetalCotizado.ORO) }
        assertMotivo(MotivoErrorCotizacion.SIN_CREDENCIAL) { crear(credencial = "   ").obtener(MetalCotizado.ORO) }
        assertEquals(0, cliente.llamadas)
    }

    private suspend fun assertMotivo(esperado: MotivoErrorCotizacion, accion: suspend () -> Unit) {
        val lanzada = try {
            accion()
            null
        } catch (e: MetalSentinelException) {
            e
        }
        assertEquals("esperaba $esperado", esperado, lanzada?.motivo)
    }
}
