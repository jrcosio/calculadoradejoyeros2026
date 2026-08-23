package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * Los metales que pueden componer la liga.
 *
 * El orden del enum es también el orden en que se pintan las filas de resultado:
 * plata fina, cobre y paladio, como en el diseño de referencia.
 */
enum class MetalLiga {
    PLATA_FINA,
    COBRE,
    PALADIO,
    ;

    /** Identificador estable para telemetría, independiente del idioma. */
    val analyticsId: String get() = name.lowercase()
}
