package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.Favorito
import com.jrblanco.calculadoradejoyeros2021.domain.repository.FavoritosRepository
import kotlinx.coroutines.flow.Flow

/** Los favoritos guardados, más reciente primero, y cada cambio. */
class ObservarFavoritosUseCase(
    private val favoritos: FavoritosRepository,
) {
    operator fun invoke(): Flow<List<Favorito>> = favoritos.favoritos
}
