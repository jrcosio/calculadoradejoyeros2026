package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.RecetasSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraClasica
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Soldadura clásica de oro amarillo, modo directo (el de los mockups): a partir del oro
 * disponible —18 K en floja y fuerte, 24 K en muy floja de ley—, cuánto añadir de cada
 * ingrediente de la receta (§3 del documento técnico).
 *
 * Sin parámetro de color por diseño de tipos: §8.1 prohíbe elegir color en las recetas
 * clásicas, que son de oro amarillo.
 */
class CalcularSoldaduraClasicaUseCase {

    /**
     * @param oroDisponible gramos del oro de la receta; debe ser mayor que cero.
     * @throws IllegalArgumentException si la cantidad no es válida (§8.1, TEST 10).
     */
    operator fun invoke(oroDisponible: BigDecimal, tipo: TipoSoldaduraClasica): CalculoSoldadura {
        require(oroDisponible > BigDecimal.ZERO) {
            "El oro disponible debe ser mayor que cero: $oroDisponible"
        }

        val receta = RecetasSoldadura.clasica(tipo)
        val patronOro = receta.componentes.first().pesoPatron
        // Única división del modo directo, redondeada a la cifra más cercana: aquí no hay
        // ley de contraste que proteger (a diferencia del DOWN/UP direccional de plata),
        // así que HALF_UP deja el factor en el valor más fiel al exacto.
        val factor = oroDisponible.divide(patronOro, CalculoSoldadura.ESCALA, RoundingMode.HALF_UP)

        return CalculoSoldadura.escalar(receta, factor)
    }
}
