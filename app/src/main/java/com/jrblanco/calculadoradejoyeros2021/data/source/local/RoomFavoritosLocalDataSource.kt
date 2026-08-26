package com.jrblanco.calculadoradejoyeros2021.data.source.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.Favorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Los favoritos en Room: una tabla, un índice único y nada más.
 *
 * **La base se crea `by lazy` aquí dentro y no se registra en Koin**, igual que el `DataStore` de
 * ajustes: `verify()` solo inspecciona constructores del tipo primario, y una base nacida de
 * `Room.databaseBuilder` habría que meterla en `extraTypes`, debilitando el test para todo el
 * proyecto. El `single` de Koin garantiza la instancia única por fichero que Room exige.
 *
 * **Journal en TRUNCATE y no WAL**: así el favorito vive en un único `favoritos.db` autocontenido,
 * sin `-wal` que una restauración pueda dejar a medias — y los favoritos sí entran en la copia de
 * seguridad (FR-033). Se escribe una vez por pulsación de botón: el rendimiento de WAL no compra
 * nada aquí.
 *
 * **Nunca `fallbackToDestructiveMigration`**: esto son datos del joyero, no una caché.
 *
 * **Sin `DispatcherProvider`, a propósito y por única vez en `data/`**: los métodos `suspend` de un
 * DAO y los `Flow` de Room ya se ejecutan en el executor de Room y son seguros desde el hilo
 * principal. Un `withContext(dispatchers.io)` encima sería un salto de hilo de adorno, y pasar el
 * dispatcher inyectado como `setQueryExecutor` es el camino corto al bloqueo cuando ese dispatcher
 * es de un solo hilo y hay una transacción por medio.
 *
 * Contrato completo en `specs/009-favoritos/contracts/favoritos-persistidos.md`.
 */
class RoomFavoritosLocalDataSource(
    private val context: Context,
) : FavoritosLocalDataSource {

    private val codificador = CodificadorFavorito()

    private val baseDatos: FavoritosDatabase by lazy {
        Room.databaseBuilder(context, FavoritosDatabase::class.java, FICHERO)
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .build()
    }

    private val dao: FavoritosDao by lazy { baseDatos.favoritosDao() }

    /**
     * Lo que esta versión no entiende se **descarta de la lista** pero no se borra de la base: puede
     * venir de una versión más nueva y le espera intacto si el joyero vuelve a ella (FR-034). Al
     * contrario que la caché de cotizaciones, que sí descarta lo que no entiende: aquello era dato
     * derivado y esto es una decisión del joyero.
     */
    override val favoritos: Flow<List<Favorito>>
        get() = dao.observar().map { filas -> filas.mapNotNull(::aDominio) }

    override suspend fun guardar(
        entradas: EntradasFavorito,
        guardadoEnEpochMillis: Long,
    ): ResultadoGuardado {
        val persistido = codificador.codificar(entradas)
        val fila = FavoritoEntity(
            tipo = persistido.tipo,
            firma = persistido.firma,
            datosJson = persistido.datosJson,
            guardadoEnEpochMillis = guardadoEnEpochMillis,
        )

        return baseDatos.withTransaction {
            val insertado = dao.insertar(fila)
            if (insertado != FILA_NO_INSERTADA) {
                ResultadoGuardado.Guardado(insertado)
            } else {
                // −1 dice *que* existía, no *cuál*: esta es la única consulta extra del diseño, y
                // solo en la rama del duplicado. Dentro de la transacción no puede haber
                // desaparecido. (`INSERT … RETURNING` lo haría en un viaje, pero pide API 34.)
                val existente = checkNotNull(dao.idPorFirma(fila.firma)) {
                    "El índice único rechazó la fila pero su firma no está en la tabla"
                }
                ResultadoGuardado.YaExistia(existente)
            }
        }
    }

    override suspend fun borrar(id: Long) {
        dao.borrar(id)
    }

    override suspend fun obtener(id: Long): Favorito? = dao.porId(id)?.let(::aDominio)

    private fun aDominio(fila: FavoritoEntity): Favorito? =
        codificador.decodificar(fila.tipo, fila.datosJson)?.let { entradas ->
            Favorito(
                id = fila.id,
                guardadoEnEpochMillis = fila.guardadoEnEpochMillis,
                entradas = entradas,
            )
        }

    companion object {
        const val FICHERO = "favoritos.db"
        private const val FILA_NO_INSERTADA = -1L
    }
}
