package com.jrblanco.calculadoradejoyeros2021.ui.favoritos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.components.JewelryBottomBar
import com.jrblanco.calculadoradejoyeros2021.ui.components.JewelryScaffold
import com.jrblanco.calculadoradejoyeros2021.ui.components.MainTab
import com.jrblanco.calculadoradejoyeros2021.ui.components.TarjetaAcento
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryRadius
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySize
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

/**
 * La pestaña de Favoritos: el listado de cálculos guardados.
 *
 * Sustituye al último `PlaceholderScreen` de la app. Conserva el nombre de pantalla `"favoritos"` en
 * telemetría, que es el que emitía el andamio, para no romper la serie histórica.
 */
@Composable
fun FavoritosScreen(
    onAbrirFavorito: (FavoritoUiModel) -> Unit,
    onTabSelect: (MainTab) -> Unit,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritosViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FavoritosContent(
        uiState = uiState,
        onAbrir = { favorito ->
            viewModel.onFavoritoPulsado(favorito)
            onAbrirFavorito(favorito)
        },
        onQuitar = viewModel::onQuitarPulsado,
        onCancelarBorrado = viewModel::onCancelarBorrado,
        onConfirmarBorrado = viewModel::onConfirmarBorrado,
        onTabSelect = onTabSelect,
        onInfo = onInfo,
        modifier = modifier,
    )
}

@Composable
fun FavoritosContent(
    uiState: FavoritosUiState,
    onAbrir: (FavoritoUiModel) -> Unit,
    onQuitar: (FavoritoUiModel) -> Unit,
    onCancelarBorrado: () -> Unit,
    onConfirmarBorrado: () -> Unit,
    onTabSelect: (MainTab) -> Unit,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    JewelryScaffold(
        onInfo = onInfo,
        modifier = modifier,
        // Zona principal: título y barra inferior, sin flecha de retroceso.
        title = stringResource(R.string.nav_favoritos),
        bottomBar = {
            JewelryBottomBar(selected = MainTab.FAVORITOS, onSelect = onTabSelect)
        },
    ) {
        // Mientras no se sabe si hay favoritos no se pinta nada: la primera emisión del flujo llega
        // un fotograma después de componer, y sin esta guarda la invitación parpadearía en cada
        // visita. Un fotograma vacío es invisible; una tarjeta que aparece y desaparece, no.
        if (uiState.cargando) return@JewelryScaffold

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(JewelrySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Md),
        ) {
            if (uiState.favoritos.isEmpty()) {
                item { TarjetaSinFavoritos() }
            } else {
                items(uiState.favoritos, key = { it.id }) { favorito ->
                    TarjetaFavorito(
                        favorito = favorito,
                        onAbrir = { onAbrir(favorito) },
                        onQuitar = { onQuitar(favorito) },
                    )
                }
            }
        }

        uiState.pendienteDeBorrar?.let { favorito ->
            DialogoConfirmacion(
                titulo = stringResource(R.string.favoritos_borrar_titulo),
                mensaje = stringResource(R.string.favoritos_borrar_mensaje, tituloDe(favorito.entradas)),
                textoConfirmar = stringResource(R.string.favoritos_borrar_confirmar),
                onConfirmar = onConfirmarBorrado,
                onCancelar = onCancelarBorrado,
            )
        }
    }
}

/**
 * La primera visita, al estilo de la de Herramientas: en vez de un listado vacío sin explicación,
 * una invitación que nombra el botón con el que se guarda — y lo nombra con su propia cadena, no
 * repitiéndola, para que si cambia la etiqueta del botón la frase siga cuadrando.
 */
@Composable
private fun TarjetaSinFavoritos(modifier: Modifier = Modifier) {
    TarjetaAcento(modifier = modifier, acento = JewelryColors.GoldPrimary) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .border(1.dp, JewelryColors.GoldPrimary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_favoritos),
                    contentDescription = null,
                    tint = JewelryColors.GoldPrimary,
                    modifier = Modifier.size(44.dp),
                )
            }
            Spacer(Modifier.width(JewelrySpacing.Md))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.favoritos_vacio_titulo),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    color = JewelryColors.GoldPrimary,
                )
                Spacer(Modifier.height(JewelrySpacing.Xs))
                Text(
                    text = stringResource(
                        R.string.favoritos_vacio_texto,
                        stringResource(R.string.accion_guardar_favoritos),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = JewelryColors.TextSecondary,
                )
            }
        }
    }
}

/**
 * El primer diálogo de la app.
 *
 * Se hace con `Dialog` de compose-ui y una tarjeta propia, no con `AlertDialog` de Material 3: aquél
 * impone padding, ancho máximo, forma, elevación tonal y `TextButton` para las acciones — que sería
 * el primer botón de Material de la app, justo lo que `BotonDorado` existe para evitar. Es la cuarta
 * vez que el proyecto coge el comportamiento de plataforma y dibuja los píxeles, después de
 * `NavigationBar`, `Button` y `SegmentedButton`.
 *
 * De Material 3 se copia a mano lo único que valía la pena: el `paneTitle`, para que el lector de
 * pantalla anuncie el panel al aparecer. Los `CompositionLocal` de `ProveedorIdioma` propagan a la
 * subcomposición del diálogo, así que sale en el idioma elegido sin hacer nada.
 *
 * Privado de este fichero, como `FilaIdioma` en Ajustes: sube a `ui/components/` el día que lo pida
 * un segundo consumidor. La firma ya es genérica para que ese día sea un corta y pega.
 */
@Composable
private fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    acento: Color = JewelryColors.Danger,
) {
    Dialog(
        onDismissRequest = onCancelar,
        // La geometría es nuestra: con el ancho por defecto de la plataforma, no lo sería.
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(Modifier.fillMaxWidth().padding(JewelrySpacing.Xl)) {
            TarjetaAcento(
                modifier = Modifier.semantics { paneTitle = titulo },
                acento = acento,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_aviso),
                        contentDescription = null,
                        tint = acento,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(JewelrySpacing.Sm))
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        color = JewelryColors.TextPrimary,
                    )
                }
                Spacer(Modifier.height(JewelrySpacing.Sm))
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JewelryColors.TextSecondary,
                )
                Spacer(Modifier.height(JewelrySpacing.Lg))
                Row(horizontalArrangement = Arrangement.spacedBy(JewelrySpacing.Md)) {
                    BotonPlano(
                        texto = stringResource(R.string.accion_cancelar),
                        onClick = onCancelar,
                        modifier = Modifier.weight(1f),
                    )
                    BotonPlano(
                        texto = textoConfirmar,
                        onClick = onConfirmar,
                        modifier = Modifier.weight(1f),
                        fondo = acento.copy(alpha = 0.18f),
                        borde = acento,
                        colorTexto = acento,
                    )
                }
            }
        }
    }
}

/**
 * Botón de diálogo. **`BotonDorado` no sirve** para ninguno de los dos: está documentado que el
 * dorado es el lenguaje de acción principal de la app, y un «Quitar» destructivo en dorado miente.
 *
 * `BasicText` + `TextAutoSize` a una línea por lo mismo que `BotonDorado`: dos botones a `weight(1f)`
 * con «Abbrechen» y «Entfernen» es exactamente el caso que obligó a introducir el auto-ajuste en la
 * feature 008.
 */
@Composable
private fun BotonPlano(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fondo: Color = Color.Transparent,
    borde: Color = JewelryColors.Border,
    colorTexto: Color = JewelryColors.TextSecondary,
) {
    val forma = RoundedCornerShape(JewelryRadius.Medium)
    Box(
        modifier = modifier
            .heightIn(min = JewelrySize.MinTouchTarget)
            .background(fondo, forma)
            .border(1.dp, borde, forma)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = JewelrySpacing.Md, vertical = JewelrySpacing.Sm),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = texto,
            style = TextStyle(
                color = colorTexto,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = 14.sp),
        )
    }
}

// --- Previews ---

private val ORO_DE_MUESTRA = FavoritoUiModel(
    id = 1L,
    entradas = EntradasFavoritoUi.Oro(
        ley = com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro.LEY_18K,
        color = com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro.BLANCO,
        cantidad = "30",
    ),
    lineas = listOf(
        LineaFavoritoUi(ConceptoFavorito.PLATA_FINA, "6,564"),
        LineaFavoritoUi(ConceptoFavorito.COBRE, "3,382"),
        LineaFavoritoUi(ConceptoFavorito.PALADIO, "3,382"),
    ),
    totalFormateado = "39,960",
    guardadoEnEpochMillis = 1_787_670_000_000L,
)

private val BASE_DE_MUESTRA = FavoritoUiModel(
    id = 2L,
    entradas = EntradasFavoritoUi.SoldaduraBase(
        modo = com.jrblanco.calculadoradejoyeros2021.domain.model.ModoEntradaSoldadura.PESO_FINAL,
        cantidad = "10",
    ),
    lineas = listOf(
        LineaFavoritoUi(ConceptoFavorito.ORO_24K, "7,541"),
        LineaFavoritoUi(ConceptoFavorito.COBRE, "0,407"),
        LineaFavoritoUi(ConceptoFavorito.PLATA_FINA, "0,603"),
        LineaFavoritoUi(ConceptoFavorito.ZINC, "0,694"),
        LineaFavoritoUi(ConceptoFavorito.CADMIO, "0,754"),
    ),
    totalFormateado = "10,000",
    guardadoEnEpochMillis = 1_787_500_000_000L,
)

private val CHAPA_DE_MUESTRA = FavoritoUiModel(
    id = 3L,
    entradas = EntradasFavoritoUi.Chapa(
        material = com.jrblanco.calculadoradejoyeros2021.domain.model.MaterialChapa.ORO_18K,
        ancho = "10",
        largo = "20",
        espesor = "0,5",
    ),
    lineas = listOf(
        LineaFavoritoUi(ConceptoFavorito.VOLUMEN, "0,100"),
        LineaFavoritoUi(ConceptoFavorito.METAL_FINO, "1,169"),
    ),
    totalFormateado = "1,56",
    guardadoEnEpochMillis = 1_787_400_000_000L,
)

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun FavoritosContentPreview() {
    Calculadoradejoyeros2021Theme {
        FavoritosContent(
            uiState = FavoritosUiState(
                cargando = false,
                favoritos = listOf(ORO_DE_MUESTRA, BASE_DE_MUESTRA, CHAPA_DE_MUESTRA),
            ),
            onAbrir = {},
            onQuitar = {},
            onCancelarBorrado = {},
            onConfirmarBorrado = {},
            onTabSelect = {},
            onInfo = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun FavoritosContentVacioPreview() {
    Calculadoradejoyeros2021Theme {
        FavoritosContent(
            uiState = FavoritosUiState(cargando = false),
            onAbrir = {},
            onQuitar = {},
            onCancelarBorrado = {},
            onConfirmarBorrado = {},
            onTabSelect = {},
            onInfo = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun FavoritosContentCargandoPreview() {
    Calculadoradejoyeros2021Theme {
        FavoritosContent(
            uiState = FavoritosUiState(cargando = true),
            onAbrir = {},
            onQuitar = {},
            onCancelarBorrado = {},
            onConfirmarBorrado = {},
            onTabSelect = {},
            onInfo = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun FavoritosContentConDialogoPreview() {
    Calculadoradejoyeros2021Theme {
        FavoritosContent(
            uiState = FavoritosUiState(
                cargando = false,
                favoritos = listOf(ORO_DE_MUESTRA, BASE_DE_MUESTRA),
                pendienteDeBorrar = ORO_DE_MUESTRA,
            ),
            onAbrir = {},
            onQuitar = {},
            onCancelarBorrado = {},
            onConfirmarBorrado = {},
            onTabSelect = {},
            onInfo = {},
        )
    }
}
