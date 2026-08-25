package com.jrblanco.calculadoradejoyeros2021.data.source.local

import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import kotlinx.coroutines.flow.Flow

/**
 * Los ajustes persistidos del joyero: lo que sobrevive al cierre de la app y viaja con la copia de
 * seguridad del dispositivo. `null` significa que no hay elección de idioma, es decir «Automático».
 */
interface AjustesLocalDataSource {
    val idioma: Flow<IdiomaApp?>

    suspend fun guardarIdioma(idioma: IdiomaApp?)
}
