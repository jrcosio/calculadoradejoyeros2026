package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * El conjunto de resultados de los cinco metales más el instante del último intento de red.
 * Es lo que se conserva durante una hora y sobrevive al cierre de la app.
 *
 * Una sola verdad por dato: el instante de éxito de cada metal vive en su cotización
 * (`obtenidoEnEpochMillis`) y el instante del intento es único y global; el «último éxito»
 * no se almacena, se deriva. [origen] lo pone el repositorio al devolverla y no se persiste.
 */
data class InstantaneaCotizaciones(
    val resultados: Map<MetalCotizado, ResultadoCotizacion> = emptyMap(),
    val instanteIntentoEpochMillis: Long? = null,
    val origen: OrigenDatos = OrigenDatos.CACHE,
) {
    /** Los cinco metales con cotización. */
    val estaCompleta: Boolean
        get() = MetalCotizado.entries.all { resultados[it] is ResultadoCotizacion.Exito }

    val hayErrores: Boolean
        get() = resultados.values.any { it is ResultadoCotizacion.Error }

    /** Algún metal falló porque el proveedor agotó la cuota: la espera de reintento es mayor. */
    val hayErrorPorLimite: Boolean
        get() = resultados.values.any {
            it is ResultadoCotizacion.Error && it.motivo == MotivoErrorCotizacion.LIMITE_ALCANZADO
        }

    /** El `obtenidoEn` más reciente de los éxitos: la hora de la última consulta con dato. */
    val instanteMasRecienteEpochMillis: Long?
        get() = resultados.values
            .filterIsInstance<ResultadoCotizacion.Exito>()
            .maxOfOrNull { it.cotizacion.obtenidoEnEpochMillis }

    /**
     * Un metal está vigente si tiene cotización obtenida hace menos de [vigenciaMillis].
     * Un dato «del futuro» (reloj del dispositivo atrasado) **no** se considera vigente.
     */
    fun esVigente(metal: MetalCotizado, ahoraMillis: Long, vigenciaMillis: Long): Boolean {
        val exito = resultados[metal] as? ResultadoCotizacion.Exito ?: return false
        val transcurrido = ahoraMillis - exito.cotizacion.obtenidoEnEpochMillis
        return transcurrido >= 0 && transcurrido < vigenciaMillis
    }

    /** La cotización de un metal, aunque sea la antigua que acompaña a un error. */
    fun ultimaCotizacionConocida(metal: MetalCotizado): CotizacionMetal? =
        when (val resultado = resultados[metal]) {
            is ResultadoCotizacion.Exito -> resultado.cotizacion
            is ResultadoCotizacion.Error -> resultado.ultimaConocida
            null -> null
        }

    /**
     * Incorpora los resultados de una consulta. Un éxito sustituye al anterior; un error
     * hereda como `ultimaConocida` la cotización que hubiera antes, si no trae una propia.
     */
    fun fusionarCon(
        nuevos: Map<MetalCotizado, ResultadoCotizacion>,
        instanteIntentoEpochMillis: Long,
    ): InstantaneaCotizaciones {
        val fusionados = resultados.toMutableMap()
        nuevos.forEach { (metal, nuevo) ->
            fusionados[metal] = when (nuevo) {
                is ResultadoCotizacion.Exito -> nuevo
                is ResultadoCotizacion.Error ->
                    if (nuevo.ultimaConocida == null) {
                        nuevo.copy(ultimaConocida = ultimaCotizacionConocida(metal))
                    } else {
                        nuevo
                    }
            }
        }
        return copy(resultados = fusionados, instanteIntentoEpochMillis = instanteIntentoEpochMillis)
    }

    companion object {
        val VACIA = InstantaneaCotizaciones()
    }
}
