package com.jrblanco.calculadoradejoyeros2021.ui.placeholder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.components.JewelryScaffold
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing
import org.koin.androidx.compose.koinViewModel

/**
 * Pantalla de andamiaje: muestra el nombre de su destino y el armazón compartido.
 *
 * Es un único composable parametrizado y no siete ficheros casi idénticos. Cuando un
 * destino reciba su feature real, solo cambia su cableado en el grafo de navegación.
 *
 * [analyticsName] es un identificador estable para telemetría, distinto de [title],
 * que es texto traducible y no serviría para comparar datos entre idiomas.
 */
@Composable
fun PlaceholderScreen(
    title: String,
    analyticsName: String,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    viewModel: PlaceholderViewModel = koinViewModel(),
) {
    LaunchedEffect(analyticsName) { viewModel.onScreenShown(analyticsName) }

    PlaceholderContent(
        title = title,
        onInfo = onInfo,
        modifier = modifier,
        onBack = onBack,
        bottomBar = bottomBar,
    )
}

@Composable
fun PlaceholderContent(
    title: String,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
) {
    JewelryScaffold(
        onInfo = onInfo,
        modifier = modifier,
        title = title,
        onBack = onBack,
        bottomBar = bottomBar,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(JewelrySpacing.Xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = JewelryColors.GoldPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.placeholder_pendiente),
                style = MaterialTheme.typography.bodyMedium,
                color = JewelryColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = JewelrySpacing.Sm),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun PlaceholderContentPreview() {
    Calculadoradejoyeros2021Theme {
        PlaceholderContent(title = "Aleaciones de ORO", onInfo = {}, onBack = {})
    }
}
