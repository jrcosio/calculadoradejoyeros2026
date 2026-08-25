package com.jrblanco.calculadoradejoyeros2021.data.source.remote

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

/**
 * [ClienteHttp] sobre `java.net.HttpURLConnection`, que en Android va sobre el OkHttp interno
 * de la plataforma. Cinco GET por hora a un solo host no justifican una dependencia.
 *
 * HTTPS lo impone la URL de producción; aceptar `HttpURLConnection` genérico permite probar
 * el cliente contra un servidor local en JVM. Nunca registra cabeceras ni URL: la credencial
 * viaja en una cabecera.
 */
class ClienteHttpUrlConnection(
    private val tiempoConexionMs: Int = 10_000,
    private val tiempoLecturaMs: Int = 15_000,
) : ClienteHttp {

    @Throws(IOException::class)
    override fun get(url: String, cabeceras: Map<String, String>): RespuestaHttp {
        val conexion = URI.create(url).toURL().openConnection() as HttpURLConnection
        try {
            conexion.requestMethod = "GET"
            conexion.connectTimeout = tiempoConexionMs
            conexion.readTimeout = tiempoLecturaMs
            cabeceras.forEach { (nombre, valor) -> conexion.setRequestProperty(nombre, valor) }

            val codigo = conexion.responseCode
            val flujo = if (codigo < 400) conexion.inputStream else conexion.errorStream
            val cuerpo = flujo?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            return RespuestaHttp(codigo = codigo, cuerpo = cuerpo)
        } finally {
            conexion.disconnect()
        }
    }
}
