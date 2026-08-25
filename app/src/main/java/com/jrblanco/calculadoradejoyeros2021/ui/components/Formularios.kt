package com.jrblanco.calculadoradejoyeros2021.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.theme.CifraGrande
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryRadius
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing

/**
 * Campo de cantidad en gramos, a mano con `BasicTextField`: `OutlinedTextField` impone
 * altura, padding y tipografía de Material y el diseño pide cifra grande centrada en caja
 * redondeada con el sufijo «gr».
 *
 * Nació privado en la calculadora de oro; lo comparten desde que la de plata pidió el
 * mismo campo en plateado. [acento] tiñe el cursor y la unidad, y [borde] el filete de la
 * caja: dorado en oro, plateado en plata.
 */
@Composable
fun CampoCantidad(
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
    acento: Color = JewelryColors.GoldPrimary,
    borde: Color = JewelryColors.BorderGold,
) {
    MarcoCampo(borde = borde, modifier = modifier.heightIn(min = 64.dp)) {
        BasicTextField(
            value = valor,
            onValueChange = onCambio,
            modifier = Modifier.weight(1f),
            textStyle = CifraGrande.copy(
                color = JewelryColors.TextPrimary,
                textAlign = TextAlign.Center,
            ),
            // Teclado decimal: coma y punto valen y el ViewModel los normaliza.
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            cursorBrush = SolidColor(acento),
        )

        Spacer(Modifier.width(JewelrySpacing.Sm))

        Text(
            text = stringResource(R.string.unidad_gramos),
            style = MaterialTheme.typography.titleMedium,
            color = acento,
        )
    }
}

/**
 * Campo de medida con etiqueta, icono y unidad: «Ancho · 10,00 · mm».
 *
 * Nació con la calculadora de chapas, que necesita tres medidas seguidas: tres cifras de 34 sp
 * sin etiqueta —el [CampoCantidad]— pesan demasiado y no dicen cuál es cuál. Comparte el marco
 * con [CampoCantidad] (regla del segundo consumidor). [error] pinta el filete en rojo cuando la
 * medida se sale del rango operativo; [imeAction] encadena los campos con «Siguiente».
 */
@Composable
fun CampoMedida(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    iconRes: Int,
    unidad: String,
    modifier: Modifier = Modifier,
    acento: Color = JewelryColors.GoldPrimary,
    borde: Color = JewelryColors.Border,
    error: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
) {
    MarcoCampo(
        borde = if (error) JewelryColors.Danger else borde,
        modifier = modifier
            .heightIn(min = 56.dp)
            .semantics(mergeDescendants = true) {},
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = acento,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(JewelrySpacing.Md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = JewelryColors.TextMuted,
            )
            BasicTextField(
                value = valor,
                onValueChange = onCambio,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = JewelryColors.TextPrimary,
                ),
                // Teclado decimal: coma y punto valen y el ViewModel los normaliza.
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = imeAction),
                singleLine = true,
                cursorBrush = SolidColor(acento),
            )
        }
        Spacer(Modifier.width(JewelrySpacing.Sm))
        Text(
            text = unidad,
            style = MaterialTheme.typography.titleMedium,
            color = acento,
        )
    }
}

/** La caja redondeada con filete que comparten los campos de la app. */
@Composable
private fun MarcoCampo(
    borde: Color,
    modifier: Modifier = Modifier,
    contenido: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(JewelryRadius.Medium)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(JewelryColors.Background, shape)
            .border(1.dp, borde, shape)
            .padding(horizontal = JewelrySpacing.Md, vertical = JewelrySpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
        content = contenido,
    )
}

/**
 * Cabecera de sección: icono y título, anunciado como encabezado al lector de pantalla.
 *
 * Nació privada en la calculadora de oro. [tinte] es dorado por defecto y plateado en la
 * pantalla de plata.
 */
@Composable
fun CabeceraSeccion(
    iconRes: Int,
    titulo: String,
    modifier: Modifier = Modifier,
    tinte: Color = JewelryColors.GoldPrimary,
) {
    Row(
        modifier = modifier.semantics { heading() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tinte,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(JewelrySpacing.Sm))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            color = JewelryColors.TextPrimary,
        )
    }
}
