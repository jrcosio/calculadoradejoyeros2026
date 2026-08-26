package com.jrblanco.calculadoradejoyeros2021.data.repository

import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.Favorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import com.jrblanco.calculadoradejoyeros2021.domain.repository.FavoritosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Favoritos de test: el flujo se puede empujar a mano y las operaciones se registran, al estilo de
 * [FakePreferenciasRepository].
 */
class FakeFavoritosRepository(
    inicial: List<Favorito> = emptyList(),
) : FavoritosRepository {

    val flujo = MutableStateFlow(inicial)

    var guardados = mutableListOf<EntradasFavorito>()
        private set
    var borrados = mutableListOf<Long>()
        private set

    /** Lo que devolverá el siguiente `guardar`. Por defecto, una fila nueva. */
    var resultadoGuardar: ResultadoGuardado = ResultadoGuardado.Guardado(1L)

    override val favoritos: Flow<List<Favorito>> = flujo

    override suspend fun guardar(entradas: EntradasFavorito): ResultadoGuardado {
        guardados += entradas
        return resultadoGuardar
    }

    override suspend fun borrar(id: Long) {
        borrados += id
        flujo.value = flujo.value.filterNot { it.id == id }
    }

    override suspend fun obtener(id: Long): Favorito? = flujo.value.firstOrNull { it.id == id }
}
