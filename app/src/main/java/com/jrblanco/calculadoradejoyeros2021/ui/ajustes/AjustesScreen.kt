package com.jrblanco.calculadoradejoyeros2021.ui.ajustes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import com.jrblanco.calculadoradejoyeros2021.ui.components.CabeceraSeccion
import com.jrblanco.calculadoradejoyeros2021.ui.components.JewelryBottomBar
import com.jrblanco.calculadoradejoyeros2021.ui.components.JewelryScaffold
import com.jrblanco.calculadoradejoyeros2021.ui.components.MainTab
import com.jrblanco.calculadoradejoyeros2021.ui.components.TarjetaAcento
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryRadius
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySize
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing
import org.koin.androidx.compose.koinViewModel

/**
 * Ajustes. Resuelve el ViewModel y delega el pintado en [AjustesContent].
 *
 * El idioma elegido aquí lo aplica `ui/idioma/ProveedorIdioma` desde la raíz de la app: esta
 * pantalla solo guarda la elección, y se repinta con las demás.
 */
@Composable
fun AjustesScreen(
    onTabSelect: (MainTab) -> Unit,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AjustesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AjustesContent(
        uiState = uiState,
        onIdiomaSeleccionado = viewModel::onIdiomaSeleccionado,
        onAutomaticoSeleccionado = viewModel::onAutomaticoSeleccionado,
        onTabSelect = onTabSelect,
        onInfo = onInfo,
        modifier = modifier,
    )
}

@Composable
fun AjustesContent(
    uiState: AjustesUiState,
    onIdiomaSeleccionado: (IdiomaApp) -> Unit,
    onAutomaticoSeleccionado: () -> Unit,
    onTabSelect: (MainTab) -> Unit,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    JewelryScaffold(
        onInfo = onInfo,
        modifier = modifier,
        // Zona principal: título y barra inferior, sin flecha de retroceso.
        title = stringResource(R.string.nav_ajustes),
        bottomBar = {
            JewelryBottomBar(selected = MainTab.AJUSTES, onSelect = onTabSelect)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(JewelrySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Md),
        ) {
            CabeceraSeccion(
                iconRes = R.drawable.ic_idioma,
                titulo = stringResource(R.string.ajustes_seccion_idioma),
            )

            Text(
                text = stringResource(R.string.ajustes_idioma_descripcion),
                style = MaterialTheme.typography.bodyMedium,
                color = JewelryColors.TextSecondary,
            )

            TarjetaAcento {
                Column(
                    // Un solo grupo de selección: una y solo una fila activa entre las seis.
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Xs),
                ) {
                    FilaIdioma(
                        nombre = stringResource(R.string.ajustes_idioma_automatico),
                        detalle = stringResource(
                            R.string.ajustes_idioma_automatico_detalle,
                            stringResource(uiState.sistema.nombreRes),
                        ),
                        activa = uiState.elegido == null,
                        onClick = onAutomaticoSeleccionado,
                    )

                    IdiomaApp.entries.forEach { idioma ->
                        FilaIdioma(
                            nombre = stringResource(idioma.nombreRes),
                            banderaRes = idioma.banderaRes,
                            activa = uiState.elegido == idioma,
                            onClick = { onIdiomaSeleccionado(idioma) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Una fila del selector de idioma: bandera, nombre y el check dorado si está activa.
 *
 * Privada del fichero, como manda la regla del proyecto: sube a `ui/components/` el día que un
 * segundo consumidor la pida. Sin [banderaRes] pinta el globo, que es la fila «Automático».
 */
@Composable
private fun FilaIdioma(
    nombre: String,
    activa: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    banderaRes: Int? = null,
    detalle: String? = null,
) {
    val forma = RoundedCornerShape(JewelryRadius.Small)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = JewelrySize.MinTouchTarget)
            .clip(forma)
            .background(if (activa) JewelryColors.GoldPrimary.copy(alpha = 0.14f) else JewelryColors.Surface)
            .selectable(
                selected = activa,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(horizontal = JewelrySpacing.Sm, vertical = JewelrySpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (banderaRes != null) {
            Image(
                painter = painterResource(banderaRes),
                // El nombre del idioma va justo al lado: repetirlo aquí sería ruido para el
                // lector de pantalla.
                contentDescription = null,
                modifier = Modifier
                    .size(width = 32.dp, height = 21.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .border(1.dp, JewelryColors.Border, RoundedCornerShape(3.dp)),
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_idioma),
                contentDescription = null,
                tint = JewelryColors.GoldPrimary,
                modifier = Modifier.size(width = 32.dp, height = 21.dp),
            )
        }

        Spacer(Modifier.width(JewelrySpacing.Md))

        Column(Modifier.weight(1f)) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyLarge,
                color = if (activa) JewelryColors.GoldPrimary else JewelryColors.TextPrimary,
            )
            if (detalle != null) {
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.labelMedium,
                    color = JewelryColors.TextMuted,
                )
            }
        }

        if (activa) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = stringResource(R.string.ajustes_idioma_activo),
                tint = JewelryColors.GoldPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun AjustesContentAutomaticoPreview() {
    Calculadoradejoyeros2021Theme {
        AjustesContent(
            uiState = AjustesUiState(elegido = null, sistema = IdiomaApp.ESPANOL),
            onIdiomaSeleccionado = {},
            onAutomaticoSeleccionado = {},
            onTabSelect = {},
            onInfo = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun AjustesContentItalianoPreview() {
    Calculadoradejoyeros2021Theme {
        AjustesContent(
            uiState = AjustesUiState(elegido = IdiomaApp.ITALIANO, sistema = IdiomaApp.ESPANOL),
            onIdiomaSeleccionado = {},
            onAutomaticoSeleccionado = {},
            onTabSelect = {},
            onInfo = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun AjustesContentAlemanPreview() {
    Calculadoradejoyeros2021Theme {
        AjustesContent(
            // El dispositivo en inglés y el joyero eligió alemán: la elección manda.
            uiState = AjustesUiState(elegido = IdiomaApp.ALEMAN, sistema = IdiomaApp.INGLES),
            onIdiomaSeleccionado = {},
            onAutomaticoSeleccionado = {},
            onTabSelect = {},
            onInfo = {},
        )
    }
}
