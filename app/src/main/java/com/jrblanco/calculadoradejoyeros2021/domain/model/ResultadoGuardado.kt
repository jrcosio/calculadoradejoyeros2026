package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * Qué pasó al guardar un favorito.
 *
 * El duplicado **no es un error**: es el resultado normal de pedir dos veces lo mismo, y por eso
 * viaja como valor y no como excepción (FR-006). En los dos casos hay un `id`, que es lo que la
 * pantalla necesita para poder señalar el favorito afectado.
 */
sealed interface ResultadoGuardado {

    val id: Long

    /** Identificador estable para telemetría, independiente del idioma. */
    val analyticsId: String

    /** No existía: hay fila nueva. */
    data class Guardado(override val id: Long) : ResultadoGuardado {
        override val analyticsId: String get() = "nuevo"
    }

    /** Ya había un favorito con estas mismas entradas; no se ha creado otro. */
    data class YaExistia(override val id: Long) : ResultadoGuardado {
        override val analyticsId: String get() = "repetido"
    }
}
