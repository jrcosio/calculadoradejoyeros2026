package com.jrblanco.calculadoradejoyeros2021.ui.info

/**
 * Perfiles externos a los que da acceso la pantalla de información.
 *
 * Kotlin puro, sin `R` ni `Color`: es lo que permite testear el ViewModel en la JVM. El
 * mapeo a icono, acento y textos vive en la capa Compose, igual que en `HomeModule`.
 *
 * La URL va aquí y no en `strings.xml` porque no es texto traducible, es el destino. La
 * dirección que se muestra en pantalla sí es un recurso.
 */
enum class InfoEnlace(val url: String) {
    LINKEDIN("https://www.linkedin.com/in/jr-blanco/"),
    INSTAGRAM("https://www.instagram.com/blancojoyeros/"),
    ;

    /** Identificador estable para telemetría: no se traduce y no cambia con el idioma. */
    val analyticsId: String get() = name.lowercase()
}
