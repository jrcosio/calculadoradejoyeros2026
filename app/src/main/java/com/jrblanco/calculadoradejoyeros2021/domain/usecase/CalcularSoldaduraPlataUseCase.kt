package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.ComponenteCalculado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.RecetasSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraPlata
import java.math.BigDecimal

/**
 * Soldadura de plata, modo directo (§4.2 del documento técnico): a partir de la plata
 * fina 999 disponible, cuánto latón añadir.
 *
 * El factor `p` es latón **respecto a la plata**, no sobre el peso final (§4.1,
 * interpretación obligatoria). No hay división: latón = plata × p, exacto.
 */
class CalcularSoldaduraPlataUseCase {

    /**
     * @param plataFina gramos de plata fina 999 de partida; debe ser mayor que cero.
     * @throws IllegalArgumentException si la cantidad no es válida (§8.1, TEST 10).
     */
    operator fun invoke(plataFina: BigDecimal, tipo: TipoSoldaduraPlata): CalculoSoldadura {
        require(plataFina > BigDecimal.ZERO) {
            "La plata fina de partida debe ser mayor que cero: $plataFina"
        }

        val laton = plataFina.multiply(RecetasSoldadura.factorLaton(tipo))

        return CalculoSoldadura.de(
            listOf(
                ComponenteCalculado(MetalSoldadura.PLATA_FINA, plataFina),
                ComponenteCalculado(MetalSoldadura.LATON, laton),
            ),
        )
    }
}
