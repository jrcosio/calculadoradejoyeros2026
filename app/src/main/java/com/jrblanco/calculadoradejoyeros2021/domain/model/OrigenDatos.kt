package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * De dónde salió la instantánea que recibe la pantalla.
 *
 * [RED] acaba de consultarse al proveedor (al menos un metal); [CACHE] se sirvió lo guardado
 * porque todo sigue vigente; [CACHE_EN_ESPERA] se sirvió lo guardado porque el último intento
 * fue hace demasiado poco para volver a molestar al proveedor.
 */
enum class OrigenDatos {
    RED,
    CACHE,
    CACHE_EN_ESPERA,
}
