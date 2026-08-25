package com.jrblanco.calculadoradejoyeros2021.data.source.local

import kotlinx.serialization.Serializable

/**
 * Forma persistida de la instantánea de cotizaciones: DTOs propios, independientes de los del
 * proveedor, con los importes como `String` (`toPlainString()`) y los enums por nombre.
 * `origen` y la causa de los errores no se persisten. [version] permite migrar en el futuro.
 */
@Serializable
data class InstantaneaPersistidaDto(
    val version: Int = 1,
    val instanteIntentoEpochMillis: Long? = null,
    val resultados: List<ResultadoPersistidoDto> = emptyList(),
)

@Serializable
data class ResultadoPersistidoDto(
    val metal: String,
    val cotizacion: CotizacionPersistidaDto? = null,
    val motivoError: String? = null,
    val ultimaConocida: CotizacionPersistidaDto? = null,
)

@Serializable
data class CotizacionPersistidaDto(
    val moneda: String,
    val ask: String,
    val bid: String,
    val mid: String,
    val maximo: String,
    val minimo: String,
    val variacion: String,
    val variacionPorcentaje: String,
    val unidadOrigen: String? = null,
    val etiquetaUnidadOrigen: String,
    val instanteMercadoEpochMillis: Long,
    val obtenidoEnEpochMillis: Long,
)
