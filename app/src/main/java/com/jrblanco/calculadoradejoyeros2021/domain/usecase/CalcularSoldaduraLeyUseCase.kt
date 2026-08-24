package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.RecetasSoldadura
import java.math.BigDecimal

/**
 * Soldadura de oro de ley desde la base disponible (§5.4 del documento técnico, TEST 7):
 * oro = base × r, sin división, exacto.
 *
 * **Sin interfaz en esta versión**: existe y se prueba desde el principio porque §5.4 lo
 * define y los criterios de §11 lo exigen — el mismo precedente que
 * [CalcularAleacionInversaPlataUseCase] en la feature 005.
 */
class CalcularSoldaduraLeyUseCase {

    /**
     * @param baseDisponible gramos de soldadura base; debe ser mayor que cero.
     * @throws IllegalArgumentException si la cantidad no es válida (§8.1, TEST 10).
     */
    operator fun invoke(
        baseDisponible: BigDecimal,
        dureza: DurezaSoldaduraLey,
        color: ColorOroSoldadura,
    ): CalculoSoldaduraLey {
        require(baseDisponible > BigDecimal.ZERO) {
            "La base disponible debe ser mayor que cero: $baseDisponible"
        }

        val oro18K = baseDisponible.multiply(RecetasSoldadura.factorOro(dureza))

        return CalculoSoldaduraLey.de(
            base = baseDisponible,
            oro18K = oro18K,
            color = color,
            dureza = dureza,
        )
    }
}
