package com.jrblanco.calculadoradejoyeros2021.data.source.remote

import com.jrblanco.calculadoradejoyeros2021.BuildConfig
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.core.util.Reloj
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionMetal
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MotivoErrorCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.UnidadPrecio
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Único punto del proyecto que habla con Metal Sentinel (RapidAPI).
 *
 * Contrato en `specs/007-herramientas/contracts/metal-quote.md`. [PARAMETRO_METAL] es una sola
 * constante, confirmada con la credencial real: la documentación pública del proveedor se
 * contradice (`metal` frente a `symbol`) y probar las dos variantes en cada carga gastaría cuota.
 *
 * La [credencial] llega por defecto de `BuildConfig` (extraíble del APK: integración de
 * prototipo); vacía, se responde «servicio no configurado» sin tocar la red. Nunca se registra.
 */
class MetalSentinelDataSource(
    private val cliente: ClienteHttp,
    private val dispatchers: DispatcherProvider,
    private val reloj: Reloj,
    private val credencial: String = BuildConfig.RAPIDAPI_KEY,
) : CotizacionesRemoteDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun obtener(metal: MetalCotizado): CotizacionMetal {
        if (credencial.isBlank()) {
            throw MetalSentinelException(MotivoErrorCotizacion.SIN_CREDENCIAL, "Credencial del proveedor no configurada")
        }

        val url = "$URL_BASE?$PARAMETRO_METAL=${metal.simboloApi}&currency=$MONEDA"
        val respuesta = try {
            withContext(dispatchers.io) { cliente.get(url, cabeceras()) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            throw MetalSentinelException(MotivoErrorCotizacion.SIN_CONEXION, "No se ha podido conectar con el proveedor", e)
        }

        if (respuesta.codigo >= 400) {
            throw MetalSentinelException(motivoDe(respuesta.codigo), "El proveedor respondió ${respuesta.codigo}")
        }

        return try {
            interpretar(respuesta.cuerpo, metal)
        } catch (e: MetalSentinelException) {
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: SerializationException) {
            throw MetalSentinelException(MotivoErrorCotizacion.RESPUESTA_INVALIDA, "Respuesta ilegible del proveedor", e)
        } catch (e: IllegalArgumentException) {
            // Incluye NumberFormatException: un importe que BigDecimal no entiende.
            throw MetalSentinelException(MotivoErrorCotizacion.RESPUESTA_INVALIDA, "Respuesta con valores no válidos", e)
        } catch (e: Exception) {
            throw MetalSentinelException(MotivoErrorCotizacion.DESCONOCIDO, "Fallo inesperado interpretando la respuesta", e)
        }
    }

    private fun interpretar(cuerpo: String, metal: MetalCotizado): CotizacionMetal {
        val dto = json.decodeFromString<RespuestaMetalSentinelDto>(cuerpo)
        val cotizacion = dto.results.firstOrNull { it.symbol.equals(metal.simboloApi, ignoreCase = true) }
            ?: throw MetalSentinelException(
                MotivoErrorCotizacion.RESPUESTA_INVALIDA,
                "La respuesta no trae el metal ${metal.simboloApi}",
            )
        if (!cotizacion.currency.equals(MONEDA, ignoreCase = true)) {
            throw MetalSentinelException(
                MotivoErrorCotizacion.RESPUESTA_INVALIDA,
                "Moneda inesperada: ${cotizacion.currency}",
            )
        }
        return CotizacionMetal(
            metal = metal,
            moneda = cotizacion.currency.uppercase(),
            ask = cotizacion.ask,
            bid = cotizacion.bid,
            mid = cotizacion.mid,
            maximo = cotizacion.high,
            minimo = cotizacion.low,
            variacion = cotizacion.change,
            variacionPorcentaje = cotizacion.changePercentage,
            unidadOrigen = mapearUnidad(cotizacion.unit),
            etiquetaUnidadOrigen = cotizacion.unit,
            instanteMercadoEpochMillis = aMillis(cotizacion.timestamp),
            obtenidoEnEpochMillis = reloj.ahoraMillis(),
        )
    }

    private fun cabeceras(): Map<String, String> = mapOf(
        "x-rapidapi-host" to HOST,
        "x-rapidapi-key" to credencial,
        "Accept" to "application/json",
    )

    private fun motivoDe(codigo: Int): MotivoErrorCotizacion = when (codigo) {
        401, 403 -> MotivoErrorCotizacion.CREDENCIAL_RECHAZADA
        404 -> MotivoErrorCotizacion.NO_ENCONTRADO
        429 -> MotivoErrorCotizacion.LIMITE_ALCANZADO
        in 500..599 -> MotivoErrorCotizacion.SERVIDOR
        else -> MotivoErrorCotizacion.DESCONOCIDO
    }

    /** Solo la onza troy está verificada (`OUNCE`); lo demás se anticipa y lo desconocido queda sin convertir. */
    private fun mapearUnidad(unidad: String): UnidadPrecio? =
        when (unidad.trim().uppercase().replace(' ', '_').replace('-', '_')) {
            "OUNCE", "OUNCES", "OZ", "OZT", "OZ_T", "TROY_OUNCE", "TROY_OUNCES" -> UnidadPrecio.ONZA_TROY
            "GRAM", "GRAMS", "G" -> UnidadPrecio.GRAMO
            "KILOGRAM", "KILOGRAMS", "KG" -> UnidadPrecio.KILO
            else -> null
        }

    /** El proveedor manda segundos; si algún día mandara milisegundos, no se multiplica. */
    private fun aMillis(timestamp: Long): Long =
        if (timestamp > UMBRAL_MILISEGUNDOS) timestamp else timestamp * 1_000L

    companion object {
        const val URL_BASE = "https://metal-sentinel.p.rapidapi.com/api/metal-quote"
        const val HOST = "metal-sentinel.p.rapidapi.com"

        /** Nombre del parámetro del metal. Ver contrato: se confirma con la credencial real (T001). */
        const val PARAMETRO_METAL = "metal"
        const val MONEDA = "EUR"

        private const val UMBRAL_MILISEGUNDOS = 1_000_000_000_000L
    }
}
