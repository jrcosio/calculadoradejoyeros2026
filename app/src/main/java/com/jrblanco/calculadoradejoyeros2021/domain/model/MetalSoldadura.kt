package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * Los ingredientes que intervienen en las recetas y factores de soldadura.
 *
 * A diferencia de [MetalLiga] en oro, **el orden del enum no es el orden de pintado**:
 * cada receta ordena distinto sus ingredientes (§3.2–§3.4 y §5.2 del documento técnico),
 * así que el orden estable de presentación viaja en [RecetaSoldadura], no aquí.
 *
 * El color del oro de 18 K tampoco vive aquí: no cambia el metal ni su peso, solo el
 * material que se identifica (TEST 9 de §10); es un atributo de [CalculoSoldaduraLey].
 */
enum class MetalSoldadura {
    ORO_24K,
    ORO_18K,
    PLATA_FINA,
    LATON,
    COBRE,
    ZINC,
    CADMIO,
    ;

    /** Identificador estable para telemetría, independiente del idioma. */
    val analyticsId: String get() = name.lowercase()
}
