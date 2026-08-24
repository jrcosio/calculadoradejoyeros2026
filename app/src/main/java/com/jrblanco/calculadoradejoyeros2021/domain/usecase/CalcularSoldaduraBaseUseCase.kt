package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.RecetasSoldadura
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Preparación de la base de oro de 18 K desde el oro fino disponible (§5.2 del documento
 * técnico): factor = oro / 10 sobre la receta de la base.
 *
 * Nota de §5.2 sobre la ley nominal: la proporción teórica de oro es 754,15 milésimas,
 * pero se conserva el nombre tradicional «base de oro de 18 K», la interfaz **no**
 * muestra esa ley y está prohibido corregir los pesos para forzar 750 milésimas.
 */
class CalcularSoldaduraBaseUseCase {

    /**
     * @param oro24K gramos de oro fino de 24 K; debe ser mayor que cero.
     * @throws IllegalArgumentException si la cantidad no es válida (§8.1, TEST 10).
     */
    operator fun invoke(oro24K: BigDecimal): CalculoSoldadura {
        require(oro24K > BigDecimal.ZERO) {
            "El oro fino de partida debe ser mayor que cero: $oro24K"
        }

        val patronOro = RecetasSoldadura.BASE.componentes.first().pesoPatron
        // Única división del modo directo, a la cifra más cercana (sin ley que proteger).
        val factor = oro24K.divide(patronOro, CalculoSoldadura.ESCALA, RoundingMode.HALF_UP)

        return CalculoSoldadura.escalar(RecetasSoldadura.BASE, factor)
    }
}
