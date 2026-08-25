package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * Unidades de precio. Las tres primeras son las que el joyero puede elegir (gramo por defecto,
 * kilo, onza troy); [LIBRA] existe solo como **unidad de origen**: el proveedor cotiza el cobre
 * por libra avoirdupois (`unit: "POUND"`, confirmado con la credencial real el 2026-08-25) y
 * hay que poder convertirla, pero no se ofrece en el selector ([seleccionables]).
 *
 * Sin valor «desconocida» a propósito: un valor imposible obligaría a filtrarlo en todas
 * partes. Cuando el proveedor no confirma la unidad de un metal, la cotización lleva
 * `unidadOrigen = null` y la etiqueta cruda del proveedor, y se muestra sin convertir.
 */
enum class UnidadPrecio {
    GRAMO,
    KILO,
    ONZA_TROY,
    LIBRA,
    ;

    /** Identificador estable para telemetría: "gramo", "kilo", "onza_troy", "libra". */
    val analyticsId: String get() = name.lowercase()

    companion object {
        /** Las unidades del selector, en su orden. */
        val seleccionables: List<UnidadPrecio> = listOf(GRAMO, KILO, ONZA_TROY)
    }
}
