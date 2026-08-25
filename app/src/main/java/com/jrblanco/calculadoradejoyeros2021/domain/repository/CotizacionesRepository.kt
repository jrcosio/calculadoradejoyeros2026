package com.jrblanco.calculadoradejoyeros2021.domain.repository

import com.jrblanco.calculadoradejoyeros2021.domain.model.InstantaneaCotizaciones

/**
 * Las cotizaciones de los cinco metales vistas desde el dominio.
 *
 * `suspend` y no `Flow`: hay un solo consumidor que lee una vez al entrar, sin actualizaciones
 * en segundo plano. Sin parámetro «forzar»: la política de caché decide cuándo se toca la red,
 * y «Reintentar» solo vuelve a pasar por ella.
 */
interface CotizacionesRepository {
    suspend fun obtenerCotizaciones(): InstantaneaCotizaciones
}
