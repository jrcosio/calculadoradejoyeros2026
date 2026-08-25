package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.InstantaneaCotizaciones
import com.jrblanco.calculadoradejoyeros2021.domain.repository.CotizacionesRepository

/** Obtiene la instantánea de cotizaciones; la caché y la red son asunto del repositorio. */
class ObtenerCotizacionesUseCase(
    private val repositorio: CotizacionesRepository,
) {
    suspend operator fun invoke(): InstantaneaCotizaciones = repositorio.obtenerCotizaciones()
}
