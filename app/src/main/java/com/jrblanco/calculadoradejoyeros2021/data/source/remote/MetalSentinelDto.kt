package com.jrblanco.calculadoradejoyeros2021.data.source.remote

import java.math.BigDecimal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Un importe del proveedor, parseado exacto desde su literal (ver [BigDecimalExactoSerializer]). */
typealias ImporteExacto = @Serializable(with = BigDecimalExactoSerializer::class) BigDecimal

/**
 * La respuesta de `GET /api/metal-quote` tal y como la publica el proveedor
 * (`specs/007-herramientas/contracts/metal-quote.md`). Solo se declaran los campos que la app
 * usa; `open`, `close`, `originalTime` y `extra` se ignoran con `ignoreUnknownKeys`.
 */
@Serializable
data class RespuestaMetalSentinelDto(
    @SerialName("ID") val id: Long? = null,
    val results: List<CotizacionMetalSentinelDto> = emptyList(),
)

@Serializable
data class CotizacionMetalSentinelDto(
    val symbol: String,
    val currency: String,
    val ask: ImporteExacto,
    val mid: ImporteExacto,
    val bid: ImporteExacto,
    val high: ImporteExacto,
    val low: ImporteExacto,
    val change: ImporteExacto,
    val changePercentage: ImporteExacto,
    /** Segundos Unix según la muestra real; si llegara en milisegundos también se entiende. */
    val timestamp: Long,
    val unit: String,
)
