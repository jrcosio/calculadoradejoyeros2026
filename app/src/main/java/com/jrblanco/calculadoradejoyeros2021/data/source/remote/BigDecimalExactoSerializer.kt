package com.jrblanco.calculadoradejoyeros2021.data.source.remote

import java.math.BigDecimal
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive

/**
 * `BigDecimal` construido desde el **literal** que llegó por el cable, sin pasar por `Double`.
 *
 * Es la versión de red de la regla del proyecto «`BigDecimal` desde `String`»: el proveedor
 * manda `-45.30000000000018` como número JSON y así se conserva exacto. Acepta también el
 * valor entre comillas.
 */
object BigDecimalExactoSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimalExacto", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BigDecimal {
        val literal = (decoder as JsonDecoder).decodeJsonElement().jsonPrimitive.content
        return BigDecimal(literal)
    }

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }
}
