package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoAleacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.RecetasOro
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Modo directo (§9 del documento técnico): a partir del oro 999‰ disponible, cuánta
 * liga añadir y de qué metales para alcanzar la ley y el color elegidos.
 */
class CalcularAleacionOroUseCase {

    /**
     * @param masaOrigen gramos de oro 999‰ de partida; debe ser mayor que cero.
     * @throws IllegalArgumentException si la masa no es válida (§16).
     */
    operator fun invoke(masaOrigen: BigDecimal, color: ColorOro, ley: LeyOro): CalculoAleacion {
        require(masaOrigen > BigDecimal.ZERO) {
            "La masa de oro de partida debe ser mayor que cero: $masaOrigen"
        }

        val oroPuro = masaOrigen.multiply(CalculoAleacion.FINURA_ORIGEN)
        // Única división del modo directo, redondeada A LA BAJA: un peso final una
        // pizca menor da una liga una pizca menor, y la ley real queda siempre igual
        // o por encima de la objetivo (§12) — la norma no admite tolerancia en menos.
        val masaFinal = oroPuro.divide(ley.finura, CalculoAleacion.ESCALA, RoundingMode.DOWN)

        return CalculoAleacion.repartir(
            masaOrigen = masaOrigen,
            masaFinal = masaFinal,
            receta = RecetasOro.receta(color, ley),
        )
    }
}
