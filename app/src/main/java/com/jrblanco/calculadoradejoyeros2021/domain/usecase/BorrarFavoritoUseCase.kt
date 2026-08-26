package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.repository.FavoritosRepository

/** Quita un favorito. Un id que ya no existe no es un error. */
class BorrarFavoritoUseCase(
    private val favoritos: FavoritosRepository,
) {
    suspend operator fun invoke(id: Long) = favoritos.borrar(id)
}
