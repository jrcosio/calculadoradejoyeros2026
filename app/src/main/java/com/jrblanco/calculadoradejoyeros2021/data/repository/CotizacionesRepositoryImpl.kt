package com.jrblanco.calculadoradejoyeros2021.data.repository

import com.jrblanco.calculadoradejoyeros2021.core.util.Reloj
import com.jrblanco.calculadoradejoyeros2021.data.source.local.CotizacionesLocalDataSource
import com.jrblanco.calculadoradejoyeros2021.data.source.remote.CotizacionesRemoteDataSource
import com.jrblanco.calculadoradejoyeros2021.data.source.remote.MetalSentinelException
import com.jrblanco.calculadoradejoyeros2021.domain.model.DecisionCache
import com.jrblanco.calculadoradejoyeros2021.domain.model.InstantaneaCotizaciones
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.OrigenDatos
import com.jrblanco.calculadoradejoyeros2021.domain.model.PoliticaCacheCotizaciones
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.repository.CotizacionesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Cotizaciones con caché de una hora: lee lo guardado, deja que la [politica] decida y solo
 * consulta al proveedor por los metales sin precio vigente, en paralelo.
 *
 * El [cerrojo] envuelve la operación entera y es el *single-flight*: si dos pantallas piden a
 * la vez, la segunda espera y recibe la instantánea recién guardada sin gastar cuota. Un
 * metal que falla no tumba a los demás (`supervisorScope`), y solo se captura
 * [MetalSentinelException]: cualquier otra excepción es un bug y sube al ViewModel.
 */
class CotizacionesRepositoryImpl(
    private val remoto: CotizacionesRemoteDataSource,
    private val local: CotizacionesLocalDataSource,
    private val reloj: Reloj,
    private val politica: PoliticaCacheCotizaciones = PoliticaCacheCotizaciones(),
) : CotizacionesRepository {

    private val cerrojo = Mutex()
    private var enMemoria: InstantaneaCotizaciones? = null

    override suspend fun obtenerCotizaciones(): InstantaneaCotizaciones = cerrojo.withLock {
        val guardada = enMemoria ?: (local.leer() ?: InstantaneaCotizaciones.VACIA)
        val ahora = reloj.ahoraMillis()

        when (val decision = politica.decidir(guardada, ahora)) {
            DecisionCache.Servir -> guardada.copy(origen = OrigenDatos.CACHE).also { enMemoria = it }
            DecisionCache.Esperar -> guardada.copy(origen = OrigenDatos.CACHE_EN_ESPERA).also { enMemoria = it }
            is DecisionCache.Actualizar -> {
                val nuevos = supervisorScope {
                    decision.pendientes.map { metal -> async { consultar(metal) } }.awaitAll()
                }.associateBy { it.metal }
                val fusionada = guardada
                    .fusionarCon(nuevos, instanteIntentoEpochMillis = ahora)
                    .copy(origen = OrigenDatos.RED)
                enMemoria = fusionada
                local.guardar(fusionada)
                fusionada
            }
        }
    }

    private suspend fun consultar(metal: MetalCotizado): ResultadoCotizacion = try {
        ResultadoCotizacion.Exito(remoto.obtener(metal))
    } catch (e: CancellationException) {
        throw e
    } catch (e: MetalSentinelException) {
        ResultadoCotizacion.Error(metal = metal, motivo = e.motivo, causa = e.cause)
    }
}
