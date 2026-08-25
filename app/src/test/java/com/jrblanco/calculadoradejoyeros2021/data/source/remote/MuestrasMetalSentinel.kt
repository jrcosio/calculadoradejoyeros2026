package com.jrblanco.calculadoradejoyeros2021.data.source.remote

/**
 * Respuestas reales del proveedor para los tests del parser (ver contracts/metal-quote.md).
 * `AU_USD` es el ejemplo embebido en la web pública del proveedor el 2026-08-25.
 */
object MuestrasMetalSentinel {
    val AU_USD = """
        {"ID":1787665737,"results":[{"symbol":"AU","currency":"USD","ask":4607.4,"mid":4606.4,
        "bid":4605.4,"high":4697.5,"low":4604.6,"open":0,"close":0,"timestamp":1787665680,
        "change":-45.30000000000018,"changePercentage":-0.974046917668312,"unit":"OUNCE",
        "originalTime":"2026-08-25T09:47:59.727Z","extra":"{\"ChangePercentTrade\":-0.99}"}]}
    """.trimIndent()

    /** La misma muestra con la moneda que pide la app. */
    val AU_EUR = AU_USD.replace("\"currency\":\"USD\"", "\"currency\":\"EUR\"")

    fun conUnidad(unidad: String): String = AU_EUR.replace("\"unit\":\"OUNCE\"", "\"unit\":\"$unidad\"")

    fun conSimbolo(simbolo: String): String = AU_EUR.replace("\"symbol\":\"AU\"", "\"symbol\":\"$simbolo\"")
}
