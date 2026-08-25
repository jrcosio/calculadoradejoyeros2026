package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas

import java.math.BigDecimal
import kotlin.math.cbrt
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Proporciones **visuales** de la chapa dibujada, normalizadas para que siga siendo legible
 * con medidas de taller extremas (0,1 mm de espesor junto a 10 000 mm de largo).
 *
 * El mayor de ancho y largo vale 1; el otro se comprime con raíz cuadrada (una chapa 1:4 se ve
 * 1:2) sin bajar de [MIN_HORIZONTAL]; el espesor, siempre minúsculo frente al largo, se
 * comprime con raíz cúbica y se acota entre [MIN_ESPESOR] y [MAX_ESPESOR]. Una medida ausente
 * toma la de la chapa de referencia (10 × 20 × 0,5 mm). Kotlin puro: se prueba en JVM. Los
 * cálculos de peso **jamás** usan estas proporciones (FR-024).
 */
data class ProporcionesChapa(
    val ancho: Float,
    val largo: Float,
    val espesor: Float,
) {
    companion object {
        val REF_ANCHO: BigDecimal = BigDecimal("10")
        val REF_LARGO: BigDecimal = BigDecimal("20")
        val REF_ESPESOR: BigDecimal = BigDecimal("0.5")

        const val MIN_HORIZONTAL = 0.30f
        const val MIN_ESPESOR = 0.05f
        const val MAX_ESPESOR = 0.45f
        private const val FACTOR_ESPESOR = 0.35f

        fun desde(anchoMm: BigDecimal?, largoMm: BigDecimal?, espesorMm: BigDecimal?): ProporcionesChapa {
            val ancho = (anchoMm ?: REF_ANCHO).toFloat()
            val largo = (largoMm ?: REF_LARGO).toFloat()
            val espesor = (espesorMm ?: REF_ESPESOR).toFloat()
            val mayor = max(ancho, largo)
            return ProporcionesChapa(
                ancho = sqrt(ancho / mayor).coerceIn(MIN_HORIZONTAL, 1f),
                largo = sqrt(largo / mayor).coerceIn(MIN_HORIZONTAL, 1f),
                espesor = (FACTOR_ESPESOR * cbrt(espesor / mayor)).coerceIn(MIN_ESPESOR, MAX_ESPESOR),
            )
        }

        /** La chapa de referencia: lo que se ve antes de teclear nada. */
        val REFERENCIA: ProporcionesChapa = desde(null, null, null)
    }
}
