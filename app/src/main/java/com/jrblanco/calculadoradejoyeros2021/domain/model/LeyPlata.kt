package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/**
 * Las cuatro leyes objetivo de la calculadora de plata, en el orden en que se pintan:
 * de mayor a menor finura.
 *
 * @property milesimas ley en milésimas de plata fina (925 = plata Sterling).
 * @property esSoloTecnica marca 950‰ y 900‰. La Ley 17/1985 solo reconoce 999, 925 y 800
 * como leyes oficiales de contraste de plata en España, así que estas dos son
 * composiciones técnicas perfectamente fabricables pero **no** punzones oficiales, y la
 * interfaz debe advertirlo siempre (§3 y §31 del documento técnico).
 *
 * Mismo nombre de bandera que en [LeyOro] a propósito, para que las dos calculadoras
 * decidan su advertencia de la misma manera. La diferencia: aquí la llevan dos de las
 * cuatro leyes, no una.
 */
enum class LeyPlata(
    val milesimas: Int,
    val esSoloTecnica: Boolean = false,
) {
    LEY_950(950, esSoloTecnica = true),
    LEY_925(925),
    LEY_900(900, esSoloTecnica = true),
    LEY_800(800),
    ;

    /** Finura objetivo en fracción decimal exacta (925 → 0.925). */
    val finura: BigDecimal get() = BigDecimal(milesimas).movePointLeft(3)

    /** Identificador estable para telemetría: "950", "925", "900", "800". */
    val analyticsId: String get() = milesimas.toString()
}
