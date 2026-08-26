package com.jrblanco.calculadoradejoyeros2021.data.source.local

import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata
import com.jrblanco.calculadoradejoyeros2021.domain.model.MaterialChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.ModoEntradaSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraClasica
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraPlata
import java.math.BigDecimal
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Lo que va a las tres columnas de contenido de una fila. */
data class FavoritoPersistido(
    val tipo: String,
    val firma: String,
    val datosJson: String,
)

/**
 * Traduce las entradas de un favorito a su forma persistida y vuelta. Kotlin puro: se prueba en JVM
 * y no sabe que existe Room.
 *
 * La **firma** es la identidad del favorito: dos entradas que un joyero consideraría iguales dan la
 * misma firma, y el índice único de la tabla hace el resto. Se construye con un `when` explícito
 * sobre las entradas de dominio y **nunca** desde el DTO ni desde el texto JSON: si colgara del
 * JSON, reordenar un campo del DTO cambiaría la forma del texto y el deduplicado dejaría de
 * funcionar **en silencio**.
 *
 * Cinco reglas de canonización, y son contrato (`specs/009-favoritos/contracts/favoritos-persistidos.md`):
 *
 *  1. Las cantidades llegan ya normalizadas por `parsearDecimalPositivo`: aquí no hay comas.
 *  2. Decimales con `stripTrailingZeros().toPlainString()`: `30`, `30.0`, `030` y `3e1` dan `30`.
 *  3. Enums por `name`, **nunca** por `analyticsId` — que es otro contrato y además colisiona:
 *     `LeyOro.LEY_12K` y `MaterialChapa.ORO_12K` valen los dos `"12k"`.
 *  4. Orden de campos escrito a mano. Sin reflexión y sin `::class.simpleName`: R8 ofusca los
 *     nombres de clase y en release ningún favorito se leería.
 *  5. Separadores `|` y `=`, imposibles en un `name` de enum y en un decimal canónico: sin escapes,
 *     sin hash, y legible al depurar el `.db`.
 */
class CodificadorFavorito(
    // encodeDefaults: la versión debe escribirse siempre, aunque sea la 1.
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
) {

    fun codificar(entradas: EntradasFavorito): FavoritoPersistido = FavoritoPersistido(
        tipo = entradas.analyticsId,
        firma = firmaDe(entradas),
        datosJson = json.encodeToString(FavoritoPersistidoDto.serializer(), dtoDe(entradas)),
    )

    /**
     * `null` cuando esta versión no entiende la fila: tipo desconocido, enum desconocido, decimal
     * ilegible, JSON roto o una cantidad que no pasa el `require` de la variante. El llamante la
     * descarta de la lista **sin borrarla**.
     */
    fun decodificar(tipo: String, datosJson: String): EntradasFavorito? {
        val dto = try {
            json.decodeFromString(FavoritoPersistidoDto.serializer(), datosJson)
        } catch (e: SerializationException) {
            return null
        } catch (e: IllegalArgumentException) {
            return null
        }

        return try {
            when (tipo) {
                TIPO_ORO -> EntradasFavorito.Oro(
                    masaOrigen = dto.cantidad.aDecimal() ?: return null,
                    color = ColorOro.entries.porNombre(dto.color) ?: return null,
                    ley = LeyOro.entries.porNombre(dto.ley) ?: return null,
                )

                TIPO_PLATA -> EntradasFavorito.Plata(
                    masaOrigen = dto.cantidad.aDecimal() ?: return null,
                    ley = LeyPlata.entries.porNombre(dto.ley) ?: return null,
                )

                TIPO_SOLDADURA_LEY -> EntradasFavorito.SoldaduraLey(
                    cantidad = dto.cantidad.aDecimal() ?: return null,
                    dureza = DurezaSoldaduraLey.entries.porNombre(dto.dureza) ?: return null,
                    color = ColorOroSoldadura.entries.porNombre(dto.color) ?: return null,
                    modo = ModoEntradaSoldadura.entries.porNombre(dto.modo) ?: return null,
                )

                TIPO_SOLDADURA_CLASICA -> EntradasFavorito.SoldaduraClasica(
                    cantidad = dto.cantidad.aDecimal() ?: return null,
                    tipo = TipoSoldaduraClasica.entries.porNombre(dto.tipoSoldadura) ?: return null,
                    modo = ModoEntradaSoldadura.entries.porNombre(dto.modo) ?: return null,
                )

                TIPO_SOLDADURA_PLATA -> EntradasFavorito.SoldaduraPlata(
                    cantidad = dto.cantidad.aDecimal() ?: return null,
                    tipo = TipoSoldaduraPlata.entries.porNombre(dto.tipoSoldadura) ?: return null,
                    modo = ModoEntradaSoldadura.entries.porNombre(dto.modo) ?: return null,
                )

                TIPO_SOLDADURA_BASE -> EntradasFavorito.SoldaduraBase(
                    cantidad = dto.cantidad.aDecimal() ?: return null,
                    modo = ModoEntradaSoldadura.entries.porNombre(dto.modo) ?: return null,
                )

                TIPO_CHAPA -> EntradasFavorito.Chapa(
                    ancho = dto.ancho.aDecimal() ?: return null,
                    largo = dto.largo.aDecimal() ?: return null,
                    espesor = dto.espesor.aDecimal() ?: return null,
                    material = MaterialChapa.entries.porNombre(dto.material) ?: return null,
                )

                // Un tipo que esta versión no conoce: viene de una versión más nueva.
                else -> null
            }
        } catch (e: IllegalArgumentException) {
            // El `require` de la variante: una cantidad ≤ 0 guardada por una versión con otras reglas.
            null
        }
    }

    // --- La firma, campo a campo y en el orden de los parámetros de cada motor ---

    private fun firmaDe(entradas: EntradasFavorito): String = when (entradas) {
        is EntradasFavorito.Oro -> firma(
            TIPO_ORO,
            "masa" to entradas.masaOrigen.canonico(),
            "color" to entradas.color.name,
            "ley" to entradas.ley.name,
        )

        is EntradasFavorito.Plata -> firma(
            TIPO_PLATA,
            "masa" to entradas.masaOrigen.canonico(),
            "ley" to entradas.ley.name,
        )

        is EntradasFavorito.SoldaduraLey -> firma(
            TIPO_SOLDADURA_LEY,
            "cant" to entradas.cantidad.canonico(),
            "dureza" to entradas.dureza.name,
            "color" to entradas.color.name,
            "modo" to entradas.modo.name,
        )

        is EntradasFavorito.SoldaduraClasica -> firma(
            TIPO_SOLDADURA_CLASICA,
            "cant" to entradas.cantidad.canonico(),
            "tipo" to entradas.tipo.name,
            "modo" to entradas.modo.name,
        )

        is EntradasFavorito.SoldaduraPlata -> firma(
            TIPO_SOLDADURA_PLATA,
            "cant" to entradas.cantidad.canonico(),
            "tipo" to entradas.tipo.name,
            "modo" to entradas.modo.name,
        )

        is EntradasFavorito.SoldaduraBase -> firma(
            TIPO_SOLDADURA_BASE,
            "cant" to entradas.cantidad.canonico(),
            "modo" to entradas.modo.name,
        )

        is EntradasFavorito.Chapa -> firma(
            TIPO_CHAPA,
            "ancho" to entradas.ancho.canonico(),
            "largo" to entradas.largo.canonico(),
            "espesor" to entradas.espesor.canonico(),
            "material" to entradas.material.name,
        )
    }

    private fun dtoDe(entradas: EntradasFavorito): FavoritoPersistidoDto = when (entradas) {
        is EntradasFavorito.Oro -> FavoritoPersistidoDto(
            cantidad = entradas.masaOrigen.canonico(),
            color = entradas.color.name,
            ley = entradas.ley.name,
        )

        is EntradasFavorito.Plata -> FavoritoPersistidoDto(
            cantidad = entradas.masaOrigen.canonico(),
            ley = entradas.ley.name,
        )

        is EntradasFavorito.SoldaduraLey -> FavoritoPersistidoDto(
            cantidad = entradas.cantidad.canonico(),
            dureza = entradas.dureza.name,
            color = entradas.color.name,
            modo = entradas.modo.name,
        )

        is EntradasFavorito.SoldaduraClasica -> FavoritoPersistidoDto(
            cantidad = entradas.cantidad.canonico(),
            tipoSoldadura = entradas.tipo.name,
            modo = entradas.modo.name,
        )

        is EntradasFavorito.SoldaduraPlata -> FavoritoPersistidoDto(
            cantidad = entradas.cantidad.canonico(),
            tipoSoldadura = entradas.tipo.name,
            modo = entradas.modo.name,
        )

        is EntradasFavorito.SoldaduraBase -> FavoritoPersistidoDto(
            cantidad = entradas.cantidad.canonico(),
            modo = entradas.modo.name,
        )

        is EntradasFavorito.Chapa -> FavoritoPersistidoDto(
            material = entradas.material.name,
            ancho = entradas.ancho.canonico(),
            largo = entradas.largo.canonico(),
            espesor = entradas.espesor.canonico(),
        )
    }

    private fun firma(tipo: String, vararg campos: Pair<String, String>): String =
        (listOf(tipo, VERSION_FIRMA) + campos.map { (clave, valor) -> "$clave=$valor" })
            .joinToString(SEPARADOR)

    /** Regla 2: una representación única por valor numérico. */
    private fun BigDecimal.canonico(): String = stripTrailingZeros().toPlainString()

    private fun String?.aDecimal(): BigDecimal? =
        this?.let { texto -> runCatching { BigDecimal(texto) }.getOrNull() }
            ?.takeIf { it > BigDecimal.ZERO }

    /** Regla 3, y nunca `enumValueOf`, que lanza en vez de devolver `null`. */
    private fun <T : Enum<T>> Iterable<T>.porNombre(nombre: String?): T? =
        nombre?.let { buscado -> firstOrNull { it.name == buscado } }

    companion object {
        const val TIPO_ORO = "oro"
        const val TIPO_PLATA = "plata"
        const val TIPO_SOLDADURA_LEY = "soldadura_ley"
        const val TIPO_SOLDADURA_CLASICA = "soldadura_clasica"
        const val TIPO_SOLDADURA_PLATA = "soldadura_plata"
        const val TIPO_SOLDADURA_BASE = "soldadura_base"
        const val TIPO_CHAPA = "chapa"

        /**
         * Versión de la **identidad**, no del contenido. Se sube por tipo cuando una variante gana
         * o pierde un campo: las filas `v1` conviven, se deduplican entre ellas y no colisionan con
         * las `v2`.
         */
        const val VERSION_FIRMA = "v1"
        private const val SEPARADOR = "|"
    }
}
