package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrblanco.calculadoradejoyeros2021.R
import org.koin.androidx.compose.koinViewModel

/**
 * La sección de chapas con estado: resuelve su ViewModel al componerse por primera vez y lo
 * conserva mientras el joyero siga en Herramientas. El aviso de «Próximamente» de favoritos lo
 * lanza la vista: el ViewModel no conoce Android.
 */
@Composable
fun PesoChapasSection(
    modifier: Modifier = Modifier,
    viewModel: PesoChapasViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    PesoChapasContent(
        uiState = uiState,
        onFamiliaSeleccionada = viewModel::onFamiliaSeleccionada,
        onMaterialSeleccionado = viewModel::onMaterialSeleccionado,
        onMedidaCambiada = viewModel::onMedidaCambiada,
        onLimpiar = viewModel::onLimpiar,
        onGuardarFavoritos = {
            viewModel.onGuardarFavoritos()
            Toast.makeText(context, R.string.aviso_proximamente, Toast.LENGTH_SHORT).show()
        },
        modifier = modifier,
    )
}
