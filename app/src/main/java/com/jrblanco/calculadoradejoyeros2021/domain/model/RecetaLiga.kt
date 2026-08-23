package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/**
 * Proporciones internas de la liga para una combinación color×ley (§6 del documento
 * técnico): qué parte del añadido corresponde a cada metal. Son fracciones de la
 * **liga**, nunca porcentajes de la masa total final.
 *
 * Solo aparecen los metales que la receta usa: los ausentes no existen a cero.
 */
data class RecetaLiga(
    val color: ColorOro,
    val ley: LeyOro,
    val proporciones: Map<MetalLiga, BigDecimal>,
) {
    init {
        require(proporciones.isNotEmpty()) { "Una receta sin metales de liga no es una receta" }
        require(proporciones.values.all { it > BigDecimal.ZERO }) {
            "Las proporciones de la liga deben ser positivas: $this"
        }
        val suma = proporciones.values.fold(BigDecimal.ZERO, BigDecimal::add)
        // Tolerancia computacional, no igualdad estricta: los literales del documento
        // no siempre suman 1 exacto (los tres del blanco 750 suman 0.9999999999999999).
        require((suma - BigDecimal.ONE).abs() < CalculoAleacion.TOLERANCIA) {
            "Las proporciones de la liga deben sumar 1 y suman $suma: $this"
        }
    }
}
