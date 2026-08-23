package com.jrblanco.calculadoradejoyeros2021.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing

/**
 * Barra superior compartida por toda la app, con dos caras:
 *
 * - **Zonas principales** (Home, Favoritos, Ajustes): logo centrado.
 * - **Secciones de módulo**: flecha de retroceso y nombre de la sección.
 *
 * El acceso a información va siempre a la derecha.
 */
@Composable
fun JewelryTopBar(
    onInfo: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    onBack: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(JewelryColors.Background)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = JewelrySpacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_atras),
                        contentDescription = stringResource(R.string.topbar_atras),
                        tint = JewelryColors.GoldPrimary,
                    )
                }
            } else {
                // Hueco simétrico al del botón de info: sin él, el logo quedaría
                // descentrado hacia la izquierda.
                Box(Modifier.size(48.dp))
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                if (title == null) {
                    Image(
                        painter = painterResource(R.drawable.logo_calculadora),
                        contentDescription = stringResource(R.string.welcome_logo_description),
                        modifier = Modifier.size(52.dp),
                    )
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = JewelryColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            IconButton(onClick = onInfo) {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = stringResource(R.string.topbar_info),
                    tint = JewelryColors.GoldPrimary,
                )
            }
        }

        GoldSeparator(Modifier.align(Alignment.BottomCenter))
    }
}

/** Filete dorado que se desvanece hacia los extremos. */
@Composable
internal fun GoldSeparator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        JewelryColors.GoldSecondary.copy(alpha = 0f),
                        JewelryColors.GoldSecondary.copy(alpha = 0.7f),
                        JewelryColors.GoldSecondary.copy(alpha = 0f),
                    ),
                ),
            ),
    )
}
