package com.jrblanco.calculadoradejoyeros2021.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tokens de color del sistema dark luxury.
 *
 * Fuente de verdad: `UI_Plantillas/Calculadora_Joyeros_Design_Spec.md`, sección 2.
 * Van agrupados en un objeto y no como propiedades sueltas para no chocar con los
 * composables de Material 3 que comparten nombre (`Surface`, `Background`).
 *
 * Los roles de Material se consumen por `MaterialTheme.colorScheme`; este objeto se
 * usa directamente solo para lo que la paleta de Material no sabe expresar: el oro,
 * la plata y los niveles de texto de la marca.
 */
object JewelryColors {

    // Base
    val Background = Color(0xFF071018)
    val Surface = Color(0xFF101921)
    val SurfaceElevated = Color(0xFF17212A)
    val SurfaceWarm = Color(0xFF241A0E)

    // Oro: valor, metal noble, acción principal
    val GoldPrimary = Color(0xFFF4BD45)
    val GoldSecondary = Color(0xFFC98B17)
    val GoldSoft = Color(0xFFE8C36B)

    // Plata: herramientas, metal neutro
    val SilverPrimary = Color(0xFFC7CDD2)
    val SilverDark = Color(0xFF707980)

    // Turquesa: acento de utilidades
    val TealPrimary = Color(0xFF14B8B8)
    val TealDark = Color(0xFF087D82)

    // Texto
    val TextPrimary = Color(0xFFF7F7F5)
    val TextSecondary = Color(0xFFB8BEC3)
    val TextMuted = Color(0xFF808990)

    // Bordes y estados
    val Border = Color(0xFF5D6870)
    val BorderGold = Color(0xFFD9A22D)
    val BorderTeal = Color(0xFF198F91)
    val Success = Color(0xFF48B68A)
    val Warning = Color(0xFFF4BD45)
    val Danger = Color(0xFFE45454)

    /**
     * Azul del `fondo_taller`, más claro que [Background].
     *
     * Lo usa el fondo de ventana y el splash del sistema para que el arranque
     * encadene con la portada sin cambio de tono.
     */
    val SplashBackground = Color(0xFF0C1A2B)
}
