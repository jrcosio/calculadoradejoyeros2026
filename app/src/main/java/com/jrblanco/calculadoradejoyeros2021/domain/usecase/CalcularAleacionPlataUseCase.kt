package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoPlata
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Modo directo (§9 del documento técnico): a partir de la plata fina 999‰ disponible,
 * cuánto cobre añadir para alcanzar la ley elegida.
 *
 * No hay tabla de coeficientes por ley: §28 la prohíbe expresamente aunque diera el mismo
 * resultado, y exige la fórmula general. Así el motor soportará leyes futuras sin tocar la
 * lógica.
 */
class CalcularAleacionPlataUseCase {

    /**
     * @param masaOrigen gramos de plata fina 999‰ de partida; debe ser mayor que cero.
     * @throws IllegalArgumentException si la masa no es válida (§26).
     */
    operator fun invoke(masaOrigen: BigDecimal, ley: LeyPlata): CalculoPlata {
        require(masaOrigen > BigDecimal.ZERO) {
            "La masa de plata de partida debe ser mayor que cero: $masaOrigen"
        }

        val plataPura = masaOrigen.multiply(CalculoPlata.FINURA_ORIGEN)
        // Única división del modo directo, redondeada A LA BAJA: un peso final una pizca
        // menor pide una pizca menos de cobre, y la ley real queda siempre igual o por
        // encima de la objetivo (§20) — la norma no admite tolerancia en menos (§16).
        val masaFinal = plataPura.divide(ley.finura, CalculoPlata.ESCALA, RoundingMode.DOWN)

        return CalculoPlata.de(masaOrigen = masaOrigen, masaFinal = masaFinal, ley = ley)
    }
}
