package com.jrblanco.calculadoradejoyeros2021.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors

/**
 * Armazón común: barra superior arriba, contenido en medio y barra inferior opcional.
 *
 * Cada pantalla declara aquí su propio *chrome* en lugar de deducirlo husmeando la ruta
 * actual desde el `NavHost`. Es más explícito y evita que las barras aparezcan un
 * instante en la portada, que no lleva ninguna de las dos.
 */
@Composable
fun JewelryScaffold(
    onInfo: (() -> Unit)?,
    modifier: Modifier = Modifier,
    title: String? = null,
    onBack: (() -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JewelryColors.Background),
    ) {
        JewelryTopBar(onInfo = onInfo, title = title, onBack = onBack)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            content = content,
        )

        bottomBar?.invoke()
    }
}
