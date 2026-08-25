package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.precios

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

/**
 * La sección de precios con estado: resuelve su ViewModel al componerse por primera vez —es
 * entonces cuando se consulta al proveedor— y lo conserva mientras el joyero siga en la
 * pantalla de Herramientas (el dueño del ViewModel es la entrada de navegación de la ruta).
 */
@Composable
fun PreciosMetalesSection(
    modifier: Modifier = Modifier,
    viewModel: PreciosMetalesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PreciosMetalesContent(
        uiState = uiState,
        onUnidadSeleccionada = viewModel::onUnidadSeleccionada,
        onMetalSeleccionado = viewModel::onMetalSeleccionado,
        onReintentar = viewModel::onReintentar,
        modifier = modifier,
    )
}
