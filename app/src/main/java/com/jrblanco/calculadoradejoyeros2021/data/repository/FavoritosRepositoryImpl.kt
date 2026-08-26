package com.jrblanco.calculadoradejoyeros2021.data.repository

import com.jrblanco.calculadoradejoyeros2021.core.util.Reloj
import com.jrblanco.calculadoradejoyeros2021.data.source.local.FavoritosLocalDataSource
import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.Favorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import com.jrblanco.calculadoradejoyeros2021.domain.repository.FavoritosRepository
import kotlinx.coroutines.flow.Flow

/**
 * Pasarela hacia los favoritos guardados. Lo único que decide es **cuándo** se guardó: el [reloj]
 * vive aquí y no en la interfaz, para que ni la pantalla ni el caso de uso puedan mentir con la
 * hora. Mismo sitio que en `CotizacionesRepositoryImpl`.
 */
class FavoritosRepositoryImpl(
    private val local: FavoritosLocalDataSource,
    private val reloj: Reloj,
) : FavoritosRepository {

    override val favoritos: Flow<List<Favorito>> = local.favoritos

    override suspend fun guardar(entradas: EntradasFavorito): ResultadoGuardado =
        local.guardar(entradas, reloj.ahoraMillis())

    override suspend fun borrar(id: Long) = local.borrar(id)

    override suspend fun obtener(id: Long): Favorito? = local.obtener(id)
}
