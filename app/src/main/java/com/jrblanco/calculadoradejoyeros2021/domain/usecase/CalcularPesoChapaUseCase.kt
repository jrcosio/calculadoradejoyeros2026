package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.MaterialChapa
import java.math.BigDecimal

/**
 * Peso de una chapa rectangular a partir de sus tres medidas en milímetros y su material.
 *
 * Solo exige medidas mayores que cero (§11.1). Los límites operativos de §11.4 (ancho y
 * largo hasta 10 000 mm, espesor hasta 1 000 mm) son controles de interfaz y viven en el
 * ViewModel, no aquí: el motor calcula cualquier chapa físicamente posible.
 */
class CalcularPesoChapaUseCase {
    /** @throws IllegalArgumentException si alguna medida no es mayor que cero. */
    operator fun invoke(
        ancho: BigDecimal,
        largo: BigDecimal,
        espesor: BigDecimal,
        material: MaterialChapa,
    ): CalculoChapa {
        require(ancho > BigDecimal.ZERO) { "El ancho debe ser mayor que cero: $ancho" }
        require(largo > BigDecimal.ZERO) { "El largo debe ser mayor que cero: $largo" }
        require(espesor > BigDecimal.ZERO) { "El espesor debe ser mayor que cero: $espesor" }
        return CalculoChapa.de(ancho = ancho, largo = largo, espesor = espesor, material = material)
    }
}
