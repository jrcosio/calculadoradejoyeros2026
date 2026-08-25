package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/**
 * Fixtures de cotizaciones para los tests: la muestra real del proveedor (oro, 2026-08-25)
 * como valores por defecto, con todo parametrizable.
 */
object CotizacionesDePrueba {
    const val INSTANTE_MERCADO_MUESTRA = 1_787_665_680_000L

    fun cotizacion(
        metal: MetalCotizado = MetalCotizado.ORO,
        mid: String = "4606.4",
        ask: String = "4607.4",
        bid: String = "4605.4",
        maximo: String = "4697.5",
        minimo: String = "4604.6",
        variacion: String = "-45.30000000000018",
        variacionPorcentaje: String = "-0.974046917668312",
        unidadOrigen: UnidadPrecio? = UnidadPrecio.ONZA_TROY,
        etiquetaUnidadOrigen: String = "OUNCE",
        instanteMercado: Long = INSTANTE_MERCADO_MUESTRA,
        obtenidoEn: Long = 0L,
        moneda: String = "EUR",
    ): CotizacionMetal = CotizacionMetal(
        metal = metal,
        moneda = moneda,
        ask = BigDecimal(ask),
        bid = BigDecimal(bid),
        mid = BigDecimal(mid),
        maximo = BigDecimal(maximo),
        minimo = BigDecimal(minimo),
        variacion = BigDecimal(variacion),
        variacionPorcentaje = BigDecimal(variacionPorcentaje),
        unidadOrigen = unidadOrigen,
        etiquetaUnidadOrigen = etiquetaUnidadOrigen,
        instanteMercadoEpochMillis = instanteMercado,
        obtenidoEnEpochMillis = obtenidoEn,
    )

    fun exito(metal: MetalCotizado, obtenidoEn: Long = 0L): ResultadoCotizacion.Exito =
        ResultadoCotizacion.Exito(cotizacion(metal = metal, obtenidoEn = obtenidoEn))

    fun error(
        metal: MetalCotizado,
        motivo: MotivoErrorCotizacion = MotivoErrorCotizacion.SIN_CONEXION,
        ultimaConocida: CotizacionMetal? = null,
    ): ResultadoCotizacion.Error = ResultadoCotizacion.Error(metal, motivo, ultimaConocida)

    /** Los cinco metales con éxito obtenido en [obtenidoEn]. */
    fun instantaneaCompleta(
        obtenidoEn: Long,
        instanteIntento: Long = obtenidoEn,
    ): InstantaneaCotizaciones = InstantaneaCotizaciones(
        resultados = MetalCotizado.entries.associateWith { exito(it, obtenidoEn) },
        instanteIntentoEpochMillis = instanteIntento,
    )

    /** Cuatro éxitos y [fallido] en error, todo del mismo intento. */
    fun instantaneaParcial(
        obtenidoEn: Long,
        fallido: MetalCotizado = MetalCotizado.RODIO,
        motivo: MotivoErrorCotizacion = MotivoErrorCotizacion.SIN_CONEXION,
        instanteIntento: Long = obtenidoEn,
    ): InstantaneaCotizaciones = InstantaneaCotizaciones(
        resultados = MetalCotizado.entries.associateWith { metal ->
            if (metal == fallido) error(metal, motivo) else exito(metal, obtenidoEn)
        },
        instanteIntentoEpochMillis = instanteIntento,
    )
}
