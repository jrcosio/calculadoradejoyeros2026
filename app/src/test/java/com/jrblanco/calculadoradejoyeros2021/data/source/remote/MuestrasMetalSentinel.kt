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

    /** Respuesta real de `/metal-quote?symbol=AU&currency=EUR` (2026-08-25, sin `ID` ni `extra`). */
    val AU_EUR_REAL = """{"results":[{"symbol":"AU","currency":"EUR","ask":3987.0087350000003,"mid":3986.151885,"bid":3985.295035,"high":4025.052875,"low":3945.4515100000003,"open":0,"close":0,"timestamp":1787672940,"change":-3.145285000000058,"changePercentage":-0.07886002416102489,"unit":"OUNCE","originalTime":"2026-08-25T11:48:59.657Z"}]}"""

    /** Respuesta real del cobre (2026-08-25): cotiza por **libra**, no por onza. */
    val CU_EUR_REAL = """{"results":[{"symbol":"CU","currency":"EUR","ask":5.613238349729337,"mid":5.612946820240344,"bid":5.612655290751351,"high":5.613562271383772,"low":5.51750395260395,"open":0,"close":0,"timestamp":1787673000,"change":0.06252185370260932,"changePercentage":1.1264928025920586,"unit":"POUND","originalTime":"2026-08-25T11:36:21Z"}]}"""

    /** Lo que devuelve el proveedor (con HTTP 200) si el parámetro no es `symbol`. */
    val ERROR_SIN_SYMBOL = """{"error":"The symbol field is required.","errors":{"symbol":["The symbol field is required."]}}"""

    fun conUnidad(unidad: String): String = AU_EUR.replace("\"unit\":\"OUNCE\"", "\"unit\":\"$unidad\"")

    fun conSimbolo(simbolo: String): String = AU_EUR.replace("\"symbol\":\"AU\"", "\"symbol\":\"$simbolo\"")
}
