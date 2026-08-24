package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * Los tres tipos de soldadura clásica de oro amarillo (§3.1 del documento técnico), en
 * el orden en que se pintan.
 *
 * Son recetas cerradas de oro amarillo: §8.1 prohíbe elegir color en esta familia, y por
 * eso ningún caso de uso clásico acepta un color — la prohibición va en el diseño de
 * tipos, no en una validación.
 */
enum class TipoSoldaduraClasica {
    FLOJA,
    FUERTE,
    MUY_FLOJA_LEY,
    ;

    /** Identificador estable para telemetría, independiente del idioma. */
    val analyticsId: String get() = name.lowercase()

    /**
     * `true` si la receta contiene cadmio y obliga a la advertencia de seguridad de §9.
     * Mismo mecanismo que `LeyPlata.esSoloTecnica`: la pantalla decide el aviso desde el
     * dominio, sin un campo de UI que pueda desincronizarse de la receta.
     */
    val llevaCadmio: Boolean get() = this == MUY_FLOJA_LEY
}
