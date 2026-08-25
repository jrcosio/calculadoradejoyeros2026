package com.jrblanco.calculadoradejoyeros2021.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
 * Una opción del selector: su etiqueta y el color con el que se pinta al estar activa.
 *
 * El acento va por opción y no por fila porque hay selectores donde cada valor tiene
 * su propio color —el color del oro se elige en el tono de ese oro— y otros donde
 * todas comparten el dorado, que es el valor por defecto.
 *
 * [peso] reparte el ancho de la fila: con el valor por defecto todos los segmentos son
 * iguales; una etiqueta claramente más larga que sus vecinas —«Muy floja (18K)» junto a
 * «Floja» y «Fuerte» en soldaduras— puede pedir más sitio sin forzar el auto-ajuste.
 *
 * [iconRes] pinta un icono delante de la etiqueta, en el hueco que ocuparía el check: el
 * selector de sub-herramientas del mockup lleva uno por opción. Con icono no hay check —la
 * píldora y la semántica de selección ya cuentan el estado—; sin él (el valor por defecto)
 * oro, plata y soldaduras no cambian ni un píxel.
 */
data class OpcionSegmento(
    val etiqueta: String,
    val acento: Color = JewelryColors.GoldPrimary,
    val peso: Float = 1f,
    val iconRes: Int? = null,
)

/**
 * Fila de opciones excluyentes: una y solo una activa, marcada con una píldora en el
 * color de acento y un check.
 *
 * Hecho a mano y no con `SegmentedButton` de Material a propósito: ese componente
 * impone su altura, su forma y su marca de selección, y el diseño pide píldora con
 * degradado del acento — el mismo motivo por el que `JewelryBottomBar` y el botón de
 * portada tampoco usan Material.
 *
 * [maxPorFila] parte las opciones en varias filas cuando no caben legibles en una: las
 * cinco durezas de la calculadora de soldaduras (§5.4 de su documento técnico) forzarían
 * al auto-ajuste hacia lo ilegible. Con el valor por defecto todo va en una fila, el
 * camino de siempre: oro y plata no cambian ni un píxel. Los índices de [onSeleccion]
 * son siempre globales, mire donde mire la opción.
 */
@Composable
fun SelectorSegmentado(
    opciones: List<OpcionSegmento>,
    seleccionada: Int,
    onSeleccion: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxPorFila: Int = Int.MAX_VALUE,
) {
    val marco = RoundedCornerShape(JewelryRadius.Small)
    val porFila = maxPorFila.coerceAtLeast(1)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(JewelryColors.Surface, marco)
            .border(1.dp, JewelryColors.Border, marco)
            .padding(3.dp)
            // El grupo de selección es el conjunto entero, tenga una fila o varias.
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        opciones.chunked(porFila).forEachIndexed { fila, opcionesFila ->
            Row(Modifier.fillMaxWidth()) {
                opcionesFila.forEachIndexed { columna, opcion ->
                    val indice = fila * porFila + columna
                    Segmento(
                        etiqueta = opcion.etiqueta,
                        activa = indice == seleccionada,
                        acento = opcion.acento,
                        iconRes = opcion.iconRes,
                        onClick = { onSeleccion(indice) },
                        modifier = Modifier.weight(opcion.peso),
                    )
                }
            }
        }
    }
}

@Composable
private fun Segmento(
    etiqueta: String,
    activa: Boolean,
    acento: Color,
    iconRes: Int?,
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
            if (iconRes != null) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = if (activa) JewelryColors.Background else JewelryColors.TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(JewelrySpacing.Sm))
            } else if (activa) {
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
