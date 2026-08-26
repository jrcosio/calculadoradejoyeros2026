package com.jrblanco.calculadoradejoyeros2021.ui.components

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.util.Date

/**
 * Fechas y horas locales, en el idioma elegido en Ajustes.
 *
 * Son la **única excepción** a «el ViewModel formatea todo»: el nombre del mes depende del idioma y
 * ni `domain/` ni un ViewModel conocen recursos, así que los instantes viajan como `Long` en el
 * `UiState` y se formatean aquí.
 *
 * Los formateadores salen del **contexto**, que `ui/idioma/ProveedorIdioma` entrega ya localizado
 * (feature 008), y por eso siguen al idioma de la app y no al del dispositivo.
 * `DateUtils.formatDateTime` no sirve: toma el orden de la fecha de `Locale.getDefault()`, que es el
 * del sistema, así que con la app en inglés y el móvil en portugués mostraba «25/08/2026».
 * `getMediumDateFormat` usa el locale de la configuración del contexto, y `getTimeFormat` además
 * respeta el ajuste de 12/24 horas del dispositivo. Las dos son API 3, y `java.time` es API 26+ con
 * `minSdk 24`.
 *
 * Nacieron en `ui/herramientas/precios/PresentacionPrecios.kt` y subieron aquí con la feature 009,
 * cuando el listado de favoritos pidió la fecha sin la hora: la regla del segundo consumidor.
 */
@Composable
internal fun fechaLocal(epochMillis: Long): String =
    DateFormat.getMediumDateFormat(LocalContext.current).format(Date(epochMillis))

@Composable
internal fun horaLocal(epochMillis: Long): String =
    DateFormat.getTimeFormat(LocalContext.current).format(Date(epochMillis))

/** «25 ago 2026 · 10:33» en español, «Aug 25, 2026 · 10:33 AM» en inglés. */
@Composable
internal fun fechaHoraLocal(epochMillis: Long): String =
    "${fechaLocal(epochMillis)} · ${horaLocal(epochMillis)}"
