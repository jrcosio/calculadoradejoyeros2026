package com.jrblanco.calculadoradejoyeros2021.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.theme.CifraGrande
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

/**
 * Una fila de resultado: imagen del metal, nombre, puntos de guía y gramos.
 *
 * Nació privada en la calculadora de oro recibiendo un tipo de su feature; se comparte
 * desde que la de plata pidió la misma fila. Toma solo datos de presentación —ni
 * `MetalLiga`, ni `LeyPlata`, ni nada de `domain/`— y es cada pantalla la que mapea sus
 * enums a ellos.
 */
@Composable
fun FilaMetal(
    imagenRes: Int,
    imagenDescripcion: String,
    nombre: String,
    valorFormateado: String,
    acento: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        // Un solo anuncio por fila para el lector: «Plata fina, 2,191 gr».
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(imagenRes),
            contentDescription = imagenDescripcion,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(44.dp),
        )

        Spacer(Modifier.width(JewelrySpacing.Sm))

        Text(
            text = nombre,
            style = MaterialTheme.typography.bodyLarge,
            color = JewelryColors.TextPrimary,
        )

        LineaPunteada(
            // Apagada para que guíe el ojo sin competir con la cifra.
            color = acento.copy(alpha = 0.55f),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = JewelrySpacing.Sm),
        )

        Text(
            text = valorFormateado,
            style = CifraGrande.copy(fontSize = 26.sp, lineHeight = 32.sp),
            color = acento,
        )

        Spacer(Modifier.width(JewelrySpacing.Xs))

        Text(
            text = stringResource(R.string.unidad_gramos),
            style = MaterialTheme.typography.bodyMedium,
            color = JewelryColors.GoldPrimary,
        )
    }
}

/**
 * Tarjeta de total: balanza, etiqueta de lo que se ha obtenido y peso final de la aleación.
 *
 * Nació privada en la calculadora de oro. [acento] tiñe el icono, su círculo, la cifra y
 * la unidad; la etiqueta se queda siempre en texto primario, que es como está en oro.
 */
@Composable
fun TarjetaTotal(
    etiqueta: String,
    totalFormateado: String,
    modifier: Modifier = Modifier,
    acento: Color = JewelryColors.GoldPrimary,
) {
    TarjetaAcento(modifier, acento) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .border(1.dp, acento.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_balanza),
                    contentDescription = null,
                    tint = acento,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(Modifier.width(JewelrySpacing.Md))

            Text(
                text = etiqueta,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                color = JewelryColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )

            Spacer(Modifier.width(JewelrySpacing.Sm))

            Text(
                text = totalFormateado,
                style = CifraGrande.copy(fontSize = 26.sp, lineHeight = 32.sp),
                color = acento,
            )

            Spacer(Modifier.width(JewelrySpacing.Xs))

            Text(
                text = stringResource(R.string.unidad_gramos),
                style = MaterialTheme.typography.bodyMedium,
                color = acento,
            )
        }
    }
}
