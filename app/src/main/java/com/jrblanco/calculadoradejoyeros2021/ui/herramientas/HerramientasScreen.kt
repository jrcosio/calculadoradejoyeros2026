package com.jrblanco.calculadoradejoyeros2021.ui.herramientas

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.components.JewelryScaffold
import com.jrblanco.calculadoradejoyeros2021.ui.components.OpcionSegmento
import com.jrblanco.calculadoradejoyeros2021.ui.components.SelectorSegmentado
import com.jrblanco.calculadoradejoyeros2021.ui.components.TarjetaAcento
import com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas.PesoChapasSection
import com.jrblanco.calculadoradejoyeros2021.ui.herramientas.precios.PreciosMetalesSection
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing
import org.koin.androidx.compose.koinViewModel

/**
 * Pantalla de Herramientas: el armazón resuelve su ViewModel y cablea las dos secciones, cada
 * una con el suyo, que se crea al abrirla por primera vez y vive mientras dure esta pantalla.
 */
@Composable
fun HerramientasScreen(
    onInfo: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    favoritoId: Long? = null,
    viewModel: HerramientasViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Un favorito de chapa abre su sub-herramienta. El id baja por el *slot*, así que
    // `HerramientasContent` no cambia de firma y `PreciosMetalesSection` sigue sin componerse:
    // abrir un favorito de chapa no debe gastar cuota de la API de precios.
    LaunchedEffect(favoritoId) {
        if (favoritoId != null) viewModel.abrirFavoritoDeChapa()
    }

    HerramientasContent(
        uiState = uiState,
        onSubherramientaSeleccionada = viewModel::onSubherramientaSeleccionada,
        onInfo = onInfo,
        onBack = onBack,
        modifier = modifier,
        precios = { PreciosMetalesSection() },
        chapas = { PesoChapasSection(favoritoId = favoritoId) },
    )
}

/**
 * El armazón sin estado: barra, selector de sub-herramientas y, debajo, la sección elegida a
 * través de los *slots* [precios] y [chapas] — así la pantalla se prueba con marcadores y no
 * conoce los ViewModels de las secciones. En la primera visita ([HerramientasUiState.subherramienta]
 * nulo) solo se ve el selector y una invitación a elegir.
 */
@Composable
fun HerramientasContent(
    uiState: HerramientasUiState,
    onSubherramientaSeleccionada: (Subherramienta) -> Unit,
    onInfo: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    precios: @Composable () -> Unit,
    chapas: @Composable () -> Unit,
) {
    JewelryScaffold(
        onInfo = onInfo,
        modifier = modifier,
        title = stringResource(R.string.modulo_herramientas_titulo),
        onBack = onBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(JewelrySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Md),
        ) {
            // Dorado por defecto: es el lenguaje de acción de la app, no el acento de un módulo.
            SelectorSegmentado(
                opciones = Subherramienta.entries.map {
                    OpcionSegmento(etiqueta = stringResource(it.etiquetaRes), iconRes = it.iconRes)
                },
                // En la primera visita no hay sub-herramienta: -1 no marca ningún segmento.
                seleccionada = uiState.subherramienta?.ordinal ?: -1,
                onSeleccion = { onSubherramientaSeleccionada(Subherramienta.entries[it]) },
            )

            when (uiState.subherramienta) {
                null -> TarjetaPrimeraVisita()
                Subherramienta.PRECIOS -> precios()
                Subherramienta.CHAPAS -> chapas()
            }
        }
    }
}

@Composable
private fun TarjetaPrimeraVisita(modifier: Modifier = Modifier) {
    TarjetaAcento(modifier, acento = JewelryColors.TealPrimary) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.modulo_herramientas),
                contentDescription = stringResource(R.string.modulo_herramientas_imagen),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(96.dp),
            )
            Spacer(Modifier.width(JewelrySpacing.Md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.herramientas_primera_visita_titulo),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    color = JewelryColors.TealPrimary,
                )
                Spacer(Modifier.height(JewelrySpacing.Xs))
                Text(
                    text = stringResource(R.string.herramientas_primera_visita_texto),
                    style = MaterialTheme.typography.bodyMedium,
                    color = JewelryColors.TextSecondary,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun HerramientasContentPrimeraVisitaPreview() {
    Calculadoradejoyeros2021Theme {
        HerramientasContent(
            uiState = HerramientasUiState(),
            onSubherramientaSeleccionada = {},
            onInfo = {},
            onBack = {},
            precios = {},
            chapas = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun HerramientasContentConSeccionPreview() {
    Calculadoradejoyeros2021Theme {
        HerramientasContent(
            uiState = HerramientasUiState(subherramienta = Subherramienta.PRECIOS),
            onSubherramientaSeleccionada = {},
            onInfo = {},
            onBack = {},
            precios = { Text("Sección de precios", color = JewelryColors.TextPrimary) },
            chapas = {},
        )
    }
}
