package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * Las cinco durezas de la soldadura de oro de ley por base (§5.4 del documento técnico),
 * en el orden en que se pintan: factor de oro creciente.
 *
 * Regla de interpretación de §5.5: más proporción de base da una soldadura más floja;
 * más oro de 18 K, más fuerte. El factor de cada dureza —gramos de oro de 18 K por gramo
 * de base— no vive aquí sino en [RecetasSoldadura.factorOro], única transcripción de §7.
 */
enum class DurezaSoldaduraLey {
    MUY_FLOJA,
    FLOJA,
    MEDIA,
    FUERTE,
    MUY_FUERTE,
    ;

    /** Identificador estable para telemetría, independiente del idioma. */
    val analyticsId: String get() = name.lowercase()
}
