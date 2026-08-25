package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * Los cinco metales cuya cotización consulta la herramienta de precios.
 *
 * El orden del enum **es** el orden de pintado de la lista (oro, plata, cobre, paladio,
 * rodio, como el mockup). [simboloApi] es el código que espera el proveedor, en mayúsculas;
 * la pantalla lo muestra tal cual como símbolo químico. Las imágenes y los nombres visibles
 * los mapea la pantalla: `domain` no conoce recursos.
 */
enum class MetalCotizado(val simboloApi: String) {
    ORO("AU"),
    PLATA("AG"),
    COBRE("CU"),
    PALADIO("PD"),
    RODIO("RH"),
    ;

    /** Identificador estable para telemetría: "oro", "plata", "cobre", "paladio", "rodio". */
    val analyticsId: String get() = name.lowercase()
}
