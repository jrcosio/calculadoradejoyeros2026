package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.ComponenteCalculado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.RecetasSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraPlata
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Soldadura de plata, modo inverso (§4.3 del documento técnico): a partir del peso final
 * deseado, la plata fina y el latón necesarios.
 *
 * El latón se obtiene por resta y no con una segunda división: así el total recupera
 * exactamente el peso pedido.
 */
class CalcularSoldaduraPlataInversaUseCase {

    /**
     * @param pesoFinal gramos de soldadura que se quieren obtener; debe ser mayor que cero.
     * @throws IllegalArgumentException si el peso no es válido (§8.1, TEST 10).
     */
    operator fun invoke(pesoFinal: BigDecimal, tipo: TipoSoldaduraPlata): CalculoSoldadura {
        require(pesoFinal > BigDecimal.ZERO) {
            "El peso final deseado debe ser mayor que cero: $pesoFinal"
        }

        val unoMasFactor = BigDecimal.ONE.add(RecetasSoldadura.factorLaton(tipo))
        // Única división del modo inverso, a la cifra más cercana (sin ley que proteger).
        val plataFina = pesoFinal.divide(unoMasFactor, CalculoSoldadura.ESCALA, RoundingMode.HALF_UP)
        val laton = pesoFinal.subtract(plataFina)

        return CalculoSoldadura.de(
            listOf(
                ComponenteCalculado(MetalSoldadura.PLATA_FINA, plataFina),
                ComponenteCalculado(MetalSoldadura.LATON, laton),
            ),
        )
    }
}
