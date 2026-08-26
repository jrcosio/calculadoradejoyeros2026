package com.jrblanco.calculadoradejoyeros2021.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a la tabla de favoritos. Todos los métodos son **abstractos** a propósito: la lógica de
 * «inserta o dime cuál ya existía» vive en [RoomFavoritosLocalDataSource] con `withTransaction`, y
 * no como un `@Transaction` con cuerpo en la interfaz — los `DefaultImpls` de una interfaz Kotlin
 * con `suspend` son terreno resbaladizo para el procesador de Room, y la transacción sobre la
 * instancia de la base es igual de exclusiva y se lee mejor.
 */
@Dao
interface FavoritosDao {

    /**
     * Orden por `id DESC` y **no por fecha**: el id es autoincremental y por tanto monótono, así que
     * es el orden real de inserción y sobrevive a que el joyero cambie la hora del móvil o a dos
     * guardados en el mismo milisegundo (FR-012). La fecha se guarda para mostrarla, no para ordenar.
     */
    @Query("SELECT * FROM favoritos ORDER BY id DESC")
    fun observar(): Flow<List<FavoritoEntity>>

    @Query("SELECT * FROM favoritos WHERE id = :id")
    suspend fun porId(id: Long): FavoritoEntity?

    @Query("SELECT id FROM favoritos WHERE firma = :firma")
    suspend fun idPorFirma(firma: String): Long?

    /** Devuelve el id nuevo, o −1 si el índice único de `firma` ya tenía esa fila. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(entidad: FavoritoEntity): Long

    /** Filas borradas: 0 si ese id ya no estaba, que no es un error. */
    @Query("DELETE FROM favoritos WHERE id = :id")
    suspend fun borrar(id: Long): Int
}
