package com.jrblanco.calculadoradejoyeros2021.data.source.remote

import com.jrblanco.calculadoradejoyeros2021.core.util.Reloj
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionMetal
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MotivoErrorCotizacion
import kotlinx.coroutines.CompletableDeferred

/**
 * Fuente remota de test: cotizaciones o fallos programados por metal, contador de llamadas y
 * una puerta opcional para simular concurrencia. Si recibe un [reloj], estampa `obtenidoEn`
 * con la hora del reloj al responder, como hace el data source real.
 */
class FakeCotizacionesRemoteDataSource(
    private val reloj: Reloj? = null,
) : CotizacionesRemoteDataSource {
    val cotizaciones = mutableMapOf<MetalCotizado, CotizacionMetal>()
    val fallos = mutableMapOf<MetalCotizado, Throwable>()
    val llamadas = mutableMapOf<MetalCotizado, Int>()
    var puerta: CompletableDeferred<Unit>? = null

    val totalLlamadas: Int get() = llamadas.values.sum()

    fun programarTodos() {
        MetalCotizado.entries.forEach { cotizaciones[it] = CotizacionesDePrueba.cotizacion(metal = it) }
    }

    override suspend fun obtener(metal: MetalCotizado): CotizacionMetal {
        llamadas[metal] = (llamadas[metal] ?: 0) + 1
        puerta?.await()
        fallos[metal]?.let { throw it }
        val base = cotizaciones[metal]
            ?: throw MetalSentinelException(MotivoErrorCotizacion.DESCONOCIDO, "Sin cotización programada para $metal")
        return reloj?.let { base.copy(obtenidoEnEpochMillis = it.ahoraMillis()) } ?: base
    }
}
