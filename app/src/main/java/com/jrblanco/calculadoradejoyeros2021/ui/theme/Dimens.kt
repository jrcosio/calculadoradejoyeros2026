package com.jrblanco.calculadoradejoyeros2021.ui.theme

import androidx.compose.ui.unit.dp

/** Radios de esquina del sistema de diseño (design spec, sección 4). */
object JewelryRadius {
    val Small = 12.dp
    val Medium = 18.dp
    val Large = 28.dp
    val ExtraLarge = 34.dp
    val Pill = 999.dp
}

/** Escala de espaciado del sistema de diseño (design spec, sección 4). */
object JewelrySpacing {
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Lg = 16.dp
    val Xl = 24.dp
    val Xxl = 32.dp
}

/**
 * Tamaños táctiles mínimos. La design spec fija 48dp para todo elemento accionable;
 * los botones principales se llevan a 56dp para que respiren.
 */
object JewelrySize {
    val MinTouchTarget = 48.dp
    val PrimaryButtonHeight = 56.dp
    val BottomNavHeight = 88.dp
}
