package com.jrblanco.calculadoradejoyeros2021.data.source.remote

import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionMetal
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado

/**
 * La fuente remota de cotizaciones, un metal por llamada.
 *
 * @throws MetalSentinelException con el motivo ya clasificado; cualquier otra excepción es un
 *   bug y no se disfraza de error de red.
 */
interface CotizacionesRemoteDataSource {
    suspend fun obtener(metal: MetalCotizado): CotizacionMetal
}
