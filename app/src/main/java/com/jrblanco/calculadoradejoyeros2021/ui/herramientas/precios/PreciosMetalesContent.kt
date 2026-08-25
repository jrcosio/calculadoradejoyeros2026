package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.precios

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MotivoErrorCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.OrigenDatos
import com.jrblanco.calculadoradejoyeros2021.domain.model.Tendencia
import com.jrblanco.calculadoradejoyeros2021.domain.model.UnidadPrecio
import com.jrblanco.calculadoradejoyeros2021.ui.components.AvisoTecnico
import com.jrblanco.calculadoradejoyeros2021.ui.components.BotonDorado
import com.jrblanco.calculadoradejoyeros2021.ui.components.CabeceraSeccion
import com.jrblanco.calculadoradejoyeros2021.ui.components.OpcionSegmento
import com.jrblanco.calculadoradejoyeros2021.ui.components.SelectorSegmentado
import com.jrblanco.calculadoradejoyeros2021.ui.components.TarjetaAcento
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import com.jrblanco.calculadoradejoyeros2021.ui.theme.CifraGrande
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryRadius
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySize
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing

/**
 * La sección de precios sin estado: una columna con la tarjeta de cotizaciones y la de
 * información del mercado. **Sin** scaffold, scroll, `imePadding` ni padding exterior: los
 * pone el armazón de Herramientas una sola vez.
 */
@Composable
fun PreciosMetalesContent(
    uiState: PreciosMetalesUiState,
    onUnidadSeleccionada: (UnidadPrecio) -> Unit,
    onMetalSeleccionado: (MetalCotizado) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Md),
    ) {
        TarjetaPrecios(
            uiState = uiState,
            onUnidadSeleccionada = onUnidadSeleccionada,
            onMetalSeleccionado = onMetalSeleccionado,
            onReintentar = onReintentar,
        )
        // Sin ningún precio (ni antiguo) la tarjeta de mercado solo tendría guiones.
        if (uiState.filas.any { it.precioFormateado != null }) {
            TarjetaMercado(detalle = uiState.detalle, metal = uiState.seleccionado)
        }
    }
}

@Composable
private fun TarjetaPrecios(
    uiState: PreciosMetalesUiState,
    onUnidadSeleccionada: (UnidadPrecio) -> Unit,
    onMetalSeleccionado: (MetalCotizado) -> Unit,
    onReintentar: () -> Unit,
) {
    TarjetaAcento(acento = JewelryColors.TealPrimary) {
        Text(
            text = stringResource(R.string.precios_titulo),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            color = JewelryColors.TealPrimary,
        )
        uiState.ultimaConsultaEpochMillis?.let { instante ->
            Text(
                text = stringResource(R.string.precios_actualizado, fechaHoraLocal(instante)),
                style = MaterialTheme.typography.bodySmall,
                color = JewelryColors.TextMuted,
            )
        }
        if (uiState.origen == OrigenDatos.CACHE || uiState.origen == OrigenDatos.CACHE_EN_ESPERA) {
            Text(
                text = stringResource(R.string.precios_origen_cache),
                style = MaterialTheme.typography.bodySmall,
                color = JewelryColors.TextMuted,
            )
        }

        Spacer(Modifier.height(JewelrySpacing.Md))
        CabeceraSeccion(
            iconRes = R.drawable.ic_balanza,
            titulo = stringResource(R.string.precios_seccion_unidad),
            tinte = JewelryColors.TealPrimary,
        )
        Spacer(Modifier.height(JewelrySpacing.Sm))
        SelectorSegmentado(
            opciones = UnidadPrecio.entries.map {
                OpcionSegmento(stringResource(it.etiquetaRes), JewelryColors.TealPrimary)
            },
            seleccionada = uiState.unidad.ordinal,
            onSeleccion = { onUnidadSeleccionada(UnidadPrecio.entries[it]) },
        )
        Spacer(Modifier.height(JewelrySpacing.Md))

        Avisos(uiState)

        if (uiState.fase == FasePrecios.CARGANDO && uiState.filas.isEmpty()) {
            IndicadorDeCarga()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Sm)) {
                uiState.filas.forEach { fila ->
                    FilaPrecio(
                        fila = fila,
                        seleccionada = fila.metal == uiState.seleccionado,
                        // Si los cinco comparten motivo, ya lo dice el aviso global: no se repite por fila.
                        mostrarMotivo = fila.error != null && fila.error != uiState.errorGlobal,
                        onClick = { onMetalSeleccionado(fila.metal) },
                    )
                }
            }
        }

        if (uiState.reintentando) {
            Spacer(Modifier.height(JewelrySpacing.Sm))
            IndicadorDeCarga()
        } else if (uiState.puedeReintentar) {
            Spacer(Modifier.height(JewelrySpacing.Md))
            BotonDorado(
                iconRes = R.drawable.ic_refrescar,
                texto = stringResource(R.string.precios_accion_reintentar),
                onClick = onReintentar,
            )
        }

        Spacer(Modifier.height(JewelrySpacing.Md))
        Text(
            text = stringResource(R.string.precios_nota_orientativos),
            style = MaterialTheme.typography.bodySmall,
            color = JewelryColors.TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.precios_fuente),
            style = MaterialTheme.typography.bodySmall,
            color = JewelryColors.TealPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun Avisos(uiState: PreciosMetalesUiState) {
    when (uiState.fase) {
        FasePrecios.ERROR -> {
            val motivo = uiState.errorGlobal ?: MotivoErrorCotizacion.DESCONOCIDO
            val texto = if (uiState.filas.any { it.desactualizada }) {
                stringResource(motivo.mensajeRes) + " " + stringResource(R.string.precios_aviso_desactualizado)
            } else {
                stringResource(motivo.mensajeRes)
            }
            AvisoTecnico(texto)
            Spacer(Modifier.height(JewelrySpacing.Md))
        }
        FasePrecios.PARCIAL -> {
            AvisoTecnico(stringResource(R.string.precios_aviso_parcial))
            Spacer(Modifier.height(JewelrySpacing.Md))
        }
        FasePrecios.CARGANDO, FasePrecios.LISTO -> Unit
    }
    if (uiState.avisoEspera) {
        AvisoTecnico(stringResource(R.string.precios_aviso_espera))
        Spacer(Modifier.height(JewelrySpacing.Md))
    }
}

@Composable
private fun IndicadorDeCarga() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = JewelrySpacing.Lg),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            color = JewelryColors.TealPrimary,
            modifier = Modifier.size(22.dp),
            strokeWidth = 2.dp,
        )
        Spacer(Modifier.width(JewelrySpacing.Sm))
        Text(
            text = stringResource(R.string.precios_cargando),
            style = MaterialTheme.typography.bodyMedium,
            color = JewelryColors.TextSecondary,
        )
    }
}

/** Una fila de metal, pulsable: la elegida lleva filete teal y alimenta la tarjeta de mercado. */
@Composable
private fun FilaPrecio(
    fila: FilaMetalPrecio,
    seleccionada: Boolean,
    mostrarMotivo: Boolean,
    onClick: () -> Unit,
) {
    val forma = RoundedCornerShape(JewelryRadius.Small)
    val borde = if (seleccionada) JewelryColors.TealPrimary else JewelryColors.Border
    val colorCifra = if (fila.desactualizada) JewelryColors.TextMuted else JewelryColors.TealPrimary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = JewelrySize.MinTouchTarget)
            .background(JewelryColors.Surface, forma)
            .border(1.dp, borde, forma)
            .selectable(selected = seleccionada, role = Role.RadioButton, onClick = onClick)
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = JewelrySpacing.Md, vertical = JewelrySpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(fila.metal.imagenRes),
            contentDescription = stringResource(fila.metal.imagenDescripcionRes),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(44.dp),
        )
        Spacer(Modifier.width(JewelrySpacing.Md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(fila.metal.nombreRes),
                style = MaterialTheme.typography.bodyLarge,
                color = JewelryColors.TextPrimary,
            )
            Text(
                text = fila.metal.simboloApi,
                style = MaterialTheme.typography.labelMedium,
                color = JewelryColors.TealPrimary,
            )
            fila.error?.takeIf { mostrarMotivo }?.let { motivo ->
                Text(
                    text = stringResource(motivo.mensajeRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = JewelryColors.Danger,
                )
            }
            if (fila.desactualizada) {
                Text(
                    text = stringResource(R.string.precios_desactualizado),
                    style = MaterialTheme.typography.bodySmall,
                    color = JewelryColors.TextMuted,
                )
            }
        }
        Spacer(Modifier.width(JewelrySpacing.Sm))
        Text(
            text = fila.precioFormateado ?: SIN_DATO,
            style = CifraGrande.copy(fontSize = 24.sp, lineHeight = 30.sp),
            color = colorCifra,
        )
        Spacer(Modifier.width(JewelrySpacing.Xs))
        Text(
            text = fila.unidad?.let { stringResource(it.simboloRes) } ?: fila.etiquetaUnidadOrigen.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = JewelryColors.GoldPrimary,
        )
        Spacer(Modifier.width(JewelrySpacing.Sm))
        val tendencia = fila.tendencia
        if (tendencia != null) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = stringResource(tendencia.descripcionRes),
                tint = tendencia.color,
                modifier = Modifier
                    .size(22.dp)
                    .rotate(tendencia.rotacionFlecha),
            )
        } else {
            Spacer(Modifier.size(22.dp))
        }
    }
}

/** «Información del mercado» del metal elegido; con [detalle] nulo pinta guiones. */
@Composable
private fun TarjetaMercado(detalle: DetalleMercado?, metal: MetalCotizado) {
    TarjetaAcento(acento = JewelryColors.GoldPrimary) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .border(1.dp, JewelryColors.GoldPrimary.copy(alpha = 0.65f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_balanza),
                    contentDescription = null,
                    tint = JewelryColors.GoldPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(JewelrySpacing.Md))
            Column {
                Text(
                    text = stringResource(R.string.precios_mercado_titulo),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    color = JewelryColors.GoldPrimary,
                )
                Text(
                    text = stringResource(R.string.precios_mercado_subtitulo),
                    style = MaterialTheme.typography.bodySmall,
                    color = JewelryColors.TextMuted,
                )
            }
        }

        Spacer(Modifier.height(JewelrySpacing.Md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(metal.imagenRes),
                contentDescription = stringResource(metal.imagenDescripcionRes),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(32.dp),
            )
            Spacer(Modifier.width(JewelrySpacing.Sm))
            Text(
                text = stringResource(R.string.precios_mercado_metal, stringResource(metal.nombreRes), metal.simboloApi),
                style = MaterialTheme.typography.titleMedium,
                color = JewelryColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Pildora(texto = metal.simboloApi, color = JewelryColors.GoldPrimary)
            Spacer(Modifier.width(JewelrySpacing.Xs))
            Pildora(texto = detalle?.moneda ?: SIN_DATO, color = JewelryColors.TealPrimary)
        }
        if (detalle?.desactualizada == true) {
            Text(
                text = stringResource(R.string.precios_desactualizado),
                style = MaterialTheme.typography.bodySmall,
                color = JewelryColors.TextMuted,
            )
        }

        Spacer(Modifier.height(JewelrySpacing.Md))
        val colorTendencia = detalle?.tendencia?.color ?: JewelryColors.TextPrimary
        val unidad = detalle?.unidad?.let { stringResource(it.etiquetaRes) }
            ?: detalle?.etiquetaUnidadOrigen
            ?: SIN_DATO
        val actualizacion = detalle?.let { fechaHoraLocal(it.instanteMercadoEpochMillis) } ?: SIN_DATO
        val datos = listOf(
            Dato(R.string.precios_detalle_ask, detalle?.ask ?: SIN_DATO),
            Dato(R.string.precios_detalle_bid, detalle?.bid ?: SIN_DATO),
            Dato(R.string.precios_detalle_maximo, detalle?.maximo ?: SIN_DATO),
            Dato(R.string.precios_detalle_minimo, detalle?.minimo ?: SIN_DATO),
            Dato(R.string.precios_detalle_variacion, detalle?.variacion ?: SIN_DATO, colorTendencia),
            Dato(R.string.precios_detalle_variacion_pct, detalle?.variacionPorcentaje ?: SIN_DATO, colorTendencia),
            Dato(R.string.precios_detalle_unidad, unidad),
            Dato(R.string.precios_detalle_actualizacion, actualizacion),
        )
        // Con la fuente del sistema grande, una columna: nada se recorta (SC-009).
        val porFila = if (LocalConfiguration.current.fontScale > 1.3f) 1 else 2
        Column(verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Sm)) {
            datos.chunked(porFila).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(JewelrySpacing.Sm)) {
                    fila.forEach { dato -> DatoMercado(dato, Modifier.weight(1f)) }
                }
            }
        }
    }
}

private data class Dato(val etiquetaRes: Int, val valor: String, val color: Color = JewelryColors.GoldPrimary)

@Composable
private fun DatoMercado(dato: Dato, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(JewelryRadius.Small)
    Column(
        modifier = modifier
            .background(JewelryColors.Surface, forma)
            .border(1.dp, JewelryColors.Border, forma)
            .padding(JewelrySpacing.Sm)
            .semantics(mergeDescendants = true) {},
    ) {
        Text(
            text = stringResource(dato.etiquetaRes),
            style = MaterialTheme.typography.bodySmall,
            color = JewelryColors.TextMuted,
        )
        Text(
            text = dato.valor,
            style = MaterialTheme.typography.bodyLarge,
            color = dato.color,
        )
    }
}

@Composable
private fun Pildora(texto: String, color: Color) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.65f), RoundedCornerShape(JewelryRadius.Pill))
            .padding(horizontal = JewelrySpacing.Sm, vertical = JewelrySpacing.Xs),
    )
}

private const val SIN_DATO = "—"

// --- Previews ---

private val filasDePreview = listOf(
    FilaMetalPrecio(MetalCotizado.ORO, "148,10", UnidadPrecio.GRAMO, null, Tendencia.BAJA, null, false),
    FilaMetalPrecio(MetalCotizado.PLATA, "1,20", UnidadPrecio.GRAMO, null, Tendencia.SUBE, null, false),
    FilaMetalPrecio(MetalCotizado.COBRE, "0,0089", UnidadPrecio.GRAMO, null, Tendencia.PLANA, null, false),
    FilaMetalPrecio(MetalCotizado.PALADIO, "34,90", UnidadPrecio.GRAMO, null, Tendencia.SUBE, null, false),
    FilaMetalPrecio(MetalCotizado.RODIO, "152,30", UnidadPrecio.GRAMO, null, Tendencia.BAJA, null, false),
)

private val detalleDePreview = DetalleMercado(
    metal = MetalCotizado.ORO,
    moneda = "EUR",
    ask = "148,13",
    bid = "148,07",
    maximo = "151,03",
    minimo = "148,04",
    variacion = "-1,46",
    variacionPorcentaje = "-0,97",
    tendencia = Tendencia.BAJA,
    unidad = UnidadPrecio.GRAMO,
    etiquetaUnidadOrigen = "OUNCE",
    instanteMercadoEpochMillis = 1_787_665_680_000L,
    desactualizada = false,
)

@Preview(showBackground = true, widthDp = 411, heightDp = 1000)
@Composable
private fun PreciosMetalesContentPreview() {
    Calculadoradejoyeros2021Theme {
        Box(Modifier.background(JewelryColors.Background).padding(JewelrySpacing.Md)) {
            PreciosMetalesContent(
                uiState = PreciosMetalesUiState(
                    fase = FasePrecios.LISTO,
                    filas = filasDePreview,
                    detalle = detalleDePreview,
                    origen = OrigenDatos.RED,
                    ultimaConsultaEpochMillis = 1_787_670_000_000L,
                ),
                onUnidadSeleccionada = {},
                onMetalSeleccionado = {},
                onReintentar = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 600)
@Composable
private fun PreciosMetalesContentCargandoPreview() {
    Calculadoradejoyeros2021Theme {
        Box(Modifier.background(JewelryColors.Background).padding(JewelrySpacing.Md)) {
            PreciosMetalesContent(
                uiState = PreciosMetalesUiState(),
                onUnidadSeleccionada = {},
                onMetalSeleccionado = {},
                onReintentar = {},
            )
        }
    }
}
