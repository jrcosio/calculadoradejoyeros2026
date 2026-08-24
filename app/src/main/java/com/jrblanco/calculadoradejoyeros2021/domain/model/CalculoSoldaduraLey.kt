package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/**
 * Resultado de la mezcla de soldadura de oro de ley: base + oro de 18 K del color
 * elegido (§5.4 del documento técnico).
 *
 * Tipo propio y no un [CalculoSoldadura] a propósito: la base es un preparado de la
 * primera fase, no un [MetalSoldadura], y el color debe viajar en el resultado — cambia
 * el material identificado, nunca su peso (§5.1, TEST 9 de §10).
 */
data class CalculoSoldaduraLey(
    /** Gramos de soldadura base. */
    val base: BigDecimal,
    /** Gramos de oro de 18 K del color elegido. */
    val oro18K: BigDecimal,
    val color: ColorOroSoldadura,
    val dureza: DurezaSoldaduraLey,
    /** Peso final teórico: base + oro, exacto. */
    val total: BigDecimal,
) {
    companion object {
        /**
         * Construye el resultado y verifica sus invariantes como `check`: base y oro
         * positivos y total igual a su suma exacta. Red de seguridad, no lógica de
         * negocio.
         */
        internal fun de(
            base: BigDecimal,
            oro18K: BigDecimal,
            color: ColorOroSoldadura,
            dureza: DurezaSoldaduraLey,
        ): CalculoSoldaduraLey {
            check(base > BigDecimal.ZERO) { "La base debe ser positiva y salió $base" }
            check(oro18K > BigDecimal.ZERO) { "El oro debe ser positivo y salió $oro18K" }

            return CalculoSoldaduraLey(
                base = base,
                oro18K = oro18K,
                color = color,
                dureza = dureza,
                total = base.add(oro18K),
            )
        }
    }
}
