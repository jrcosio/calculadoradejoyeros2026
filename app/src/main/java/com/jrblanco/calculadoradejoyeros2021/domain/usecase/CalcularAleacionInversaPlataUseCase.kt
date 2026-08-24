package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoPlata
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Modo inverso (§22 del documento técnico): «quiero fabricar X gramos de plata de esta
 * ley», cuánta plata fina 999‰ hace falta y cuánto cobre añadir.
 *
 * Sin interfaz en esta versión: existe y se prueba desde el principio para que la pantalla
 * pueda exponerlo en el futuro sin tocar el motor (FR-016).
 */
class CalcularAleacionInversaPlataUseCase {

    /**
     * @param masaFinalDeseada gramos de aleación final que se quieren obtener.
     * @throws IllegalArgumentException si la masa no es válida (§26).
     */
    operator fun invoke(masaFinalDeseada: BigDecimal, ley: LeyPlata): CalculoPlata {
        require(masaFinalDeseada > BigDecimal.ZERO) {
            "La masa final deseada debe ser mayor que cero: $masaFinalDeseada"
        }

        // Única división del modo inverso, redondeada AL ALZA: aquí lo que protege la ley
        // es poner una pizca MÁS de plata fina, no menos — redondear a la baja dejaría la
        // aleación una billonésima por debajo de la objetivo y eso está prohibido (§20).
        val masaOrigen = masaFinalDeseada
            .multiply(ley.finura)
            .divide(CalculoPlata.FINURA_ORIGEN, CalculoPlata.ESCALA, RoundingMode.UP)

        return CalculoPlata.de(masaOrigen = masaOrigen, masaFinal = masaFinalDeseada, ley = ley)
    }
}
