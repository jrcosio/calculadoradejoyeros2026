package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/**
 * Las cifras que un favorito produce, recalculadas y **sin formatear**: el redondeo de vista es
 * exclusivo del ViewModel y no es el mismo en las cinco calculadoras (oro y soldaduras a la media,
 * plata truncando, chapas a dos decimales), así que aquí no se toca ni un decimal.
 *
 * Una variante por motor, y no una lista plana de líneas con un enum de ingredientes: no hay ningún
 * enum que pueda nombrar a la vez un metal de liga, un ingrediente de receta y la soldadura BASE sin
 * ser el cuarto enum paralelo del proyecto (`MetalLiga`, `MetalSoldadura`, `IngredienteSoldadura` y
 * el nuevo). Cada variante habla el tipo de su propio motor, que es la regla de la casa; el aplanado
 * que la tarjeta necesita se hace en `ui/favoritos/`, que es donde viven los mapeos de presentación.
 *
 * No repite nada que el joyero eligiera: eso sigue en las entradas del favorito. Por eso
 * [SoldaduraLey] no lleva color ni dureza y [Chapa] no lleva densidad ni pureza — la pantalla los
 * tiene a mano en `entradas` y duplicarlos sería una segunda fuente de verdad.
 */
sealed interface ResumenFavorito {

    /** Los metales de la liga con sus gramos exactos, en el orden que da el motor. */
    data class Oro(
        val metales: Map<MetalLiga, BigDecimal>,
        val masaFinal: BigDecimal,
    ) : ResumenFavorito

    /** El cobre es el único metal de liga de la plata (§2, §33): campo, no mapa. */
    data class Plata(
        val cobre: BigDecimal,
        val masaFinal: BigDecimal,
    ) : ResumenFavorito

    /**
     * La base y —sólo en el modo inverso— el oro de 18 K. [oro18K] es nulo en el modo directo
     * porque ahí el oro es lo que el joyero ya tiene y no se pinta como fila.
     */
    data class SoldaduraLey(
        val base: BigDecimal,
        val oro18K: BigDecimal?,
        val total: BigDecimal,
    ) : ResumenFavorito

    /** Clásica, de plata y BASE: los componentes del motor, ya filtrados según el modo. */
    data class Soldadura(
        val componentes: List<ComponenteCalculado>,
        val total: BigDecimal,
    ) : ResumenFavorito

    data class Chapa(
        val peso: BigDecimal,
        val volumenCm3: BigDecimal,
        val metalFino: BigDecimal,
    ) : ResumenFavorito
}
