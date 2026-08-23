package com.jrblanco.calculadoradejoyeros2021.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing

/**
 * Ornamento de marca: línea fina dorada, rombo, línea fina dorada.
 *
 * Nació privado en la portada y lo reclamaron después las tarjetas de la pantalla de
 * información, así que vive aquí en vez de duplicado en dos sitios.
 *
 * [widthFraction] existe porque el ancho útil cambia con el sitio: en la portada respira
 * al 70% del ancho de pantalla, y dentro de una tarjeta tiene que ocupar todo el hueco
 * que le deja el padding.
 */
@Composable
fun DiamondDivider(
    modifier: Modifier = Modifier,
    widthFraction: Float = 0.7f,
) {
    Row(
        modifier = modifier.fillMaxWidth(fraction = widthFraction),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        GoldHairline(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .padding(horizontal = JewelrySpacing.Md)
                .size(8.dp)
                .rotate(45f)
                .background(JewelryColors.GoldPrimary),
        )
        GoldHairline(Modifier.weight(1f))
    }
}

/** Filete dorado de un píxel que nace transparente y se satura hacia el final. */
@Composable
fun GoldHairline(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(JewelryColors.GoldSecondary.copy(alpha = 0f), JewelryColors.GoldSecondary),
                ),
            ),
    )
}
