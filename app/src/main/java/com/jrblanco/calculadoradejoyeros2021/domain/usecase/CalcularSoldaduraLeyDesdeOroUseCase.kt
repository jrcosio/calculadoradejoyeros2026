package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.RecetasSoldadura
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Soldadura de oro de ley desde el oro de 18 K disponible — el modo de los mockups:
 * base = oro / r (§5.4 del documento técnico; 2 g muy floja → 6,67 g de base).
 *
 * El color entra obligatorio (§8.1) y viaja al resultado sin tocar cantidades (TEST 9).
 */
class CalcularSoldaduraLeyDesdeOroUseCase {

    /**
     * @param oro18K gramos de oro de 18 K del color elegido; debe ser mayor que cero.
     * @throws IllegalArgumentException si la cantidad no es válida (§8.1, TEST 10).
     */
    operator fun invoke(
        oro18K: BigDecimal,
        dureza: DurezaSoldaduraLey,
        color: ColorOroSoldadura,
    ): CalculoSoldaduraLey {
        require(oro18K > BigDecimal.ZERO) {
            "El oro de 18 K disponible debe ser mayor que cero: $oro18K"
        }

        // Única división del modo, a la cifra más cercana (sin ley que proteger). Con
        // r = 0,3 es infinita: de ahí la nota de redondeo de §8.3 en pantalla.
        val base = oro18K.divide(
            RecetasSoldadura.factorOro(dureza),
            CalculoSoldadura.ESCALA,
            RoundingMode.HALF_UP,
        )

        return CalculoSoldaduraLey.de(base = base, oro18K = oro18K, color = color, dureza = dureza)
    }
}
