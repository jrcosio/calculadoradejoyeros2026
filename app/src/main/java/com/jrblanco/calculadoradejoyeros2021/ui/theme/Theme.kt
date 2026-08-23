package com.jrblanco.calculadoradejoyeros2021.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Esquema fijo y oscuro.
 *
 * No hay variante clara ni color dinámico a propósito: el color dinámico de
 * Android 12+ repintaría la marca con el fondo de pantalla del usuario y se llevaría
 * por delante la identidad dorada, que es justo lo que distingue al producto de una
 * calculadora genérica.
 */
private val JewelryColorScheme = darkColorScheme(
    primary = JewelryColors.GoldPrimary,
    onPrimary = JewelryColors.Background,
    primaryContainer = JewelryColors.SurfaceWarm,
    onPrimaryContainer = JewelryColors.GoldSoft,

    secondary = JewelryColors.SilverPrimary,
    onSecondary = JewelryColors.Background,
    secondaryContainer = JewelryColors.SurfaceElevated,
    onSecondaryContainer = JewelryColors.SilverPrimary,

    tertiary = JewelryColors.TealPrimary,
    onTertiary = JewelryColors.Background,
    tertiaryContainer = JewelryColors.TealDark,
    onTertiaryContainer = JewelryColors.TextPrimary,

    background = JewelryColors.Background,
    onBackground = JewelryColors.TextPrimary,
    surface = JewelryColors.Surface,
    onSurface = JewelryColors.TextPrimary,
    surfaceVariant = JewelryColors.SurfaceElevated,
    onSurfaceVariant = JewelryColors.TextSecondary,

    outline = JewelryColors.Border,
    outlineVariant = JewelryColors.SilverDark,

    error = JewelryColors.Danger,
    onError = JewelryColors.TextPrimary,
)

@Composable
fun Calculadoradejoyeros2021Theme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = JewelryColorScheme,
        typography = Typography,
        content = content,
    )
}
