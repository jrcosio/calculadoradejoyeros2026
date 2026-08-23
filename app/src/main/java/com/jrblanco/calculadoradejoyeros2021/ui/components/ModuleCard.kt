package com.jrblanco.calculadoradejoyeros2021.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryRadius
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing

/**
 * Tarjeta de acceso a un módulo de cálculo.
 *
 * El [accent] tiñe borde, título y chevron: es lo que distingue de un vistazo el oro de
 * la plata y de las utilidades.
 */
@Composable
fun ModuleCard(
    imageRes: Int,
    imageDescription: String,
    title: String,
    description: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(JewelryRadius.Large)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(158.dp)
            .background(
                // Degradado que arranca del acento y se apaga: da el volumen cálido del
                // mockup sin recurrir a sombras duras.
                Brush.horizontalGradient(
                    listOf(
                        accent.copy(alpha = 0.14f),
                        JewelryColors.Surface,
                        JewelryColors.Surface,
                    ),
                ),
                shape,
            )
            .border(1.dp, accent.copy(alpha = 0.65f), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(JewelrySpacing.Md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Fit y no Crop: la imagen de herramientas es apaisada y con Crop se perderían
        // el calibre y la lupa.
        Image(
            painter = painterResource(imageRes),
            contentDescription = imageDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .width(112.dp)
                .height(112.dp),
        )

        Spacer(Modifier.width(JewelrySpacing.Md))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Xs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = accent,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = JewelryColors.TextSecondary,
            )
        }

        Spacer(Modifier.width(JewelrySpacing.Sm))

        Box(
            modifier = Modifier
                .size(44.dp)
                .border(1.dp, accent.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                // La tarjeta entera ya es accionable y se anuncia con su título: repetir
                // aquí solo añadiría ruido al lector de pantalla.
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
