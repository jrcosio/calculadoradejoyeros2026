package com.jrblanco.calculadoradejoyeros2021.ui.home

/**
 * Los cuatro módulos de cálculo que ofrece la app, en el orden del menú.
 *
 * Kotlin puro a propósito: sin `R`, sin `Color`, sin nada de Android. Es lo que permite
 * que [HomeViewModel] siga sin conocer la plataforma y que su test corra en la JVM. El
 * mapeo a imagen, textos y color de acento vive en la capa Compose.
 */
enum class HomeModule {
    ORO,
    PLATA,
    SOLDADURAS,
    HERRAMIENTAS,
    ;

    /** Nombre que se registra en telemetría. */
    val analyticsId: String get() = name.lowercase()
}
