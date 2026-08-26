package com.jrblanco.calculadoradejoyeros2021.domain.repository

import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.Favorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import kotlinx.coroutines.flow.Flow

/**
 * Los cálculos que el joyero conserva.
 *
 * `Flow` en la lista y `suspend` en el resto, al contrario que `CotizacionesRepository` y como
 * `PreferenciasRepository`: la pantalla de Favoritos **observa** la lista, y de que el cambio llegue
 * como emisión depende que un favorito borrado desaparezca al instante sin que nadie vuelva a
 * preguntar. Guardar, borrar y obtener son operaciones de un toque, sin nadie escuchando.
 *
 * No hay `actualizar`: un favorito es inmutable por diseño. Editar el cálculo en la calculadora no
 * toca el guardado (FR-022) y guardar la variante crea otro (FR-023).
 */
interface FavoritosRepository {

    /** Emite la lista completa, **más reciente primero**, y cada cambio. */
    val favoritos: Flow<List<Favorito>>

    /** Guarda las entradas. Si ya había un favorito con las mismas, no crea otro (FR-006). */
    suspend fun guardar(entradas: EntradasFavorito): ResultadoGuardado

    /** Borra por id. Un id que ya no existe no es un error. */
    suspend fun borrar(id: Long)

    /** El favorito que se va a reabrir, o `null` si ya no está o no se entiende. */
    suspend fun obtener(id: Long): Favorito?
}
