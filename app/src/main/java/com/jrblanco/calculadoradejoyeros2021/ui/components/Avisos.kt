package com.jrblanco.calculadoradejoyeros2021.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryRadius
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing

/**
 * Advertencia de que una ley es solo una composición técnica y no una ley oficial
 * española. Región viva para que el lector de pantalla la anuncie al aparecer.
 *
 * Nació privada en la calculadora de oro con el texto de 500‰ cableado. El texto sale
 * ahora por parámetro porque la calculadora de plata tiene dos avisos distintos, uno para
 * 950‰ y otro para 900‰, y cada uno sitúa su milésima respecto de las oficiales.
 */
@Composable
fun AvisoTecnico(
    texto: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(JewelryRadius.Small)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(JewelryColors.SurfaceWarm, shape)
            .border(1.dp, JewelryColors.Warning.copy(alpha = 0.65f), shape)
            .padding(JewelrySpacing.Md)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_aviso),
            contentDescription = null,
            tint = JewelryColors.Warning,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(JewelrySpacing.Sm))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = JewelryColors.Warning,
        )
    }
}
