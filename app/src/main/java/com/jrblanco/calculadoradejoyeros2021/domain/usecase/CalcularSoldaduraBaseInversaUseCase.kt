package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.RecetasSoldadura
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Preparación de la base de oro de 18 K desde el peso de base deseado (§5.2 del
 * documento técnico): factor = peso / 13,26 — división infinita, de ahí la nota de
 * redondeo de §8.3 en pantalla.
 *
 * Como en el modo directo: el nombre «base de oro de 18 K» se conserva, su ley real no
 * se muestra y los pesos no se corrigen hacia 750 milésimas (§5.2).
 */
class CalcularSoldaduraBaseInversaUseCase {

    /**
     * @param pesoBase gramos de base que se quieren obtener; debe ser mayor que cero.
     * @throws IllegalArgumentException si el peso no es válido (§8.1, TEST 10).
     */
    operator fun invoke(pesoBase: BigDecimal): CalculoSoldadura {
        require(pesoBase > BigDecimal.ZERO) {
            "El peso de base deseado debe ser mayor que cero: $pesoBase"
        }

        val receta = RecetasSoldadura.BASE
        // Única división del modo inverso, a la cifra más cercana (sin ley que proteger).
        val factor = pesoBase.divide(receta.totalPatron, CalculoSoldadura.ESCALA, RoundingMode.HALF_UP)

        return CalculoSoldadura.escalar(receta, factor)
    }
}
