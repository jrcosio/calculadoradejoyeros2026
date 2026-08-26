package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrblanco.calculadoradejoyeros2021.ui.favoritos.mensajeRes
import org.koin.androidx.compose.koinViewModel

/**
 * La sección de chapas con estado: resuelve su ViewModel al componerse por primera vez y lo
 * conserva mientras el joyero siga en Herramientas. El aviso de favoritos lo lanza la vista, porque
 * el ViewModel no conoce Android.
 *
 * [favoritoId] llega por el *slot* de Herramientas y no por la firma de `HerramientasContent`: así
 * el armazón no cambia de contrato y su test instrumentado no se toca.
 */
@Composable
fun PesoChapasSection(
    modifier: Modifier = Modifier,
    favoritoId: Long? = null,
    viewModel: PesoChapasViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Aviso efímero del sistema: los Toast se reemplazan solos. El ViewModel decide qué decir; la
    // vista lo muestra y lo consume, para que dos guardados seguidos vuelvan a avisar.
    val aviso = uiState.avisoFavorito
    LaunchedEffect(aviso) {
        if (aviso != null) {
            Toast.makeText(context, aviso.mensajeRes, Toast.LENGTH_SHORT).show()
            viewModel.onAvisoFavoritoMostrado()
        }
    }

    LaunchedEffect(favoritoId) {
        favoritoId?.let(viewModel::cargarFavorito)
    }

    PesoChapasContent(
        uiState = uiState,
        onFamiliaSeleccionada = viewModel::onFamiliaSeleccionada,
        onMaterialSeleccionado = viewModel::onMaterialSeleccionado,
        onMedidaCambiada = viewModel::onMedidaCambiada,
        onLimpiar = viewModel::onLimpiar,
        onGuardarFavoritos = viewModel::onGuardarFavoritos,
        modifier = modifier,
    )
}
