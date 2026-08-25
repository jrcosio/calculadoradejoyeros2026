package com.jrblanco.calculadoradejoyeros2021.data.source.local

import com.jrblanco.calculadoradejoyeros2021.domain.model.InstantaneaCotizaciones

/**
 * La caché persistida de cotizaciones: lo que sobrevive al cierre de la app durante la hora
 * de vigencia. `null` al leer significa que no hay nada guardado (o que lo guardado no se
 * entiende y se ha descartado).
 */
interface CotizacionesLocalDataSource {
    suspend fun leer(): InstantaneaCotizaciones?

    suspend fun guardar(instantanea: InstantaneaCotizaciones)
}
