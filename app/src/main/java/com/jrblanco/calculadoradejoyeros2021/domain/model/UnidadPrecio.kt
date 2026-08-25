package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * Unidad en la que el joyero quiere ver los precios: gramo (por defecto), kilo u onza troy.
 *
 * Sin valor «desconocida» a propósito: este enum alimenta el selector de la pantalla y el
 * `when` del conversor, y un valor imposible obligaría a filtrarlo en todas partes. Cuando el
 * proveedor no confirma la unidad de un metal, la cotización lleva `unidadOrigen = null` y la
 * etiqueta cruda del proveedor, y se muestra sin convertir.
 */
enum class UnidadPrecio {
    GRAMO,
    KILO,
    ONZA_TROY,
    ;

    /** Identificador estable para telemetría: "gramo", "kilo", "onza_troy". */
    val analyticsId: String get() = name.lowercase()
}
