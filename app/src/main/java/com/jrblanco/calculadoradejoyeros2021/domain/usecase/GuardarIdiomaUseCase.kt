package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import com.jrblanco.calculadoradejoyeros2021.domain.repository.PreferenciasRepository

/**
 * Guarda el idioma que el joyero acaba de elegir. `null` es «Automático»: devuelve el control al
 * dispositivo, y también se recuerda.
 */
class GuardarIdiomaUseCase(
    private val preferencias: PreferenciasRepository,
) {
    suspend operator fun invoke(idioma: IdiomaApp?) = preferencias.guardarIdioma(idioma)
}
