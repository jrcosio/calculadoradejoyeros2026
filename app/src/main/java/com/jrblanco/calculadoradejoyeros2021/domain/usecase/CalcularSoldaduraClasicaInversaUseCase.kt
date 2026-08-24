package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.RecetasSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraClasica
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Soldadura clásica de oro amarillo, modo inverso (§2.2–§2.3 del documento técnico): a
 * partir del peso final deseado, el reparto completo de la receta.
 *
 * Con el total patrón de 1,44 g (muy floja de ley) la división es infinita: por eso la
 * suma de los componentes mostrados puede desviarse una milésima del total pedido, la
 * pantalla lo advierte con la nota de §8.3 y **jamás** se ajusta un ingrediente para
 * cuadrar la vista.
 */
class CalcularSoldaduraClasicaInversaUseCase {

    /**
     * @param pesoFinal gramos de soldadura que se quieren obtener; debe ser mayor que cero.
     * @throws IllegalArgumentException si el peso no es válido (§8.1, TEST 10).
     */
    operator fun invoke(pesoFinal: BigDecimal, tipo: TipoSoldaduraClasica): CalculoSoldadura {
        require(pesoFinal > BigDecimal.ZERO) {
            "El peso final deseado debe ser mayor que cero: $pesoFinal"
        }

        val receta = RecetasSoldadura.clasica(tipo)
        // Única división del modo inverso, a la cifra más cercana (sin ley que proteger).
        val factor = pesoFinal.divide(receta.totalPatron, CalculoSoldadura.ESCALA, RoundingMode.HALF_UP)

        return CalculoSoldadura.escalar(receta, factor)
    }
}
