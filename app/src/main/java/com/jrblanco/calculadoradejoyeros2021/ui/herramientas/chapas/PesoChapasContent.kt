package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.FamiliaChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.MaterialChapa
import com.jrblanco.calculadoradejoyeros2021.ui.components.AvisoTecnico
import com.jrblanco.calculadoradejoyeros2021.ui.components.BotonDorado
import com.jrblanco.calculadoradejoyeros2021.ui.components.CabeceraSeccion
import com.jrblanco.calculadoradejoyeros2021.ui.components.CampoMedida
import com.jrblanco.calculadoradejoyeros2021.ui.components.DiamondDivider
import com.jrblanco.calculadoradejoyeros2021.ui.components.LineaPunteada
import com.jrblanco.calculadoradejoyeros2021.ui.components.OpcionSegmento
import com.jrblanco.calculadoradejoyeros2021.ui.components.SelectorSegmentado
import com.jrblanco.calculadoradejoyeros2021.ui.components.TarjetaAcento
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import com.jrblanco.calculadoradejoyeros2021.ui.theme.CifraGrande
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryRadius
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing

/**
 * La sección de chapas sin estado, en el orden del mockup: ilustración, material, medidas,
 * resultado y botones. **Sin** scaffold, scroll ni padding exterior: los pone el armazón.
 * El acento es el de la familia: dorado en oro, turquesa en plata.
 */
@Composable
fun PesoChapasContent(
    uiState: PesoChapasUiState,
    onFamiliaSeleccionada: (FamiliaChapa) -> Unit,
    onMaterialSeleccionado: (MaterialChapa) -> Unit,
    onMedidaCambiada: (MedidaChapa, String) -> Unit,
    onLimpiar: () -> Unit,
    onGuardarFavoritos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val familia = uiState.material.familia
    val acento = familia.acento

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Md),
    ) {
        TarjetaIlustracion(uiState = uiState, acento = acento)

        CabeceraSeccion(
            iconRes = R.drawable.ic_lingotes,
            titulo = stringResource(R.string.chapas_seccion_material),
            tinte = acento,
        )
        SelectorSegmentado(
            opciones = FamiliaChapa.entries.map { OpcionSegmento(stringResource(it.etiquetaRes), it.acento) },
            seleccionada = familia.ordinal,
            onSeleccion = { onFamiliaSeleccionada(FamiliaChapa.entries[it]) },
        )
        val leyes = MaterialChapa.deFamilia(familia)
        SelectorSegmentado(
            opciones = leyes.map { OpcionSegmento(stringResource(it.etiquetaRes), acento) },
            seleccionada = leyes.indexOf(uiState.material),
            onSeleccion = { onMaterialSeleccionado(leyes[it]) },
        )
        uiState.material.avisoRes?.let { AvisoTecnico(stringResource(it)) }

        CabeceraSeccion(
            iconRes = R.drawable.ic_regla,
            titulo = stringResource(R.string.chapas_seccion_medidas),
            tinte = acento,
        )
        TarjetaAcento(acento = acento) {
            MedidaChapa.entries.forEachIndexed { indice, medida ->
                if (indice > 0) Spacer(Modifier.height(JewelrySpacing.Sm))
                CampoMedida(
                    etiqueta = stringResource(medida.etiquetaRes),
                    valor = uiState.medidas[medida].orEmpty(),
                    onCambio = { texto -> onMedidaCambiada(medida, texto) },
                    iconRes = medida.iconRes,
                    unidad = stringResource(R.string.unidad_milimetros),
                    acento = acento,
                    error = medida in uiState.fueraDeRango,
                    imeAction = if (medida == MedidaChapa.entries.last()) ImeAction.Done else ImeAction.Next,
                )
            }
        }
        if (uiState.fueraDeRango.isNotEmpty()) {
            AvisoTecnico(stringResource(R.string.chapas_aviso_rango))
        }

        uiState.resultado?.let { resultado ->
            TarjetaResultadoChapa(resultado = resultado, material = uiState.material, acento = acento)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = null,
                    tint = JewelryColors.TextMuted,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(JewelrySpacing.Sm))
                Text(
                    text = stringResource(R.string.chapas_nota_aproximado),
                    style = MaterialTheme.typography.bodySmall,
                    color = JewelryColors.TextMuted,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(JewelrySpacing.Md)) {
            BotonDorado(
                iconRes = R.drawable.ic_refrescar,
                texto = stringResource(R.string.accion_limpiar),
                onClick = onLimpiar,
                modifier = Modifier.weight(1f),
            )
            BotonDorado(
                iconRes = R.drawable.ic_estrella,
                texto = stringResource(R.string.accion_guardar_favoritos),
                onClick = onGuardarFavoritos,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Título, subtítulo y la ilustración de la chapa, que se redibuja con las medidas. */
@Composable
private fun TarjetaIlustracion(uiState: PesoChapasUiState, acento: Color) {
    TarjetaAcento(acento = acento) {
        Text(
            text = stringResource(R.string.chapas_titulo),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            color = acento,
        )
        Text(
            text = stringResource(R.string.chapas_subtitulo),
            style = MaterialTheme.typography.bodySmall,
            color = JewelryColors.TextMuted,
        )
        Spacer(Modifier.height(JewelrySpacing.Sm))
        DibujoChapa(
            estado = uiState.dibujo,
            familia = uiState.material.familia,
            descripcion = descripcionDibujo(uiState),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.4f),
        )
    }
}

/** Lo que anuncia el lector de pantalla al llegar a la ilustración: material y tres medidas. */
@Composable
private fun descripcionDibujo(uiState: PesoChapasUiState): String {
    val sinMedida = stringResource(R.string.chapas_dibujo_sin_medida)
    val ancho = uiState.dibujo.etiquetaAncho?.let { stringResource(R.string.chapas_dibujo_medida, it) } ?: sinMedida
    val largo = uiState.dibujo.etiquetaLargo?.let { stringResource(R.string.chapas_dibujo_medida, it) } ?: sinMedida
    val espesor = uiState.dibujo.etiquetaEspesor?.let { stringResource(R.string.chapas_dibujo_medida, it) } ?: sinMedida
    val material = stringResource(uiState.material.familia.nombreMaterialRes, stringResource(uiState.material.etiquetaRes))
    return stringResource(R.string.chapas_dibujo_descripcion, material, ancho, largo, espesor)
}

@Composable
private fun TarjetaResultadoChapa(
    resultado: ResultadoChapa,
    material: MaterialChapa,
    acento: Color,
) {
    val familia = material.familia
    val nombreMaterial = stringResource(familia.nombreMaterialRes, stringResource(material.etiquetaRes))
    TarjetaAcento(acento = acento) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .border(1.dp, acento.copy(alpha = 0.65f), CircleShape),
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
                text = stringResource(R.string.chapas_resultado_titulo),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                color = acento,
            )
        }
        Spacer(Modifier.height(JewelrySpacing.Sm))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = resultado.pesoFormateado,
                style = CifraGrande,
                color = acento,
            )
            Spacer(Modifier.width(JewelrySpacing.Xs))
            Text(
                text = stringResource(R.string.unidad_gramos),
                style = MaterialTheme.typography.titleMedium,
                color = acento,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Spacer(Modifier.height(JewelrySpacing.Sm))
        Text(
            text = stringResource(R.string.chapas_resultado_para, nombreMaterial),
            style = MaterialTheme.typography.labelMedium,
            color = JewelryColors.TextPrimary,
            modifier = Modifier
                .border(1.dp, acento.copy(alpha = 0.65f), RoundedCornerShape(JewelryRadius.Pill))
                .padding(horizontal = JewelrySpacing.Md, vertical = JewelrySpacing.Xs),
        )
        Spacer(Modifier.height(JewelrySpacing.Md))
        DiamondDivider(widthFraction = 1f)
        Spacer(Modifier.height(JewelrySpacing.Sm))
        FilaDetalle(
            etiqueta = stringResource(R.string.chapas_detalle_volumen),
            valor = resultado.volumenFormateado,
            unidad = stringResource(R.string.unidad_cm3),
            acento = acento,
        )
        FilaDetalle(
            etiqueta = stringResource(R.string.chapas_detalle_densidad),
            valor = resultado.densidadFormateada,
            unidad = stringResource(R.string.unidad_g_cm3),
            acento = acento,
        )
        FilaDetalle(
            etiqueta = stringResource(R.string.chapas_detalle_pureza),
            valor = stringResource(R.string.chapas_pureza_formato, resultado.purezaFormateada, stringResource(material.etiquetaRes)),
            unidad = "",
            acento = acento,
        )
        FilaDetalle(
            etiqueta = stringResource(familia.metalFinoRes),
            valor = resultado.metalFinoFormateado,
            unidad = stringResource(R.string.unidad_gramos),
            acento = acento,
        )
    }
}

/** Etiqueta · puntos de guía · valor · unidad, como las filas de metal de las otras calculadoras. */
@Composable
private fun FilaDetalle(
    etiqueta: String,
    valor: String,
    unidad: String,
    acento: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = JewelrySpacing.Xs)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = JewelryColors.TextSecondary,
        )
        Spacer(Modifier.width(JewelrySpacing.Sm))
        LineaPunteada(color = acento.copy(alpha = 0.55f), modifier = Modifier.weight(1f))
        Spacer(Modifier.width(JewelrySpacing.Sm))
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
            color = acento,
        )
        if (unidad.isNotEmpty()) {
            Spacer(Modifier.width(JewelrySpacing.Xs))
            Text(
                text = unidad,
                style = MaterialTheme.typography.bodyMedium,
                color = JewelryColors.TextSecondary,
            )
        }
    }
}

// --- Previews ---

private val resultado18K = ResultadoChapa(
    pesoFormateado = "1,56",
    volumenFormateado = "0,100",
    densidadFormateada = "15,58",
    purezaFormateada = "75,0",
    metalFinoFormateado = "1,169",
)

private val medidasDeReferencia = mapOf(
    MedidaChapa.ANCHO to "10",
    MedidaChapa.ESPESOR to "0,5",
    MedidaChapa.LARGO to "20",
)

@Composable
private fun PreviewEnFondo(contenido: @Composable () -> Unit) {
    Calculadoradejoyeros2021Theme {
        Box(Modifier.background(JewelryColors.Background).padding(JewelrySpacing.Md)) { contenido() }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 900)
@Composable
private fun PesoChapasContentInicialPreview() {
    PreviewEnFondo {
        PesoChapasContent(PesoChapasUiState(), {}, {}, { _, _ -> }, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 1200)
@Composable
private fun PesoChapasContentOroPreview() {
    PreviewEnFondo {
        PesoChapasContent(
            PesoChapasUiState(
                medidas = medidasDeReferencia,
                dibujo = DibujoChapaUiState(etiquetaAncho = "10,00", etiquetaEspesor = "0,50", etiquetaLargo = "20,00", completa = true),
                resultado = resultado18K,
            ),
            {}, {}, { _, _ -> }, {}, {},
        )
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 1200)
@Composable
private fun PesoChapasContentPlataPreview() {
    PreviewEnFondo {
        PesoChapasContent(
            PesoChapasUiState(
                material = MaterialChapa.PLATA_925,
                medidas = medidasDeReferencia,
                dibujo = DibujoChapaUiState(etiquetaAncho = "10,00", etiquetaEspesor = "0,50", etiquetaLargo = "20,00", completa = true),
                resultado = ResultadoChapa("1,04", "0,100", "10,36", "92,5", "0,958"),
            ),
            {}, {}, { _, _ -> }, {}, {},
        )
    }
}
