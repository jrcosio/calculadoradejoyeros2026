package com.jrblanco.calculadoradejoyeros2021.data.source.remote

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** El cliente real contra un servidor HTTP local del propio JDK. */
class ClienteHttpUrlConnectionTest {

    private lateinit var servidor: HttpServer
    private var cabecerasRecibidas: Map<String, List<String>> = emptyMap()

    @Before
    fun arrancar() {
        servidor = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        servidor.createContext("/ok") { intercambio ->
            cabecerasRecibidas = intercambio.requestHeaders.mapKeys { it.key.lowercase() }
            val cuerpo = "{\"hola\":\"mundo\"}".toByteArray()
            intercambio.sendResponseHeaders(200, cuerpo.size.toLong())
            intercambio.responseBody.use { it.write(cuerpo) }
        }
        servidor.createContext("/limite") { intercambio ->
            val cuerpo = "{\"message\":\"quota\"}".toByteArray()
            intercambio.sendResponseHeaders(429, cuerpo.size.toLong())
            intercambio.responseBody.use { it.write(cuerpo) }
        }
        servidor.start()
    }

    @After
    fun parar() {
        servidor.stop(0)
    }

    private val base get() = "http://127.0.0.1:${servidor.address.port}"

    @Test
    fun `devuelve codigo y cuerpo de una respuesta correcta`() {
        val respuesta = ClienteHttpUrlConnection().get("$base/ok", mapOf("x-rapidapi-key" to "clave", "Accept" to "application/json"))
        assertEquals(200, respuesta.codigo)
        assertEquals("{\"hola\":\"mundo\"}", respuesta.cuerpo)
    }

    @Test
    fun `envia las cabeceras tal cual`() {
        ClienteHttpUrlConnection().get("$base/ok", mapOf("x-rapidapi-key" to "clave", "Accept" to "application/json"))
        assertEquals(listOf("clave"), cabecerasRecibidas["x-rapidapi-key"])
        assertEquals(listOf("application/json"), cabecerasRecibidas["accept"])
    }

    @Test
    fun `lee el cuerpo de error cuando el codigo es de fallo`() {
        val respuesta = ClienteHttpUrlConnection().get("$base/limite", emptyMap())
        assertEquals(429, respuesta.codigo)
        assertEquals("{\"message\":\"quota\"}", respuesta.cuerpo)
    }
}
