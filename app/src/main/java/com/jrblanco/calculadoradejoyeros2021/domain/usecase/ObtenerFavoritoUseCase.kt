package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.Favorito
import com.jrblanco.calculadoradejoyeros2021.domain.repository.FavoritosRepository

/** El favorito que se va a reabrir, o `null` si ya no está o no se entiende. */
class ObtenerFavoritoUseCase(
    private val favoritos: FavoritosRepository,
) {
    suspend operator fun invoke(id: Long): Favorito? = favoritos.obtener(id)
}
