package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/** Un ingrediente de una receta de soldadura con su peso de referencia en gramos. */
data class ComponenteReceta(
    val metal: MetalSoldadura,
    val pesoPatron: BigDecimal,
)

/**
 * Receta de soldadura escalable proporcionalmente (§2.2 del documento técnico).
 *
 * **Lista y no mapa a propósito**: §8.2 exige presentar los ingredientes, y cada receta
 * ordena los suyos según su propia tabla (§3.2–§3.4 y §5.2) — a diferencia de
 * [RecetaLiga] en oro, cuyo orden de pintado lo pone el enum. El orden de esta lista es
 * el orden estable de presentación.
 */
data class RecetaSoldadura(
    val componentes: List<ComponenteReceta>,
) {
    /** Suma exacta de los pesos patrón (8; 6,50; 1,44; 13,26). */
    val totalPatron: BigDecimal = componentes.fold(BigDecimal.ZERO) { suma, componente ->
        suma.add(componente.pesoPatron)
    }

    init {
        require(componentes.isNotEmpty()) { "Una receta de soldadura no puede estar vacía" }
        require(componentes.all { it.pesoPatron > BigDecimal.ZERO }) {
            "Todos los pesos patrón deben ser positivos: $componentes"
        }
        require(componentes.distinctBy { it.metal }.size == componentes.size) {
            "Una receta no puede repetir metal: $componentes"
        }
    }
}
