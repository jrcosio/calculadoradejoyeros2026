package com.jrblanco.calculadoradejoyeros2021.data.repository

import com.jrblanco.calculadoradejoyeros2021.domain.model.InstantaneaCotizaciones
import com.jrblanco.calculadoradejoyeros2021.domain.repository.CotizacionesRepository
import kotlinx.coroutines.CompletableDeferred

/** Repositorio de test: respuesta programable, contador de llamadas y puerta opcional. */
class FakeCotizacionesRepository(
    var respuesta: InstantaneaCotizaciones = InstantaneaCotizaciones.VACIA,
) : CotizacionesRepository {
    var llamadas = 0
        private set
    var excepcion: Throwable? = null

    /** Si se fija, la llamada se queda esperando hasta que el test la complete. */
    var puerta: CompletableDeferred<Unit>? = null

    override suspend fun obtenerCotizaciones(): InstantaneaCotizaciones {
        llamadas++
        puerta?.await()
        excepcion?.let { throw it }
        return respuesta
    }
}
