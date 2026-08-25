package com.jrblanco.calculadoradejoyeros2021.data.source.local

import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionMetal
import com.jrblanco.calculadoradejoyeros2021.domain.model.InstantaneaCotizaciones
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MotivoErrorCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.UnidadPrecio
import java.math.BigDecimal
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Traduce la instantánea a su forma persistida y viceversa. Kotlin puro: se prueba en JVM.
 *
 * Tolerante hacia delante: un metal o un motivo que esta versión no conozca se **descarta**
 * en vez de tumbar la lectura; un JSON ilegible devuelve `null` y el que lo guardó lo borra.
 */
class CodificadorInstantanea(
    // encodeDefaults: la versión debe escribirse siempre, aunque sea la 1.
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
) {
    fun codificar(instantanea: InstantaneaCotizaciones): String {
        val dto = InstantaneaPersistidaDto(
            instanteIntentoEpochMillis = instantanea.instanteIntentoEpochMillis,
            resultados = instantanea.resultados.values.map { resultado ->
                when (resultado) {
                    is ResultadoCotizacion.Exito -> ResultadoPersistidoDto(
                        metal = resultado.metal.name,
                        cotizacion = resultado.cotizacion.aDto(),
                    )
                    is ResultadoCotizacion.Error -> ResultadoPersistidoDto(
                        metal = resultado.metal.name,
                        motivoError = resultado.motivo.name,
                        ultimaConocida = resultado.ultimaConocida?.aDto(),
                    )
                }
            },
        )
        return json.encodeToString(InstantaneaPersistidaDto.serializer(), dto)
    }

    fun decodificar(texto: String): InstantaneaCotizaciones? {
        val dto = try {
            json.decodeFromString(InstantaneaPersistidaDto.serializer(), texto)
        } catch (e: SerializationException) {
            return null
        } catch (e: IllegalArgumentException) {
            return null
        }
        val resultados = dto.resultados.mapNotNull { it.aDominio() }.associateBy { it.metal }
        return InstantaneaCotizaciones(
            resultados = resultados,
            instanteIntentoEpochMillis = dto.instanteIntentoEpochMillis,
        )
    }

    private fun ResultadoPersistidoDto.aDominio(): ResultadoCotizacion? {
        val metalDominio = MetalCotizado.entries.firstOrNull { it.name == metal } ?: return null
        cotizacion?.let { return ResultadoCotizacion.Exito(it.aDominio(metalDominio) ?: return null) }
        val motivoDominio = MotivoErrorCotizacion.entries.firstOrNull { it.name == motivoError } ?: return null
        return ResultadoCotizacion.Error(
            metal = metalDominio,
            motivo = motivoDominio,
            ultimaConocida = ultimaConocida?.aDominio(metalDominio),
        )
    }

    private fun CotizacionPersistidaDto.aDominio(metal: MetalCotizado): CotizacionMetal? = try {
        CotizacionMetal(
            metal = metal,
            moneda = moneda,
            ask = BigDecimal(ask),
            bid = BigDecimal(bid),
            mid = BigDecimal(mid),
            maximo = BigDecimal(maximo),
            minimo = BigDecimal(minimo),
            variacion = BigDecimal(variacion),
            variacionPorcentaje = BigDecimal(variacionPorcentaje),
            unidadOrigen = unidadOrigen?.let { nombre -> UnidadPrecio.entries.firstOrNull { it.name == nombre } },
            etiquetaUnidadOrigen = etiquetaUnidadOrigen,
            instanteMercadoEpochMillis = instanteMercadoEpochMillis,
            obtenidoEnEpochMillis = obtenidoEnEpochMillis,
        )
    } catch (e: NumberFormatException) {
        null
    }

    private fun CotizacionMetal.aDto(): CotizacionPersistidaDto = CotizacionPersistidaDto(
        moneda = moneda,
        ask = ask.toPlainString(),
        bid = bid.toPlainString(),
        mid = mid.toPlainString(),
        maximo = maximo.toPlainString(),
        minimo = minimo.toPlainString(),
        variacion = variacion.toPlainString(),
        variacionPorcentaje = variacionPorcentaje.toPlainString(),
        unidadOrigen = unidadOrigen?.name,
        etiquetaUnidadOrigen = etiquetaUnidadOrigen,
        instanteMercadoEpochMillis = instanteMercadoEpochMillis,
        obtenidoEnEpochMillis = obtenidoEnEpochMillis,
    )
}
