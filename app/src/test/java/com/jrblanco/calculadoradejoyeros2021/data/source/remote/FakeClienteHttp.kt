package com.jrblanco.calculadoradejoyeros2021.data.source.remote

/** [ClienteHttp] de test: cola de respuestas o excepciones y registro de la última petición. */
class FakeClienteHttp : ClienteHttp {
    private val programadas = ArrayDeque<Any>()

    var llamadas = 0
        private set
    var ultimaUrl: String? = null
        private set
    var ultimasCabeceras: Map<String, String> = emptyMap()
        private set

    fun responder(codigo: Int, cuerpo: String) {
        programadas.addLast(RespuestaHttp(codigo, cuerpo))
    }

    fun fallar(excepcion: Throwable) {
        programadas.addLast(excepcion)
    }

    override fun get(url: String, cabeceras: Map<String, String>): RespuestaHttp {
        llamadas++
        ultimaUrl = url
        ultimasCabeceras = cabeceras
        val siguiente = programadas.removeFirstOrNull() ?: error("Sin respuesta programada para $url")
        if (siguiente is Throwable) throw siguiente
        return siguiente as RespuestaHttp
    }
}
