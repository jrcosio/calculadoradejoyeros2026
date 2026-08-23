package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * Los cuatro colores de oro que ofrece la calculadora, en el orden de la interfaz.
 *
 * Kotlin puro a propósito: sin `R`, sin `Color`, sin nada de Android. El mapeo a
 * textos y acentos vive en la capa Compose, igual que `HomeModule`.
 */
enum class ColorOro {
    AMARILLO,
    BLANCO,
    ROSA,
    ROJO,
    ;

    /** Identificador estable para telemetría, independiente del idioma. */
    val analyticsId: String get() = name.lowercase()
}
