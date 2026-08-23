package com.jrblanco.calculadoradejoyeros2021.ui.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrblanco.calculadoradejoyeros2021.BuildConfig
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.components.DiamondDivider
import com.jrblanco.calculadoradejoyeros2021.ui.components.JewelryScaffold
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryRadius
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing
import org.koin.androidx.compose.koinViewModel

/**
 * Pantalla de información. Resuelve el ViewModel y delega el pintado en [InfoContent].
 */
@Composable
fun InfoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InfoViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    // Al volver de LinkedIn o de Instagram hay que bajar la guarda de FR-017; si no, el
    // acceso quedaría muerto durante el resto de la vida de la pantalla.
    LifecycleResumeEffect(Unit) {
        viewModel.onPantallaVisible()
        onPauseOrDispose { }
    }

    InfoContent(
        uiState = uiState,
        onEnlaceClick = { enlace ->
            if (viewModel.onEnlacePulsado(enlace)) {
                // `AndroidUriHandler` lanza `IllegalArgumentException` cuando no hay
                // ninguna actividad capaz de atender el enlace; sin capturarla la app se
                // cerraría en un móvil sin navegador.
                runCatching { uriHandler.openUri(enlace.url) }
                    .onFailure(viewModel::onEnlaceFallido)
            }
        },
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun InfoContent(
    uiState: InfoUiState,
    onEnlaceClick: (InfoEnlace) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    // Entra como parámetro con valor por defecto y no inyectada: un `String` en el grafo
    // de Koin debilitaría `KoinModulesTest`, y así la preview y el test la fijan a mano.
    versionName: String = BuildConfig.VERSION_NAME,
) {
    JewelryScaffold(
        // Sin acceso a información: ya estamos en ella (FR-018).
        onInfo = null,
        modifier = modifier,
        onBack = onBack,
    ) {
        // El mismo fondo de taller de la portada, apenas insinuado: da la textura del
        // mockup sin restar contraste a las tarjetas que van encima.
        Image(
            painter = painterResource(R.drawable.fondo_taller),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.30f,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(JewelrySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Md),
        ) {
            Titulo()

            PerfilCard()

            uiState.enlaces.forEach { enlace ->
                // Cada acceso va detrás de la tarjeta de quien lo firma: LinkedIn tras el
                // perfil del autor e Instagram tras la mención a la joyería, como en el
                // mockup.
                if (enlace == InfoEnlace.INSTAGRAM) {
                    BlancoJoyerosCard()
                }
                EnlaceCard(enlace = enlace, onClick = { onEnlaceClick(enlace) })
            }

            Text(
                text = stringResource(R.string.info_version, versionName),
                style = MaterialTheme.typography.labelMedium,
                color = JewelryColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = JewelrySpacing.Sm, bottom = JewelrySpacing.Lg),
            )
        }
    }
}

/**
 * Título de la pantalla.
 *
 * En Manrope y no en el serif del mockup: `Type.kt` reserva Playfair Display a la portada.
 * El degradado dorado es lo que conserva el aire de la referencia.
 */
@Composable
private fun Titulo(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.info_titulo),
        style = MaterialTheme.typography.displayLarge.copy(
            brush = Brush.verticalGradient(
                colors = listOf(
                    JewelryColors.GoldSoft,
                    JewelryColors.GoldPrimary,
                    JewelryColors.GoldSecondary,
                ),
            ),
        ),
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = JewelrySpacing.Sm)
            .semantics { heading() },
    )
}

/** Quién ha hecho la app: foto, nombre, propósito y perfil profesional. */
@Composable
private fun PerfilCard(modifier: Modifier = Modifier) {
    TarjetaDorada(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.foto_jrblanco),
                contentDescription = stringResource(R.string.info_perfil_foto),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .border(2.dp, JewelryColors.BorderGold, CircleShape),
            )

            Spacer(Modifier.width(JewelrySpacing.Md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.info_perfil_nombre),
                    style = MaterialTheme.typography.titleMedium,
                    color = JewelryColors.GoldPrimary,
                )
                DiamondDivider(
                    modifier = Modifier.padding(vertical = JewelrySpacing.Sm),
                    widthFraction = 1f,
                )
                Text(
                    text = stringResource(R.string.info_perfil_descripcion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = JewelryColors.TextSecondary,
                )
            }
        }

        Spacer(Modifier.height(JewelrySpacing.Md))

        Text(
            text = stringResource(R.string.info_perfil_etiquetas),
            style = MaterialTheme.typography.labelMedium.copy(lineHeight = 22.sp),
            color = JewelryColors.GoldSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Mención a la joyería. Es informativa: no se puede pulsar y no lo aparenta. */
@Composable
private fun BlancoJoyerosCard(modifier: Modifier = Modifier) {
    TarjetaDorada(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.logo_blanco_joyeros),
                contentDescription = stringResource(R.string.info_blanco_joyeros_logo),
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .border(1.dp, JewelryColors.BorderGold, CircleShape),
            )

            Spacer(Modifier.width(JewelrySpacing.Md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.info_blanco_joyeros_titulo),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = JewelryColors.GoldPrimary,
                )
                DiamondDivider(
                    modifier = Modifier.padding(vertical = JewelrySpacing.Sm),
                    widthFraction = 1f,
                )
                Text(
                    text = stringResource(R.string.info_blanco_joyeros_descripcion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = JewelryColors.TextSecondary,
                )
            }

            Spacer(Modifier.width(JewelrySpacing.Sm))

            Image(
                painter = painterResource(R.drawable.joya_lupa),
                contentDescription = stringResource(R.string.info_blanco_joyeros_imagen),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(72.dp),
            )
        }
    }
}

/**
 * Acceso a un perfil externo.
 *
 * La tarjeta entera es el objetivo táctil y se anuncia como botón con la etiqueta de
 * acción; el icono de la derecha va sin descripción para no repetir el anuncio, igual que
 * el chevron de `ModuleCard`.
 */
@Composable
private fun EnlaceCard(
    enlace: InfoEnlace,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val presentacion = enlace.presentation()
    val acento = presentacion.acento
    val shape = RoundedCornerShape(JewelryRadius.Large)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(acento.copy(alpha = 0.16f), JewelryColors.Surface, JewelryColors.Surface),
                ),
                shape,
            )
            .border(1.dp, acento.copy(alpha = 0.65f), shape)
            .clickable(
                onClickLabel = stringResource(R.string.info_enlace_abrir),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(JewelrySpacing.Md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .border(1.dp, acento.copy(alpha = 0.8f), RoundedCornerShape(JewelryRadius.Small)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(presentacion.iconRes),
                contentDescription = null,
                tint = acento,
                modifier = Modifier.size(32.dp),
            )
        }

        Spacer(Modifier.width(JewelrySpacing.Md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(presentacion.tituloRes),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = acento,
            )
            Text(
                text = enlace.urlVisible(),
                style = MaterialTheme.typography.bodyMedium,
                color = JewelryColors.TextSecondary,
            )
        }

        Spacer(Modifier.width(JewelrySpacing.Sm))

        Box(
            modifier = Modifier
                .size(44.dp)
                .border(1.dp, acento.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_enlace_externo),
                contentDescription = null,
                tint = acento,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Cómo se pinta cada acceso.
 *
 * Vive aquí y no en [InfoEnlace] para que el enum siga libre de Android y su ViewModel se
 * pueda testear en la JVM, igual que `HomeModule`.
 */
private data class EnlacePresentation(
    val iconRes: Int,
    val tituloRes: Int,
    val acento: Color,
)

private fun InfoEnlace.presentation(): EnlacePresentation = when (this) {
    InfoEnlace.LINKEDIN -> EnlacePresentation(
        iconRes = R.drawable.ic_linkedin,
        tituloRes = R.string.info_linkedin_titulo,
        acento = AcentoLinkedIn,
    )
    InfoEnlace.INSTAGRAM -> EnlacePresentation(
        iconRes = R.drawable.ic_instagram,
        tituloRes = R.string.info_instagram_titulo,
        acento = AcentoInstagram,
    )
}

/** La dirección tal y como se lee en el mockup: sin esquema y sin `www`. */
private fun InfoEnlace.urlVisible(): String = url.removePrefix("https://www.")

/**
 * Colores de marca de terceros, aclarados para que contrasten sobre `Background`. No
 * entran en `JewelryColors`: no son tokens del sistema de diseño, son de LinkedIn y de
 * Instagram.
 */
private val AcentoLinkedIn = Color(0xFF3BA5E0)
private val AcentoInstagram = Color(0xFFE8497F)

/**
 * Envoltorio de tarjeta con el lenguaje visual de `ModuleCard`: esquina grande, degradado
 * que arranca del acento y se apaga, y filete del acento.
 */
@Composable
private fun TarjetaDorada(
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(JewelryRadius.Large)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        JewelryColors.GoldPrimary.copy(alpha = 0.14f),
                        JewelryColors.Surface,
                        JewelryColors.Surface,
                    ),
                ),
                shape,
            )
            .border(1.dp, JewelryColors.GoldPrimary.copy(alpha = 0.65f), shape)
            .padding(JewelrySpacing.Md),
        content = contenido,
    )
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun InfoContentPreview() {
    Calculadoradejoyeros2021Theme {
        InfoContent(
            uiState = InfoUiState(enlaces = InfoEnlace.entries),
            onEnlaceClick = {},
            onBack = {},
        )
    }
}
