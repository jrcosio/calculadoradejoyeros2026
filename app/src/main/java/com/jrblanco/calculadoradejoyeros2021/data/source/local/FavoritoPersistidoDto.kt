package com.jrblanco.calculadoradejoyeros2021.data.source.local

import kotlinx.serialization.Serializable

/**
 * Forma persistida de las entradas de un favorito: un DTO plano con los campos de las siete
 * variantes, todos nulables, los decimales como `String` **ya canónico** y los enums por nombre.
 * Mismo patrón que [InstantaneaPersistidaDto], que también reúne dos formas en un solo DTO nulable.
 *
 * El discriminador **no** está aquí: vive en la columna `tipo`, que es la que se consulta y la que
 * le dice al decodificador qué variante construir. Una sola fuente para el tipo.
 *
 * [version] permite migrar el **contenido** sin migrar el esquema de SQLite. No confundir con la
 * versión que lleva la firma, que gobierna la **identidad**: se pueden mover por separado.
 */
@Serializable
data class FavoritoPersistidoDto(
    val version: Int = 1,
    /** La única cifra de las seis variantes de metal: masa de origen o cantidad. */
    val cantidad: String? = null,
    val ley: String? = null,
    val color: String? = null,
    val dureza: String? = null,
    val tipoSoldadura: String? = null,
    val modo: String? = null,
    val material: String? = null,
    val ancho: String? = null,
    val largo: String? = null,
    val espesor: String? = null,
)
