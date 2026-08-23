package com.jrblanco.calculadoradejoyeros2021.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.components.DiamondDivider
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryRadius
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySize
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing
import com.jrblanco.calculadoradejoyeros2021.ui.theme.TitleSerif
import org.koin.androidx.compose.koinViewModel

/**
 * Portada de la app. Resuelve el ViewModel y delega el pintado en [WelcomeContent].
 */
@Composable
fun WelcomeScreen(
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WelcomeViewModel = koinViewModel(),
) {
    WelcomeContent(
        onStart = {
            viewModel.onStartClicked()
            onStart()
        },
        modifier = modifier,
    )
}

@Composable
fun WelcomeContent(
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JewelryColors.SplashBackground),
    ) {
        // El fondo es proporcionalmente más ancho que un móvil actual, así que Crop
        // recorta por los lados. El contenido del mockup está centrado, con lo que el
        // recorte cae sobre zona vacía.
        Image(
            painter = painterResource(R.drawable.fondo_taller),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = JewelrySpacing.Xl)
                .padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.logo_calculadora),
                contentDescription = stringResource(R.string.welcome_logo_description),
                modifier = Modifier.size(230.dp),
            )

            Spacer(Modifier.height(JewelrySpacing.Xl))

            GoldTitle(text = stringResource(R.string.welcome_title))

            Spacer(Modifier.height(JewelrySpacing.Lg))

            DiamondDivider()

            Spacer(Modifier.height(JewelrySpacing.Lg))

            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = JewelryColors.TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(JewelrySpacing.Xxl))

            StartButton(onClick = onStart)
        }

        Text(
            text = stringResource(R.string.welcome_developer),
            style = MaterialTheme.typography.labelMedium,
            color = JewelryColors.TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = JewelrySpacing.Xl, start = JewelrySpacing.Xl, end = JewelrySpacing.Xl),
        )
    }
}

/** Título de portada con degradado dorado y sombra suave, como el mockup. */
@Composable
private fun GoldTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = TitleSerif.copy(
            brush = Brush.verticalGradient(
                colors = listOf(
                    JewelryColors.GoldSoft,
                    JewelryColors.GoldPrimary,
                    JewelryColors.GoldSecondary,
                ),
            ),
            shadow = Shadow(
                color = JewelryColors.Background,
                offset = Offset(0f, 3f),
                blurRadius = 6f,
            ),
        ),
        textAlign = TextAlign.Center,
        modifier = modifier.semantics { heading() },
    )
}

/**
 * Botón principal: pill oscuro translúcido con borde dorado y texto dorado.
 *
 * Se construye a mano en vez de con `Button` de Material porque el relleno debe dejar
 * ver el fondo, y `Button` impone un color opaco de contenedor.
 */
@Composable
private fun StartButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(JewelryRadius.Pill)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(280.dp)
            .height(JewelrySize.PrimaryButtonHeight)
            .background(JewelryColors.Surface.copy(alpha = 0.72f), shape)
            .border(2.dp, JewelryColors.GoldPrimary, shape)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Text(
            text = stringResource(R.string.welcome_start),
            style = MaterialTheme.typography.titleMedium,
            color = JewelryColors.GoldPrimary,
        )
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun WelcomeContentPreview() {
    Calculadoradejoyeros2021Theme {
        WelcomeContent(onStart = {})
    }
}
