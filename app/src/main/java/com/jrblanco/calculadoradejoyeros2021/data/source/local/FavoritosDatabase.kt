package com.jrblanco.calculadoradejoyeros2021.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * La base de los favoritos del joyero. Una tabla y nada más.
 *
 * `exportSchema` se queda en su valor por defecto (`true`) y `app/schemas/…/1.json` **se commitea**:
 * es el contrato con la versión siguiente, y sin él no se puede escribir una migración verificable
 * el día que haya una versión 2. Ese día se estrena `room-testing` con `MigrationTestHelper`.
 */
@Database(entities = [FavoritoEntity::class], version = 1)
abstract class FavoritosDatabase : RoomDatabase() {
    abstract fun favoritosDao(): FavoritosDao
}
