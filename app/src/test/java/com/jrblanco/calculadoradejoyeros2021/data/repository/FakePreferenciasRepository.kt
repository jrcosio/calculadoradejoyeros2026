package com.jrblanco.calculadoradejoyeros2021.data.repository

import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import com.jrblanco.calculadoradejoyeros2021.domain.repository.PreferenciasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Preferencias de test: el flujo se puede empujar a mano y las escrituras se cuentan. */
class FakePreferenciasRepository(
    inicial: IdiomaApp? = null,
) : PreferenciasRepository {

    val flujo = MutableStateFlow(inicial)

    var guardados = mutableListOf<IdiomaApp?>()
        private set

    override val idioma: Flow<IdiomaApp?> = flujo

    override suspend fun guardarIdioma(idioma: IdiomaApp?) {
        guardados += idioma
        flujo.value = idioma
    }
}
