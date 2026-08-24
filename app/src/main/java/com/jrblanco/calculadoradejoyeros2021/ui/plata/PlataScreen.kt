package com.jrblanco.calculadoradejoyeros2021.ui.plata

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
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata
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
 * Calculadora de aleaciones de plata. Resuelve el ViewModel y delega el pintado en
 * [PlataContent].
 */
@Composable
fun PlataScreen(
    onInfo: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlataViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    PlataContent(
        uiState = uiState,
        onCantidadCambiada = viewModel::onCantidadCambiada,
        onLeySeleccionada = viewModel::onLeySeleccionada,
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
fun PlataContent(
    uiState: PlataUiState,
    onCantidadCambiada: (String) -> Unit,
    onLeySeleccionada: (LeyPlata) -> Unit,
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
        title = stringResource(R.string.modulo_plata_titulo),
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
                titulo = stringResource(R.string.plata_seccion_ley),
                tinte = JewelryColors.SilverPrimary,
            )
            SelectorSegmentado(
                // Las cuatro leyes en teal, como el mockup: lo que las distingue no es un
                // color sino el «(ley)» de las dos oficiales.
                opciones = LeyPlata.entries.map {
                    OpcionSegmento(stringResource(it.etiquetaRes), JewelryColors.TealPrimary)
                },
                seleccionada = uiState.ley.ordinal,
                onSeleccion = { onLeySeleccionada(LeyPlata.entries[it]) },
            )

            // Advertencia obligatoria de 950‰ y 900‰ (§3 del documento técnico): son
            // composiciones técnicas, no leyes oficiales de contraste en España. Un texto
            // por ley, porque cada uno la sitúa respecto de las oficiales.
            uiState.ley.avisoRes?.let { AvisoTecnico(stringResource(it)) }

            uiState.resultado?.let { resultado ->
                // El cobre es el único metal de liga de esta calculadora (§2, §33).
                TarjetaAcento(acento = JewelryColors.TealPrimary) {
                    FilaMetal(
                        imagenRes = R.drawable.cobre,
                        imagenDescripcion = stringResource(R.string.metal_cobre_imagen),
                        nombre = stringResource(R.string.metal_cobre),
                        valorFormateado = resultado.cobreFormateado,
                        acento = JewelryColors.TealPrimary,
                    )
                }

                TarjetaTotal(
                    etiqueta = stringResource(
                        R.string.plata_total,
                        uiState.ley.milesimas.toString(),
                    ),
                    totalFormateado = resultado.totalFormateado,
                    // Lo que pesa al final es plata: entrada y total enmarcan en plateado
                    // un centro en teal.
                    acento = JewelryColors.SilverPrimary,
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

/** Tarjeta de entrada: los lingotes de plata fina y el campo de gramos. */
@Composable
private fun TarjetaEntrada(
    cantidad: String,
    onCantidadCambiada: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TarjetaAcento(modifier, acento = JewelryColors.SilverPrimary) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.modulo_plata),
                contentDescription = stringResource(R.string.plata_entrada_imagen),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(96.dp),
            )

            Spacer(Modifier.width(JewelrySpacing.Md))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.plata_entrada_titulo),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    color = JewelryColors.SilverPrimary,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(JewelrySpacing.Sm))

                CampoCantidad(
                    valor = cantidad,
                    onCambio = onCantidadCambiada,
                    acento = JewelryColors.SilverPrimary,
                    borde = JewelryColors.SilverDark,
                )
            }
        }
    }
}

// --- Cómo se pinta cada valor de dominio. Vive aquí y no en el enum para que ---
// --- `domain/` siga libre de Android, igual que `LeyOro.etiquetaRes` en oro.  ---

private val LeyPlata.etiquetaRes: Int
    get() = when (this) {
        LeyPlata.LEY_950 -> R.string.plata_ley_950
        LeyPlata.LEY_925 -> R.string.plata_ley_925
        LeyPlata.LEY_900 -> R.string.plata_ley_900
        LeyPlata.LEY_800 -> R.string.plata_ley_800
    }

/**
 * Texto de advertencia de la ley, o `null` si es una de las oficiales españolas.
 *
 * Nulable y no `Int` a propósito: así el `when` cubre las cuatro ramas sin inventarles un
 * aviso a 925 y 800, y el `?.let` de la pantalla sustituye al `if (esSoloTecnica)` en
 * lugar de duplicar la condición.
 */
private val LeyPlata.avisoRes: Int?
    get() = when (this) {
        LeyPlata.LEY_950 -> R.string.plata_aviso_950
        LeyPlata.LEY_900 -> R.string.plata_aviso_900
        LeyPlata.LEY_925, LeyPlata.LEY_800 -> null
    }

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun PlataContentPreview() {
    Calculadoradejoyeros2021Theme {
        PlataContent(
            uiState = PlataUiState(
                cantidadTexto = "25",
                ley = LeyPlata.LEY_925,
                resultado = ResultadoPlata(
                    cobreFormateado = "2,000",
                    totalFormateado = "27,000",
                ),
            ),
            onCantidadCambiada = {},
            onLeySeleccionada = {},
            onLimpiar = {},
            onGuardarFavoritos = {},
            onInfo = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun PlataContent950Preview() {
    Calculadoradejoyeros2021Theme {
        PlataContent(
            uiState = PlataUiState(
                cantidadTexto = "100",
                ley = LeyPlata.LEY_950,
                resultado = ResultadoPlata(
                    // Truncado, no redondeado: con HALF_UP sería 5,158 y la ley caería
                    // por debajo de 950‰ (§17, §19).
                    cobreFormateado = "5,157",
                    totalFormateado = "105,157",
                ),
            ),
            onCantidadCambiada = {},
            onLeySeleccionada = {},
            onLimpiar = {},
            onGuardarFavoritos = {},
            onInfo = {},
            onBack = {},
        )
    }
}
