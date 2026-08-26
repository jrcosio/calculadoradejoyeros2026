package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.precios

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MotivoErrorCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.Tendencia
import com.jrblanco.calculadoradejoyeros2021.domain.model.UnidadPrecio
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors

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

// La fecha y la hora localizadas viven en `ui/components/Fechas.kt` desde la feature 009: el
// listado de favoritos pidió la fecha sin la hora, y con dos consumidores dejan de ser de aquí.
