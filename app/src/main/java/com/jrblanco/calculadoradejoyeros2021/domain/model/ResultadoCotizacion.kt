package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * Lo que la caché guarda de cada metal: su cotización o el motivo por el que no se obtuvo.
 *
 * [Error.ultimaConocida] es lo que permite pintar un precio «desactualizado» junto al
 * motivo cuando el metal falló pero alguna vez se obtuvo. [Error.causa] solo sirve para
 * enviar la excepción inesperada a Crashlytics desde el ViewModel; no se persiste.
 */
sealed interface ResultadoCotizacion {
    val metal: MetalCotizado

    data class Exito(val cotizacion: CotizacionMetal) : ResultadoCotizacion {
        override val metal: MetalCotizado get() = cotizacion.metal
    }

    data class Error(
        override val metal: MetalCotizado,
        val motivo: MotivoErrorCotizacion,
        val ultimaConocida: CotizacionMetal? = null,
        val causa: Throwable? = null,
    ) : ResultadoCotizacion
}
