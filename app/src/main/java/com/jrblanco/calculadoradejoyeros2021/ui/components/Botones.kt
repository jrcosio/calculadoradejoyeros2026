package com.jrblanco.calculadoradejoyeros2021.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryRadius
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySize
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing

/**
 * Botón dorado a mano, como el de la portada: `Button` de Material impone un contenedor
 * opaco y su propia geometría.
 *
 * Nació privado en la calculadora de oro y lo comparte ahora la de plata. Va en dorado en
 * las dos sin parametrizar el color: el dorado es el lenguaje de acción principal de la
 * app, no el acento de un módulo.
 */
@Composable
fun BotonDorado(
    iconRes: Int,
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(JewelryRadius.Medium)
    Row(
        modifier = modifier
            .heightIn(min = JewelrySize.PrimaryButtonHeight)
            .background(
                Brush.verticalGradient(
                    listOf(
                        JewelryColors.GoldSoft,
                        JewelryColors.GoldPrimary,
                        JewelryColors.GoldSecondary,
                    ),
                ),
                shape,
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = JewelrySpacing.Sm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = JewelryColors.Background,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(JewelrySpacing.Sm))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
            color = JewelryColors.Background,
            textAlign = TextAlign.Center,
        )
    }
}
