package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/**
 * Las cuatro leyes objetivo de la calculadora.
 *
 * @property milesimas ley en milésimas de oro fino (750 = 18 quilates).
 * @property esSoloTecnica marca 500‰: la Ley 17/1985 no lo recoge como ley oficial
 * española y la interfaz debe advertirlo siempre (§2 del documento técnico).
 */
enum class LeyOro(
    val milesimas: Int,
    val esSoloTecnica: Boolean = false,
) {
    LEY_18K(750),
    LEY_14K(585),
    LEY_12K(500, esSoloTecnica = true),
    LEY_9K(375),
    ;

    /** Finura objetivo en fracción decimal exacta (750 → 0.750). */
    val finura: BigDecimal get() = BigDecimal(milesimas).movePointLeft(3)

    /** Identificador estable para telemetría: "18k", "14k", "12k", "9k". */
    val analyticsId: String get() = name.removePrefix("LEY_").lowercase()
}
