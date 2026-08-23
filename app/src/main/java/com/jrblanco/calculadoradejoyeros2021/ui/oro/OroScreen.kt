package com.jrblanco.calculadoradejoyeros2021.ui.oro

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalLiga
import com.jrblanco.calculadoradejoyeros2021.ui.components.JewelryScaffold
import com.jrblanco.calculadoradejoyeros2021.ui.components.SelectorSegmentado
import com.jrblanco.calculadoradejoyeros2021.ui.components.TarjetaAcento
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import com.jrblanco.calculadoradejoyeros2021.ui.theme.CifraGrande
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryRadius
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySize
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing
import org.koin.androidx.compose.koinViewModel

/**
 * Calculadora de aleaciones de oro. Resuelve el ViewModel y delega el pintado en
 * [OroContent].
 */
@Composable
fun OroScreen(
    onInfo: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OroViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    OroContent(
        uiState = uiState,
        onCantidadCambiada = viewModel::onCantidadCambiada,
        onLeySeleccionada = viewModel::onLeySeleccionada,
        onColorSeleccionado = viewModel::onColorSeleccionado,
        onLimpiar = viewModel::onLimpiar,
        onGuardarFavoritos = {
            viewModel.onGuardarFavoritos()
            // Aviso efímero del sistema: los Toast se reemplazan solos y no se
            // acumulan por muchas pulsaciones que haya. El ViewModel no lo conoce.
            Toast.makeText(context, R.string.oro_proximamente, Toast.LENGTH_SHORT).show()
        },
        onInfo = onInfo,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun OroContent(
    uiState: OroUiState,
    onCantidadCambiada: (String) -> Unit,
    onLeySeleccionada: (LeyOro) -> Unit,
    onColorSeleccionado: (ColorOro) -> Unit,
    onLimpiar: () -> Unit,
    onGuardarFavoritos: () -> Unit,
    onInfo: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    JewelryScaffold(
        onInfo = onInfo,
        modifier = modifier,
        // Sección de módulo: título y flecha atrás, sin barra inferior.
        title = stringResource(R.string.modulo_oro_titulo),
        onBack = onBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                // Con el teclado desplegado los resultados siguen alcanzables.
                .imePadding()
                .padding(JewelrySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Md),
        ) {
            TarjetaEntrada(
                cantidad = uiState.cantidadTexto,
                onCantidadCambiada = onCantidadCambiada,
            )

            CabeceraSeccion(
                iconRes = R.drawable.ic_lingotes,
                titulo = stringResource(R.string.oro_seccion_ley),
            )
            SelectorSegmentado(
                opciones = LeyOro.entries.map { stringResource(it.etiquetaRes) },
                seleccionada = uiState.ley.ordinal,
                onSeleccion = { onLeySeleccionada(LeyOro.entries[it]) },
            )

            if (uiState.ley.esSoloTecnica) {
                AvisoLeyTecnica()
            }

            CabeceraSeccion(
                iconRes = R.drawable.ic_paleta,
                titulo = stringResource(R.string.oro_seccion_color),
            )
            SelectorSegmentado(
                opciones = ColorOro.entries.map { stringResource(it.etiquetaRes) },
                seleccionada = uiState.color.ordinal,
                onSeleccion = { onColorSeleccionado(ColorOro.entries[it]) },
                acento = JewelryColors.TealPrimary,
            )

            uiState.resultado?.let { resultado ->
                TarjetaAcento(acento = JewelryColors.TealPrimary) {
                    resultado.metales.forEachIndexed { indice, metal ->
                        if (indice > 0) Spacer(Modifier.height(JewelrySpacing.Md))
                        FilaMetal(metal)
                    }
                }

                TarjetaTotal(
                    color = uiState.color,
                    totalFormateado = resultado.totalFormateado,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(JewelrySpacing.Md)) {
                BotonDorado(
                    iconRes = R.drawable.ic_refrescar,
                    texto = stringResource(R.string.oro_limpiar),
                    onClick = onLimpiar,
                    modifier = Modifier.weight(1f),
                )
                BotonDorado(
                    iconRes = R.drawable.ic_estrella,
                    texto = stringResource(R.string.oro_guardar_favoritos),
                    onClick = onGuardarFavoritos,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Advertencia obligatoria de 500‰ (§2 del documento técnico): referencia técnica de
 * cálculo, no ley oficial española. Región viva para que el lector de pantalla la
 * anuncie al aparecer.
 */
@Composable
private fun AvisoLeyTecnica(modifier: Modifier = Modifier) {
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
            text = stringResource(R.string.oro_aviso_12k),
            style = MaterialTheme.typography.bodyMedium,
            color = JewelryColors.Warning,
        )
    }
}

/**
 * Botón dorado a mano, como el de la portada: `Button` de Material impone un
 * contenedor opaco y su propia geometría.
 */
@Composable
private fun BotonDorado(
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

/** Tarjeta de entrada: los lingotes de partida y el campo de gramos. */
@Composable
private fun TarjetaEntrada(
    cantidad: String,
    onCantidadCambiada: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TarjetaAcento(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.modulo_oro),
                contentDescription = stringResource(R.string.oro_entrada_imagen),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(96.dp),
            )

            Spacer(Modifier.width(JewelrySpacing.Md))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.oro_entrada_titulo),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    color = JewelryColors.GoldSoft,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(JewelrySpacing.Sm))

                CampoCantidad(
                    valor = cantidad,
                    onCambio = onCantidadCambiada,
                )
            }
        }
    }
}

/**
 * Campo de cantidad a mano con `BasicTextField`: `OutlinedTextField` impone altura,
 * padding y tipografía de Material y el diseño pide cifra grande centrada en caja
 * redondeada con el sufijo «gr».
 */
@Composable
private fun CampoCantidad(
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(JewelryRadius.Medium)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .background(JewelryColors.Background, shape)
            .border(1.dp, JewelryColors.BorderGold, shape)
            .padding(horizontal = JewelrySpacing.Md, vertical = JewelrySpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
            cursorBrush = SolidColor(JewelryColors.GoldPrimary),
        )

        Spacer(Modifier.width(JewelrySpacing.Sm))

        Text(
            text = stringResource(R.string.oro_entrada_unidad),
            style = MaterialTheme.typography.titleMedium,
            color = JewelryColors.GoldPrimary,
        )
    }
}

/** Cabecera de sección: icono dorado y título, anunciado como encabezado. */
@Composable
private fun CabeceraSeccion(
    iconRes: Int,
    titulo: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.semantics { heading() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = JewelryColors.GoldPrimary,
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

/** Una fila de resultado: imagen del metal, nombre, puntos de guía y gramos. */
@Composable
private fun FilaMetal(
    metal: MetalCalculado,
    modifier: Modifier = Modifier,
) {
    val presentacion = metal.metal.presentacion()
    Row(
        // Un solo anuncio por fila para el lector: «Plata fina, 2,191 gr».
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(presentacion.imagenRes),
            contentDescription = stringResource(presentacion.imagenDescripcionRes),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(44.dp),
        )

        Spacer(Modifier.width(JewelrySpacing.Sm))

        Text(
            text = stringResource(presentacion.nombreRes),
            style = MaterialTheme.typography.bodyLarge,
            color = JewelryColors.TextPrimary,
        )

        LineaPunteada(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = JewelrySpacing.Sm),
        )

        Text(
            text = metal.gramosFormateados,
            style = CifraGrande.copy(fontSize = 26.sp, lineHeight = 32.sp),
            color = JewelryColors.TealPrimary,
        )

        Spacer(Modifier.width(JewelrySpacing.Xs))

        Text(
            text = stringResource(R.string.oro_entrada_unidad),
            style = MaterialTheme.typography.bodyMedium,
            color = JewelryColors.GoldPrimary,
        )
    }
}

/** Línea de puntos que guía el ojo del nombre del metal a su cifra. */
@Composable
private fun LineaPunteada(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.height(2.dp)) {
        drawLine(
            color = JewelryColors.TealDark,
            start = Offset(0f, center.y),
            end = Offset(size.width, center.y),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(2.dp.toPx(), 5.dp.toPx()),
            ),
        )
    }
}

/** Tarjeta de total: balanza, color resultante y peso final de la aleación. */
@Composable
private fun TarjetaTotal(
    color: ColorOro,
    totalFormateado: String,
    modifier: Modifier = Modifier,
) {
    TarjetaAcento(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .border(1.dp, JewelryColors.GoldPrimary.copy(alpha = 0.8f), CircleShape),
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

            Text(
                text = stringResource(
                    R.string.oro_total,
                    stringResource(color.etiquetaRes).uppercase(),
                ),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                color = JewelryColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )

            Spacer(Modifier.width(JewelrySpacing.Sm))

            Text(
                text = totalFormateado,
                style = CifraGrande.copy(fontSize = 26.sp, lineHeight = 32.sp),
                color = JewelryColors.GoldPrimary,
            )

            Spacer(Modifier.width(JewelrySpacing.Xs))

            Text(
                text = stringResource(R.string.oro_entrada_unidad),
                style = MaterialTheme.typography.bodyMedium,
                color = JewelryColors.GoldPrimary,
            )
        }
    }
}

// --- Cómo se pinta cada valor de dominio. Vive aquí y no en los enums para que ---
// --- `domain/` siga libre de Android, igual que `ModulePresentation` en Home.  ---

private val LeyOro.etiquetaRes: Int
    get() = when (this) {
        LeyOro.LEY_18K -> R.string.oro_ley_18k
        LeyOro.LEY_14K -> R.string.oro_ley_14k
        LeyOro.LEY_12K -> R.string.oro_ley_12k
        LeyOro.LEY_9K -> R.string.oro_ley_9k
    }

private val ColorOro.etiquetaRes: Int
    get() = when (this) {
        ColorOro.AMARILLO -> R.string.oro_color_amarillo
        ColorOro.BLANCO -> R.string.oro_color_blanco
        ColorOro.ROSA -> R.string.oro_color_rosa
        ColorOro.ROJO -> R.string.oro_color_rojo
    }

private data class MetalPresentacion(
    val imagenRes: Int,
    val imagenDescripcionRes: Int,
    val nombreRes: Int,
)

private fun MetalLiga.presentacion(): MetalPresentacion = when (this) {
    MetalLiga.PLATA_FINA -> MetalPresentacion(
        imagenRes = R.drawable.modulo_plata,
        imagenDescripcionRes = R.string.oro_metal_plata_imagen,
        nombreRes = R.string.oro_metal_plata,
    )
    MetalLiga.COBRE -> MetalPresentacion(
        imagenRes = R.drawable.cobre,
        imagenDescripcionRes = R.string.oro_metal_cobre_imagen,
        nombreRes = R.string.oro_metal_cobre,
    )
    MetalLiga.PALADIO -> MetalPresentacion(
        imagenRes = R.drawable.paladio,
        imagenDescripcionRes = R.string.oro_metal_paladio_imagen,
        nombreRes = R.string.oro_metal_paladio,
    )
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun OroContentPreview() {
    Calculadoradejoyeros2021Theme {
        OroContent(
            uiState = OroUiState(
                cantidadTexto = "50",
                ley = LeyOro.LEY_18K,
                color = ColorOro.BLANCO,
                resultado = ResultadoOro(
                    metales = listOf(
                        MetalCalculado(MetalLiga.PLATA_FINA, "6,564"),
                        MetalCalculado(MetalLiga.COBRE, "2,690"),
                        MetalCalculado(MetalLiga.PALADIO, "7,346"),
                    ),
                    totalFormateado = "66,600",
                ),
            ),
            onCantidadCambiada = {},
            onLeySeleccionada = {},
            onColorSeleccionado = {},
            onLimpiar = {},
            onGuardarFavoritos = {},
            onInfo = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun OroContent12KPreview() {
    Calculadoradejoyeros2021Theme {
        OroContent(
            uiState = OroUiState(
                cantidadTexto = "100",
                ley = LeyOro.LEY_12K,
                color = ColorOro.AMARILLO,
                resultado = ResultadoOro(
                    metales = listOf(
                        MetalCalculado(MetalLiga.PLATA_FINA, "69,836"),
                        MetalCalculado(MetalLiga.COBRE, "29,964"),
                    ),
                    totalFormateado = "199,800",
                ),
            ),
            onCantidadCambiada = {},
            onLeySeleccionada = {},
            onColorSeleccionado = {},
            onLimpiar = {},
            onGuardarFavoritos = {},
            onInfo = {},
            onBack = {},
        )
    }
}
