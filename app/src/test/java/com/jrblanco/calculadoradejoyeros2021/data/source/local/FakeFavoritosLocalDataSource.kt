package com.jrblanco.calculadoradejoyeros2021.data.source.local

import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.Favorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Favoritos de test en memoria, con la misma regla de identidad que la tabla real: la firma canónica
 * decide si algo ya estaba. Contadores con `private set` y puerta opcional, como el resto de fakes.
 *
 * Las [ilegibles] son filas que el codificador real no entendería: se guardan pero no se emiten, que
 * es lo que hace la implementación de Room con un favorito de una versión más nueva.
 */
class FakeFavoritosLocalDataSource : FavoritosLocalDataSource {

    private val codificador = CodificadorFavorito()
    private val guardados = MutableStateFlow<List<Favorito>>(emptyList())

    /** Cuántas filas hay que existen pero no se entienden. Solo para probar FR-034. */
    var ilegibles = 0

    var siguienteId = 1L
        private set
    var escrituras = 0
        private set
    var borrados = 0
        private set
    var lecturas = 0
        private set

    /** Si se fija, `guardar` se queda esperando hasta que el test la complete. */
    var puerta: CompletableDeferred<Unit>? = null

    override val favoritos: Flow<List<Favorito>> = guardados

    override suspend fun guardar(
        entradas: EntradasFavorito,
        guardadoEnEpochMillis: Long,
    ): ResultadoGuardado {
        escrituras++
        puerta?.await()

        val firma = codificador.codificar(entradas).firma
        val existente = guardados.value.firstOrNull {
            codificador.codificar(it.entradas).firma == firma
        }
        if (existente != null) return ResultadoGuardado.YaExistia(existente.id)

        val nuevo = Favorito(siguienteId++, guardadoEnEpochMillis, entradas)
        // Más reciente primero, como la consulta real.
        guardados.value = listOf(nuevo) + guardados.value
        return ResultadoGuardado.Guardado(nuevo.id)
    }

    override suspend fun borrar(id: Long) {
        borrados++
        guardados.value = guardados.value.filterNot { it.id == id }
    }

    override suspend fun obtener(id: Long): Favorito? {
        lecturas++
        return guardados.value.firstOrNull { it.id == id }
    }

    /** Siembra sin pasar por `guardar`, para preparar un estado inicial. */
    fun sembrar(vararg favoritos: Favorito) {
        guardados.value = favoritos.sortedByDescending { it.id }
        siguienteId = (favoritos.maxOfOrNull { it.id } ?: 0L) + 1
    }
}
