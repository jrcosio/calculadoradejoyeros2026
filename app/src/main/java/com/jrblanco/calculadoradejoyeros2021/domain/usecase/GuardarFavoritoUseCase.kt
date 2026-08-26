package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import com.jrblanco.calculadoradejoyeros2021.domain.repository.FavoritosRepository

/** Guarda un cálculo. Si ya estaba, lo dice en vez de duplicarlo (FR-006). */
class GuardarFavoritoUseCase(
    private val favoritos: FavoritosRepository,
) {
    suspend operator fun invoke(entradas: EntradasFavorito): ResultadoGuardado =
        favoritos.guardar(entradas)
}
