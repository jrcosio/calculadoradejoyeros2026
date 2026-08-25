package com.jrblanco.calculadoradejoyeros2021.core.util

/** [Reloj] de test que solo avanza cuando el test lo dice. */
class RelojFalso(var ahoraMillis: Long = 0L) : Reloj {
    override fun ahoraMillis(): Long = ahoraMillis

    fun avanzar(millis: Long) {
        ahoraMillis += millis
    }
}
