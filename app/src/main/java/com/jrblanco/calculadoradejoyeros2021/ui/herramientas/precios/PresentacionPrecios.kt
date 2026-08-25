package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.precios

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MotivoErrorCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.Tendencia
import com.jrblanco.calculadoradejoyeros2021.domain.model.UnidadPrecio
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import java.util.Date

/**
 * Mapeos de presentación de la sub-herramienta de precios, internos al paquete. `domain/` no
 * conoce Android: es la pantalla la que traduce cada enum a texto, imagen o color.
 */
internal val MetalCotizado.nombreRes: Int
    get() = when (this) {
        MetalCotizado.ORO -> R.string.metal_oro
        MetalCotizado.PLATA -> R.string.metal_plata
        MetalCotizado.COBRE -> R.string.metal_cobre
        MetalCotizado.PALADIO -> R.string.metal_paladio
        MetalCotizado.RODIO -> R.string.metal_rodio
    }

internal val MetalCotizado.imagenRes: Int
    get() = when (this) {
        MetalCotizado.ORO -> R.drawable.modulo_oro
        MetalCotizado.PLATA -> R.drawable.modulo_plata
        MetalCotizado.COBRE -> R.drawable.cobre
        MetalCotizado.PALADIO -> R.drawable.paladio
        MetalCotizado.RODIO -> R.drawable.rodio
    }

internal val MetalCotizado.imagenDescripcionRes: Int
    get() = when (this) {
        MetalCotizado.ORO -> R.string.metal_oro_imagen
        MetalCotizado.PLATA -> R.string.metal_plata_imagen
        MetalCotizado.COBRE -> R.string.metal_cobre_imagen
        MetalCotizado.PALADIO -> R.string.metal_paladio_imagen
        MetalCotizado.RODIO -> R.string.metal_rodio_imagen
    }

/** Nombre de la unidad para el selector. */
internal val UnidadPrecio.etiquetaRes: Int
    get() = when (this) {
        UnidadPrecio.GRAMO -> R.string.precios_unidad_gramo
        UnidadPrecio.KILO -> R.string.precios_unidad_kilo
        UnidadPrecio.ONZA_TROY -> R.string.precios_unidad_onza
        UnidadPrecio.LIBRA -> R.string.precios_unidad_libra
    }

/** Símbolo de la unidad junto a la cifra: «€/g», «€/kg», «€/oz». */
internal val UnidadPrecio.simboloRes: Int
    get() = when (this) {
        UnidadPrecio.GRAMO -> R.string.unidad_euro_gramo
        UnidadPrecio.KILO -> R.string.unidad_euro_kilo
        UnidadPrecio.ONZA_TROY -> R.string.unidad_euro_onza
        UnidadPrecio.LIBRA -> R.string.unidad_euro_libra
    }

internal val MotivoErrorCotizacion.mensajeRes: Int
    get() = when (this) {
        MotivoErrorCotizacion.SIN_CREDENCIAL -> R.string.precios_error_sin_credencial
        MotivoErrorCotizacion.CREDENCIAL_RECHAZADA -> R.string.precios_error_credencial
        MotivoErrorCotizacion.NO_ENCONTRADO -> R.string.precios_error_no_encontrado
        MotivoErrorCotizacion.LIMITE_ALCANZADO -> R.string.precios_error_limite
        MotivoErrorCotizacion.SERVIDOR -> R.string.precios_error_servidor
        MotivoErrorCotizacion.SIN_CONEXION -> R.string.precios_error_sin_conexion
        MotivoErrorCotizacion.RESPUESTA_INVALIDA -> R.string.precios_error_respuesta
        MotivoErrorCotizacion.DESCONOCIDO -> R.string.precios_error_desconocido
    }

internal val Tendencia.color: Color
    get() = when (this) {
        Tendencia.SUBE -> JewelryColors.Success
        Tendencia.BAJA -> JewelryColors.Danger
        Tendencia.PLANA -> JewelryColors.TextMuted
    }

internal val Tendencia.descripcionRes: Int
    get() = when (this) {
        Tendencia.SUBE -> R.string.precios_tendencia_sube
        Tendencia.BAJA -> R.string.precios_tendencia_baja
        Tendencia.PLANA -> R.string.precios_tendencia_plana
    }

/** Giro del chevron (que apunta a la derecha) para que señale hacia donde va el metal. */
internal val Tendencia.rotacionFlecha: Float
    get() = when (this) {
        Tendencia.SUBE -> -90f
        Tendencia.BAJA -> 90f
        Tendencia.PLANA -> 0f
    }

/**
 * Fecha y hora locales de un instante, en el idioma elegido en Ajustes («25 ago 2026 · 10:33» en
 * español, «Aug 25, 2026 · 10:33 AM» en inglés). Es el único texto de la pantalla que no formatea
 * el ViewModel: el nombre del mes depende del idioma y `domain`/ViewModel no conocen recursos
 * (FR-030).
 *
 * Los dos formateadores salen del **contexto**, que `ui/idioma/ProveedorIdioma` entrega ya
 * localizado (feature 008), y por eso siguen al idioma de la app y no al del dispositivo.
 * `DateUtils.formatDateTime` no servía: toma el orden de la fecha de `Locale.getDefault()`, que es
 * el del sistema, así que con la app en inglés y el móvil en portugués mostraba «25/08/2026».
 * `getMediumDateFormat` usa el locale de la configuración del contexto, y `getTimeFormat` además
 * respeta el ajuste de 12/24 horas del dispositivo. Las dos son API 3, como el resto del fichero.
 */
@Composable
internal fun fechaHoraLocal(epochMillis: Long): String {
    val contexto = LocalContext.current
    val instante = Date(epochMillis)
    val fecha = DateFormat.getMediumDateFormat(contexto).format(instante)
    val hora = DateFormat.getTimeFormat(contexto).format(instante)
    return "$fecha · $hora"
}
