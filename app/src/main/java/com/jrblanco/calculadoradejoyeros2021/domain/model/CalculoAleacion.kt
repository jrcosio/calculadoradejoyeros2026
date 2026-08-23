package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Resultado completo de aplicar una receta a una cantidad de oro de partida.
 *
 * Todas las cantidades están en gramos y conservan la precisión interna completa
 * (§10 del documento técnico): el redondeo para mostrar es asunto exclusivo de la
 * capa de presentación y jamás realimenta un cálculo (§21).
 */
data class CalculoAleacion(
    /** Gramos de oro 999‰ de partida. */
    val masaOrigen: BigDecimal,
    /** Oro puro realmente contenido en el oro de partida (§4.1). */
    val oroPuro: BigDecimal,
    /** Liga total a añadir (§4.3). */
    val ligaTotal: BigDecimal,
    /** Gramos exactos de cada metal de liga, solo los que usa la receta. */
    val metales: Map<MetalLiga, BigDecimal>,
    /** Peso final de la aleación (§4.2). */
    val masaFinal: BigDecimal,
    /** Ley teórica resultante; nunca por debajo de la objetivo (§12). */
    val leyTeorica: BigDecimal,
) {
    companion object {
        /**
         * Finura real del oro de partida: 999‰, nunca 1000 (§3.1). La regla clásica
         * «100 g + 33,333 g de liga» queda explícitamente descartada.
         */
        val FINURA_ORIGEN: BigDecimal = BigDecimal("0.999")

        /**
         * Escala de las divisiones internas. El documento pide un mínimo de 6
         * decimales (§10); 15 deja el error computacional muy por debajo de
         * cualquier báscula de taller.
         */
        const val ESCALA = 15

        /** Tolerancia puramente computacional de las verificaciones (§12). */
        val TOLERANCIA: BigDecimal = BigDecimal("1E-9")

        /**
         * Reparte la liga entre los metales de la receta y verifica el resultado.
         *
         * Las multiplicaciones son exactas: aquí no se redondea nada (§10). Quien
         * llama es responsable de haber redondeado su única división **a favor de la
         * ley** (a la baja el peso final en el modo directo, al alza el oro necesario
         * en el inverso), y la verificación final lo garantiza.
         */
        internal fun repartir(
            masaOrigen: BigDecimal,
            masaFinal: BigDecimal,
            receta: RecetaLiga,
        ): CalculoAleacion {
            val oroPuro = masaOrigen.multiply(FINURA_ORIGEN)
            val ligaTotal = masaFinal.subtract(masaOrigen)
            val metales = receta.proporciones.mapValues { (_, proporcion) ->
                ligaTotal.multiply(proporcion)
            }
            val sumaMetales = metales.values.fold(BigDecimal.ZERO, BigDecimal::add)
            val leyTeorica = oroPuro.divide(
                masaOrigen.add(sumaMetales),
                ESCALA,
                RoundingMode.DOWN,
            )

            // Verificación obligatoria (§12): red de seguridad, no lógica de negocio.
            check((sumaMetales - ligaTotal).abs() < TOLERANCIA) {
                "La suma de metales ($sumaMetales) no coincide con la liga ($ligaTotal)"
            }
            check(leyTeorica >= receta.ley.finura) {
                "La ley resultante ($leyTeorica) queda por debajo de la objetivo (${receta.ley.finura})"
            }

            return CalculoAleacion(
                masaOrigen = masaOrigen,
                oroPuro = oroPuro,
                ligaTotal = ligaTotal,
                metales = metales,
                masaFinal = masaFinal,
                leyTeorica = leyTeorica,
            )
        }
    }
}
