package com.jrblanco.calculadoradejoyeros2021.data.source.remote

import com.jrblanco.calculadoradejoyeros2021.domain.model.MotivoErrorCotizacion

/**
 * Fallo al obtener una cotización, ya clasificado con su [motivo] de dominio.
 *
 * El [mensaje] jamás incluye la credencial, la URL con cabeceras ni el cuerpo del proveedor:
 * es texto de diagnóstico interno, no algo que se muestre al joyero.
 */
class MetalSentinelException(
    val motivo: MotivoErrorCotizacion,
    mensaje: String,
    causa: Throwable? = null,
) : Exception(mensaje, causa)
