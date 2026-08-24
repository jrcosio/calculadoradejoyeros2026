package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * Los tres colores de oro de 18 K que admite el método de soldadura de ley (§5.1 del
 * documento técnico): amarillo, blanco y rosa.
 *
 * Enum propio y no [ColorOro] a propósito: el de la calculadora de oro incluye ROJO, que
 * este documento no admite, y cada motor es fiel a su propio documento técnico. El color
 * solo cambia el oro de 18 K añadido en la segunda fase — nunca las cantidades (§5.1,
 * TEST 9 de §10).
 */
enum class ColorOroSoldadura {
    AMARILLO,
    BLANCO,
    ROSA,
    ;

    /** Identificador estable para telemetría, independiente del idioma. */
    val analyticsId: String get() = name.lowercase()
}
