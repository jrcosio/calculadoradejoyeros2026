package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * La regla de precedencia del idioma, como función pura: **lo que el joyero eligió manda sobre lo
 * que diga el dispositivo**. Es el único sitio del proyecto donde se decide qué idioma se muestra.
 *
 * @property elegido lo que el joyero eligió en Ajustes; `null` es «Automático», que es a la vez el
 * estado inicial y un estado elegible (se puede volver a él).
 * @property sistema el idioma del dispositivo, ya reducido a uno de los soportados.
 */
data class SeleccionIdioma(
    val elegido: IdiomaApp?,
    val sistema: IdiomaApp,
) {
    /** El idioma que la app muestra. Lo único que la interfaz necesita saber. */
    val efectivo: IdiomaApp get() = elegido ?: sistema

    /** `true` cuando manda el dispositivo: es lo que marca la fila «Automático» de Ajustes. */
    val esAutomatico: Boolean get() = elegido == null
}
