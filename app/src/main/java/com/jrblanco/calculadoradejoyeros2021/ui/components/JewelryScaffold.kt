package com.jrblanco.calculadoradejoyeros2021.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors

/**
 * Armazón común: barra superior arriba, contenido en medio y barra inferior opcional.
 *
 * Cada pantalla declara aquí su propio *chrome* en lugar de deducirlo husmeando la ruta
 * actual desde el `NavHost`. Es más explícito y evita que las barras aparezcan un
 * instante en la portada, que no lleva ninguna de las dos.
 *
 * La app es edge-to-edge y los insets del sistema se reparten así: la barra superior
 * consume la barra de estado (`statusBarsPadding`), [JewelryBottomBar] consume la de
 * navegación (`navigationBarsPadding`), y cuando una pantalla **no** lleva barra
 * inferior es este scaffold quien reserva ese hueco — el equivalente Compose del
 * `SafeArea` de Flutter. Sin esto, los tres botones de Android caen encima del
 * contenido en los móviles con navegación clásica.
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
                .weight(1f)
                .then(
                    if (bottomBar == null) {
                        // Sin barra inferior, el contenido asume el hueco de la barra
                        // de navegación del sistema (3 botones o gesto). Con barra, la
                        // propia JewelryBottomBar ya lo consume. Compose descuenta lo
                        // consumido, así que los imePadding() interiores no se duplican.
                        Modifier.windowInsetsPadding(
                            WindowInsets.navigationBars.only(WindowInsetsSides.Bottom),
                        )
                    } else {
                        Modifier
                    },
                ),
            content = content,
        )

        bottomBar?.invoke()
    }
}
