package com.jrblanco.calculadoradejoyeros2021.data.source.remote

import java.io.IOException

/** Una respuesta HTTP reducida a lo que la app necesita: el código y el cuerpo como texto. */
data class RespuestaHttp(val codigo: Int, val cuerpo: String)

/**
 * Cliente HTTP mínimo detrás de una interfaz.
 *
 * Bloqueante a propósito: quien salta de hilo es el data source, con el `DispatcherProvider`
 * inyectado. Existe para poder sustituir la implementación (hoy `HttpURLConnection` del JDK,
 * sin dependencias) por otra cuando llegue un backend propio o una segunda API, y para poder
 * falsearla en los tests.
 */
interface ClienteHttp {
    @Throws(IOException::class)
    fun get(url: String, cabeceras: Map<String, String>): RespuestaHttp
}
