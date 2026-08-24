package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * Los cuatro tipos de soldadura de plata (§4.1 del documento técnico), en el orden en
 * que se pintan: de más floja a más fuerte.
 *
 * Interpretación obligatoria de §4.1: el factor de cada tipo es la cantidad de **latón
 * respecto a la plata fina**, no sobre el peso final. El factor no vive aquí sino en
 * [RecetasSoldadura.factorLaton], para que §7 tenga una única transcripción.
 */
enum class TipoSoldaduraPlata {
    MUY_FLOJA,
    FLOJA,
    NORMAL,
    FUERTE,
    ;

    /** Identificador estable para telemetría, independiente del idioma. */
    val analyticsId: String get() = name.lowercase()
}
