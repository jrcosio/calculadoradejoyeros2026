package com.jrblanco.calculadoradejoyeros2021.data.source.local

import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.Favorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import kotlinx.coroutines.flow.Flow

/**
 * Los favoritos guardados en el dispositivo. Habla tipos de dominio, como
 * [CotizacionesLocalDataSource]: la traducción a filas y el descarte de lo que no se entiende son
 * asunto suyo.
 *
 * La hora la recibe por parámetro y no la mira: quien decide **cuándo** se guardó es el repositorio,
 * con el `Reloj` inyectado.
 */
interface FavoritosLocalDataSource {

    val favoritos: Flow<List<Favorito>>

    suspend fun guardar(entradas: EntradasFavorito, guardadoEnEpochMillis: Long): ResultadoGuardado

    suspend fun borrar(id: Long)

    suspend fun obtener(id: Long): Favorito?
}
