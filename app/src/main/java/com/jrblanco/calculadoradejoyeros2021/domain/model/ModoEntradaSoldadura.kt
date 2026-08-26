package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * Los dos sentidos en que se puede resolver una soldadura (§2.3 del documento técnico): partiendo
 * del metal que el joyero ya tiene, o del peso final que quiere obtener.
 *
 * Vivía en `ui/soldaduras/` con el KDoc «concepto de UI: ningún caso de uso lo recibe». Dejó de ser
 * verdad con la feature 009: `ResumirFavoritoUseCase` lo recibe para elegir el motor directo o el
 * inverso al rehacer las cifras de un favorito, así que baja a dominio por la misma regla del
 * segundo consumidor que subió `parsearDecimalPositivo` a `core/util/`. No es una decisión de
 * pantalla: es la dirección del cálculo.
 *
 * A diferencia de `FamiliaSoldadura`, que se queda en `ui/`: la familia va implícita en la variante
 * de `EntradasFavorito` y ningún caso de uso la recibe.
 */
enum class ModoEntradaSoldadura {
    /** El de los mockups: se introduce el metal que se tiene (oro o plata). */
    DESDE_METAL,

    /** El mínimo de la spec: se introduce el peso final de soldadura deseado. */
    PESO_FINAL,
    ;

    /** Identificador estable para telemetría, independiente del idioma. */
    val analyticsId: String get() = name.lowercase()
}
