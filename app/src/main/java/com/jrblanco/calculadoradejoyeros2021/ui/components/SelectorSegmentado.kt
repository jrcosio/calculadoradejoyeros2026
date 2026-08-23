package com.jrblanco.calculadoradejoyeros2021.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryRadius
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySize
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing

/**
 * Fila de opciones excluyentes: una y solo una activa, marcada con una píldora en el
 * color de acento y un check.
 *
 * Hecho a mano y no con `SegmentedButton` de Material a propósito: ese componente
 * impone su altura, su forma y su marca de selección, y el diseño pide píldora con
 * degradado del acento — el mismo motivo por el que `JewelryBottomBar` y el botón de
 * portada tampoco usan Material.
 */
@Composable
fun SelectorSegmentado(
    opciones: List<String>,
    seleccionada: Int,
    onSeleccion: (Int) -> Unit,
    modifier: Modifier = Modifier,
    acento: Color = JewelryColors.GoldPrimary,
) {
    val marco = RoundedCornerShape(JewelryRadius.Small)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(JewelryColors.Surface, marco)
            .border(1.dp, JewelryColors.Border, marco)
            .padding(3.dp)
            .selectableGroup(),
    ) {
        opciones.forEachIndexed { indice, etiqueta ->
            Segmento(
                etiqueta = etiqueta,
                activa = indice == seleccionada,
                acento = acento,
                onClick = { onSeleccion(indice) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Segmento(
    etiqueta: String,
    activa: Boolean,
    acento: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pildora = RoundedCornerShape(JewelryRadius.Small)
    val fondo = if (activa) {
        Modifier.background(
            // La píldora activa brilla como metal pulido: el acento aclarado arriba y
            // oscurecido abajo, sea el dorado de las leyes o el teal de los colores.
            Brush.verticalGradient(
                listOf(
                    lerp(acento, Color.White, 0.25f),
                    acento,
                    lerp(acento, Color.Black, 0.25f),
                ),
            ),
            pildora,
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .heightIn(min = JewelrySize.MinTouchTarget)
            .then(fondo)
            .selectable(
                selected = activa,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (activa) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(JewelryColors.Background.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = acento,
                        modifier = Modifier.size(11.dp),
                    )
                }
                Spacer(Modifier.width(JewelrySpacing.Sm))
            }
            // Auto-ajustable: con la fuente del sistema al doble, la etiqueta encoge
            // antes que recortarse dentro de la píldora.
            BasicText(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activa) JewelryColors.Background else JewelryColors.TextSecondary,
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 6.sp,
                    maxFontSize = 14.sp,
                ),
            )
        }
    }
}
