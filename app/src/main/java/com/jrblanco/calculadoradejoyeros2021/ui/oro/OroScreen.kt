package com.jrblanco.calculadoradejoyeros2021.ui.oro

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalLiga
import com.jrblanco.calculadoradejoyeros2021.ui.components.AvisoTecnico
import com.jrblanco.calculadoradejoyeros2021.ui.components.BotonDorado
import com.jrblanco.calculadoradejoyeros2021.ui.components.CabeceraSeccion
import com.jrblanco.calculadoradejoyeros2021.ui.components.CampoCantidad
import com.jrblanco.calculadoradejoyeros2021.ui.components.FilaMetal
import com.jrblanco.calculadoradejoyeros2021.ui.components.JewelryScaffold
import com.jrblanco.calculadoradejoyeros2021.ui.components.OpcionSegmento
import com.jrblanco.calculadoradejoyeros2021.ui.components.SelectorSegmentado
import com.jrblanco.calculadoradejoyeros2021.ui.components.TarjetaAcento
import com.jrblanco.calculadoradejoyeros2021.ui.components.TarjetaTotal
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
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
            Toast.makeText(context, R.string.aviso_proximamente, Toast.LENGTH_SHORT).show()
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
                // Todas las leyes en dorado: lo que distingue a una ley no es un color.
                opciones = LeyOro.entries.map { OpcionSegmento(stringResource(it.etiquetaRes)) },
                seleccionada = uiState.ley.ordinal,
                onSeleccion = { onLeySeleccionada(LeyOro.entries[it]) },
            )

            if (uiState.ley.esSoloTecnica) {
                // Advertencia obligatoria de 500‰ (§2 del documento técnico): referencia
                // técnica de cálculo, no ley oficial española.
                AvisoTecnico(stringResource(R.string.oro_aviso_12k))
            }

            CabeceraSeccion(
                iconRes = R.drawable.ic_paleta,
                titulo = stringResource(R.string.oro_seccion_color),
            )
            SelectorSegmentado(
                // Cada color de oro se elige en su propio tono.
                opciones = ColorOro.entries.map {
                    OpcionSegmento(stringResource(it.etiquetaRes), it.acento)
                },
                seleccionada = uiState.color.ordinal,
                onSeleccion = { onColorSeleccionado(ColorOro.entries[it]) },
            )

            uiState.resultado?.let { resultado ->
                // Los resultados acompañan al color del oro que se está calculando.
                val acento = uiState.color.acento
                TarjetaAcento(acento = acento) {
                    resultado.metales.forEachIndexed { indice, metal ->
                        if (indice > 0) Spacer(Modifier.height(JewelrySpacing.Md))
                        val presentacion = metal.metal.presentacion()
                        FilaMetal(
                            imagenRes = presentacion.imagenRes,
                            imagenDescripcion = stringResource(presentacion.imagenDescripcionRes),
                            nombre = stringResource(presentacion.nombreRes),
                            valorFormateado = metal.gramosFormateados,
                            acento = acento,
                        )
                    }
                }

                TarjetaTotal(
                    etiqueta = stringResource(
                        R.string.oro_total,
                        stringResource(uiState.color.etiquetaRes).uppercase(),
                    ),
                    totalFormateado = resultado.totalFormateado,
                )
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

/** El tono con el que se pinta cada color de oro al seleccionarlo. */
private val ColorOro.acento: Color
    get() = when (this) {
        ColorOro.AMARILLO -> JewelryColors.GoldPrimary
        ColorOro.BLANCO -> JewelryColors.TealPrimary
        ColorOro.ROSA -> JewelryColors.RoseGold
        ColorOro.ROJO -> JewelryColors.RedGold
    }

private data class MetalPresentacion(
    val imagenRes: Int,
    val imagenDescripcionRes: Int,
    val nombreRes: Int,
)

private fun MetalLiga.presentacion(): MetalPresentacion = when (this) {
    MetalLiga.PLATA_FINA -> MetalPresentacion(
        imagenRes = R.drawable.modulo_plata,
        imagenDescripcionRes = R.string.metal_plata_fina_imagen,
        nombreRes = R.string.metal_plata_fina,
    )
    MetalLiga.COBRE -> MetalPresentacion(
        imagenRes = R.drawable.cobre,
        imagenDescripcionRes = R.string.metal_cobre_imagen,
        nombreRes = R.string.metal_cobre,
    )
    MetalLiga.PALADIO -> MetalPresentacion(
        imagenRes = R.drawable.paladio,
        imagenDescripcionRes = R.string.metal_paladio_imagen,
        nombreRes = R.string.metal_paladio,
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
