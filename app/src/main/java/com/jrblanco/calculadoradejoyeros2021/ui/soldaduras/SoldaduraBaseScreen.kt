package com.jrblanco.calculadoradejoyeros2021.ui.soldaduras

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.ui.components.AvisoTecnico
import com.jrblanco.calculadoradejoyeros2021.ui.components.FilaMetal
import com.jrblanco.calculadoradejoyeros2021.ui.components.JewelryScaffold
import com.jrblanco.calculadoradejoyeros2021.ui.components.TarjetaAcento
import com.jrblanco.calculadoradejoyeros2021.ui.components.TarjetaTotal
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing
import org.koin.androidx.compose.koinViewModel

/**
 * Preparación de la soldadura BASE (§5.2 del documento técnico). Resuelve el ViewModel y
 * delega el pintado en [SoldaduraBaseContent].
 */
@Composable
fun SoldaduraBaseScreen(
    onInfo: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SoldaduraBaseViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    SoldaduraBaseContent(
        uiState = uiState,
        onModoCambiado = viewModel::onModoCambiado,
        onCantidadCambiada = viewModel::onCantidadCambiada,
        onLimpiar = viewModel::onLimpiar,
        onGuardarFavoritos = {
            viewModel.onGuardarFavoritos()
            // Aviso efímero del sistema; el ViewModel no lo conoce.
            Toast.makeText(context, R.string.aviso_proximamente, Toast.LENGTH_SHORT).show()
        },
        onInfo = onInfo,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun SoldaduraBaseContent(
    uiState: SoldaduraBaseUiState,
    onModoCambiado: (ModoEntradaSoldadura) -> Unit,
    onCantidadCambiada: (String) -> Unit,
    onLimpiar: () -> Unit,
    onGuardarFavoritos: () -> Unit,
    onInfo: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    JewelryScaffold(
        onInfo = onInfo,
        modifier = modifier,
        title = stringResource(R.string.soldadura_base_titulo),
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
            // §9 lo exige ANTES del proceso informativo: la receta lleva cadmio y zinc.
            AvisoTecnico(stringResource(R.string.soldadura_aviso_seguridad))

            TarjetaProceso()

            TarjetaEntradaBase(
                cantidad = uiState.cantidadTexto,
                onCantidadCambiada = onCantidadCambiada,
                titulo = stringResource(
                    when (uiState.modo) {
                        ModoEntradaSoldadura.DESDE_METAL -> R.string.soldadura_entrada_oro_24k
                        ModoEntradaSoldadura.PESO_FINAL -> R.string.soldadura_base_entrada_peso
                    },
                ),
            )

            uiState.resultado?.let { resultado ->
                TarjetaAcento(acento = JewelryColors.TealPrimary) {
                    resultado.filas.forEachIndexed { indice, fila ->
                        if (indice > 0) Spacer(Modifier.height(JewelrySpacing.Md))
                        FilaMetal(
                            imagenRes = fila.ingrediente.imagenRes,
                            imagenDescripcion = stringResource(fila.ingrediente.imagenDescripcionRes),
                            nombre = nombreDeIngrediente(
                                ingrediente = fila.ingrediente,
                                // La base no tiene filas de oro 18K: el color no aplica.
                                colorOro = ColorOroSoldadura.AMARILLO,
                                baseNecesaria = false,
                            ),
                            valorFormateado = fila.gramosFormateados,
                            acento = JewelryColors.TealPrimary,
                        )
                    }
                }

                // El nombre tradicional se conserva y la ley real no se muestra (§5.2):
                // ni 754,15‰ ni corrección a 750.
                TarjetaTotal(
                    etiqueta = stringResource(R.string.soldadura_base_total),
                    totalFormateado = resultado.totalFormateado,
                )

                // §8.3, opción 1: nota de redondeo; jamás se ajusta un ingrediente.
                Text(
                    text = stringResource(R.string.soldadura_nota_redondeo),
                    style = MaterialTheme.typography.bodySmall,
                    color = JewelryColors.TextMuted,
                )
            }
        }
    }
}

/** El proceso de taller de §5.3, como texto informativo separado de la calculadora. */
@Composable
private fun TarjetaProceso(modifier: Modifier = Modifier) {
    TarjetaAcento(modifier) {
        Text(
            text = stringResource(R.string.soldadura_base_proceso_titulo),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            color = JewelryColors.GoldPrimary,
        )

        Spacer(Modifier.height(JewelrySpacing.Md))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.proceso),
                contentDescription = stringResource(R.string.soldadura_base_proceso_imagen),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(96.dp),
            )

            Spacer(Modifier.width(JewelrySpacing.Md))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Sm),
            ) {
                PasoDeProceso(numero = "1", textoRes = R.string.soldadura_base_proceso_1)
                PasoDeProceso(numero = "2", textoRes = R.string.soldadura_base_proceso_2)
                PasoDeProceso(numero = "3", textoRes = R.string.soldadura_base_proceso_3)
            }
        }

        Spacer(Modifier.height(JewelrySpacing.Md))

        // La masa es teórica: no se compensan pérdidas de fundición (§5.3).
        Text(
            text = stringResource(R.string.soldadura_base_masa_teorica),
            style = MaterialTheme.typography.bodySmall,
            color = JewelryColors.TextMuted,
        )
    }
}

@Composable
private fun PasoDeProceso(
    numero: String,
    textoRes: Int,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(1.dp, JewelryColors.GoldPrimary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = numero,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = JewelryColors.GoldPrimary,
            )
        }

        Spacer(Modifier.width(JewelrySpacing.Sm))

        Text(
            text = stringResource(textoRes),
            style = MaterialTheme.typography.bodyMedium,
            color = JewelryColors.TextSecondary,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Tarjeta de entrada: los lingotes de oro fino y el campo de gramos, en dorado. */
@Composable
private fun TarjetaEntradaBase(
    cantidad: String,
    onCantidadCambiada: (String) -> Unit,
    titulo: String,
    modifier: Modifier = Modifier,
) {
    TarjetaEntradaSoldadura(
        cantidad = cantidad,
        onCantidadCambiada = onCantidadCambiada,
        titulo = titulo,
        imagenRes = R.drawable.modulo_oro,
        imagenDescripcion = stringResource(R.string.metal_oro_24k_imagen),
        acento = JewelryColors.GoldPrimary,
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun SoldaduraBaseContentPreview() {
    Calculadoradejoyeros2021Theme {
        SoldaduraBaseContent(
            uiState = SoldaduraBaseUiState(
                cantidadTexto = "10",
                resultado = ResultadoSoldaduraBase(
                    // La receta patrón de §5.2 — la del documento, no la del mockup.
                    filas = listOf(
                        FilaSoldadura(IngredienteSoldadura.COBRE, "0,540"),
                        FilaSoldadura(IngredienteSoldadura.PLATA_FINA, "0,800"),
                        FilaSoldadura(IngredienteSoldadura.ZINC, "0,920"),
                        FilaSoldadura(IngredienteSoldadura.CADMIO, "1,000"),
                    ),
                    totalFormateado = "13,260",
                ),
            ),
            onModoCambiado = {},
            onCantidadCambiada = {},
            onLimpiar = {},
            onGuardarFavoritos = {},
            onInfo = {},
            onBack = {},
        )
    }
}
