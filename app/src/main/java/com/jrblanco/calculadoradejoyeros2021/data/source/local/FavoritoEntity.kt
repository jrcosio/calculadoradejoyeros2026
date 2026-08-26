package com.jrblanco.calculadoradejoyeros2021.data.source.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un favorito en SQLite. Tabla única con las entradas en JSON, y no una columna por entrada: las
 * siete formas de favorito no comparten campos, y así el **esquema no cambia** cuando una
 * calculadora gane una entrada nueva — no habría migración, sólo una firma nueva.
 *
 * [firma] es la identidad del favorito y lleva el índice único: es lo que hace que guardar dos veces
 * lo mismo sea idempotente sin una consulta previa ni un antirrebote en la pantalla.
 *
 * **No se indexa [datosJson]**: el texto exacto que produce `kotlinx.serialization` depende del
 * orden de declaración del DTO y de `encodeDefaults`, así que reordenar un campo dejaría de detectar
 * duplicados en silencio. La firma la escribe un `when` a mano en [CodificadorFavorito].
 *
 * Sin índice sobre [guardadoEnEpochMillis]: no se consulta por fecha, y el orden lo da el id.
 */
@Entity(
    tableName = "favoritos",
    indices = [Index(value = ["firma"], unique = true)],
)
data class FavoritoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,
    val firma: String,
    val datosJson: String,
    val guardadoEnEpochMillis: Long,
)
