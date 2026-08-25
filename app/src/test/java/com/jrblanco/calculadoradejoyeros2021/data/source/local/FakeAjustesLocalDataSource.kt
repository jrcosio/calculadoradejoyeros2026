package com.jrblanco.calculadoradejoyeros2021.data.source.local

import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Ajustes de test en memoria: lo que se guarda es lo que se emite, con contador de escrituras. */
class FakeAjustesLocalDataSource(
    guardado: IdiomaApp? = null,
) : AjustesLocalDataSource {

    private val flujo = MutableStateFlow(guardado)

    var escrituras = 0
        private set

    override val idioma: Flow<IdiomaApp?> = flujo

    override suspend fun guardarIdioma(idioma: IdiomaApp?) {
        escrituras++
        flujo.value = idioma
    }
}
