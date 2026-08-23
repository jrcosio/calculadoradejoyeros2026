package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoAleacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.RecetasOro
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Modo inverso (§14 del documento técnico): «quiero fabricar X gramos de aleación
 * final», cuánto oro 999‰ hace falta y cómo se reparte la liga.
 *
 * Sin interfaz en esta versión: existe y se prueba desde el principio para que la
 * pantalla pueda exponerlo en el futuro sin tocar el motor (FR-016).
 */
class CalcularAleacionInversaOroUseCase {

    /**
     * @param masaFinalDeseada gramos de aleación final que se quieren obtener.
     * @throws IllegalArgumentException si la masa no es válida (§16).
     */
    operator fun invoke(masaFinalDeseada: BigDecimal, color: ColorOro, ley: LeyOro): CalculoAleacion {
        require(masaFinalDeseada > BigDecimal.ZERO) {
            "La masa final deseada debe ser mayor que cero: $masaFinalDeseada"
        }

        // Única división del modo inverso, redondeada AL ALZA: aquí lo que protege la
        // ley es poner una pizca MÁS de oro, no menos — redondear a la baja dejaría la
        // aleación una billonésima por debajo de la objetivo y eso está prohibido (§12).
        val masaOrigen = masaFinalDeseada
            .multiply(ley.finura)
            .divide(CalculoAleacion.FINURA_ORIGEN, CalculoAleacion.ESCALA, RoundingMode.UP)

        return CalculoAleacion.repartir(
            masaOrigen = masaOrigen,
            masaFinal = masaFinalDeseada,
            receta = RecetasOro.receta(color, ley),
        )
    }
}
