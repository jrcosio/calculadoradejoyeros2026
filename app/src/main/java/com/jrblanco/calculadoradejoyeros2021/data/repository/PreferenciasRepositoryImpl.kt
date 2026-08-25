package com.jrblanco.calculadoradejoyeros2021.data.repository

import com.jrblanco.calculadoradejoyeros2021.data.source.local.AjustesLocalDataSource
import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import com.jrblanco.calculadoradejoyeros2021.domain.repository.PreferenciasRepository
import kotlinx.coroutines.flow.Flow

/**
 * Pasarela hacia los ajustes persistidos. No decide nada: la regla de precedencia vive en
 * `SeleccionIdioma` y la tolerancia a lo ilegible, en el data source.
 */
class PreferenciasRepositoryImpl(
    private val local: AjustesLocalDataSource,
) : PreferenciasRepository {

    override val idioma: Flow<IdiomaApp?> = local.idioma

    override suspend fun guardarIdioma(idioma: IdiomaApp?) = local.guardarIdioma(idioma)
}
