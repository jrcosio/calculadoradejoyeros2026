package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.RecetasSoldadura
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Soldadura de oro de ley desde el peso final deseado (§5.4 del documento técnico,
 * TEST 8): base = peso / (1 + r) y oro = peso − base.
 *
 * El oro se obtiene por resta y no con una segunda división: así el total recupera
 * exactamente el peso pedido.
 */
class CalcularSoldaduraLeyInversaUseCase {

    /**
     * @param pesoFinal gramos de soldadura que se quieren obtener; debe ser mayor que cero.
     * @throws IllegalArgumentException si el peso no es válido (§8.1, TEST 10).
     */
    operator fun invoke(
        pesoFinal: BigDecimal,
        dureza: DurezaSoldaduraLey,
        color: ColorOroSoldadura,
    ): CalculoSoldaduraLey {
        require(pesoFinal > BigDecimal.ZERO) {
            "El peso final deseado debe ser mayor que cero: $pesoFinal"
        }

        val unoMasFactor = BigDecimal.ONE.add(RecetasSoldadura.factorOro(dureza))
        // Única división del modo inverso, a la cifra más cercana (sin ley que proteger).
        val base = pesoFinal.divide(unoMasFactor, CalculoSoldadura.ESCALA, RoundingMode.HALF_UP)
        val oro18K = pesoFinal.subtract(base)

        return CalculoSoldaduraLey.de(base = base, oro18K = oro18K, color = color, dureza = dureza)
    }
}
