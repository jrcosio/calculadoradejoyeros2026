package com.jrblanco.calculadoradejoyeros2021.core.util

/**
 * Hora del sistema detrás de una interfaz, hermana de [DispatcherProvider].
 *
 * Existe para que la política de caché de cotizaciones decida «¿han pasado ya sesenta
 * minutos?» sin mirar el reloj de verdad: en los tests se inyecta un reloj falso que se
 * avanza a mano, y una vigencia de una hora se comprueba en milisegundos de ejecución.
 * Es un tipo del grafo de Koin a propósito, y no un valor por defecto de constructor: así
 * vale tanto para `verify()` como para la resolución real de `viewModelOf`/`factoryOf`.
 */
interface Reloj {
    /** Instante actual en milisegundos desde la época Unix. */
    fun ahoraMillis(): Long
}

class RelojSistema : Reloj {
    override fun ahoraMillis(): Long = System.currentTimeMillis()
}
