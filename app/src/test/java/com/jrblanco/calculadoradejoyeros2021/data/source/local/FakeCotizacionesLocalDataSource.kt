package com.jrblanco.calculadoradejoyeros2021.data.source.local

import com.jrblanco.calculadoradejoyeros2021.domain.model.InstantaneaCotizaciones

/** Caché de test en memoria: lo que se guarda es lo que se lee, con contadores. */
class FakeCotizacionesLocalDataSource(
    var guardada: InstantaneaCotizaciones? = null,
) : CotizacionesLocalDataSource {
    var lecturas = 0
        private set
    var escrituras = 0
        private set

    override suspend fun leer(): InstantaneaCotizaciones? {
        lecturas++
        return guardada
    }

    override suspend fun guardar(instantanea: InstantaneaCotizaciones) {
        escrituras++
        guardada = instantanea
    }
}
