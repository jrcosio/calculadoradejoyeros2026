package com.jrblanco.calculadoradejoyeros2021.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryRadius
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing

/**
 * Envoltorio de tarjeta con el lenguaje visual de [ModuleCard]: esquina grande,
 * degradado que arranca del acento y se apaga, y filete del acento.
 *
 * Nació privada en la pantalla de información (como `TarjetaDorada`); se comparte
 * desde que la calculadora de oro pidió la misma tarjeta con acento teal — el mismo
 * movimiento que hizo `DiamondDivider` al salir de la portada.
 */
@Composable
fun TarjetaAcento(
    modifier: Modifier = Modifier,
    acento: Color = JewelryColors.GoldPrimary,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(JewelryRadius.Large)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        acento.copy(alpha = 0.14f),
                        JewelryColors.Surface,
                        JewelryColors.Surface,
                    ),
                ),
                shape,
            )
            .border(1.dp, acento.copy(alpha = 0.65f), shape)
            .padding(JewelrySpacing.Md),
        content = contenido,
    )
}
