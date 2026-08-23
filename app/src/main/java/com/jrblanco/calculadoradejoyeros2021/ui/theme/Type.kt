package com.jrblanco.calculadoradejoyeros2021.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jrblanco.calculadoradejoyeros2021.R

/**
 * Tipografía del producto.
 *
 * Se empaquetan TTF **estáticos** y no la fuente variable que publica el repo de
 * Google Fonts: `FontVariation` requiere API 26 y el `minSdk` del proyecto es 24, con
 * lo que en Android 7 los pesos se ignorarían y los títulos saldrían en regular.
 */
val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
)

/**
 * Serif de alto contraste, reservado a la portada.
 *
 * La design spec pide Manrope y evitar fuentes decorativas; el mockup de la pantalla
 * de inicio usa un serif. Se resuelve por ámbito: esta familia es la excepción de
 * portada, Manrope manda en el resto de la app.
 */
val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_bold, FontWeight.Bold),
)

/** Escala tipográfica de la design spec, sección 3. */
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

/**
 * Estilo del título de portada. Fuera de [Typography] porque no es un rol de
 * Material: es una pieza de marca de una sola pantalla.
 */
val TitleSerif = TextStyle(
    fontFamily = PlayfairDisplay,
    fontWeight = FontWeight.Bold,
    fontSize = 44.sp,
    lineHeight = 48.sp,
)
