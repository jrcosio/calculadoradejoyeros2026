package com.jrblanco.calculadoradejoyeros2021.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySize

/** Zonas principales de la app, las que aparecen en la barra inferior. */
enum class MainTab(val iconRes: Int, val labelRes: Int) {
    HOME(R.drawable.ic_home, R.string.nav_home),
    FAVORITOS(R.drawable.ic_favoritos, R.string.nav_favoritos),
    AJUSTES(R.drawable.ic_ajustes, R.string.nav_ajustes),
}

/**
 * Barra de navegación inferior.
 *
 * Se construye a mano en vez de con `NavigationBar` de Material porque la design spec
 * fija 88dp de alto y un indicador de subrayado dorado; `NavigationBar` impone su
 * propia altura y dibuja una píldora tras el icono activo.
 */
@Composable
fun JewelryBottomBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(JewelryColors.Background),
    ) {
        GoldSeparator()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(JewelrySize.BottomNavHeight),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainTab.entries.forEach { tab ->
                TabItem(
                    tab = tab,
                    isSelected = tab == selected,
                    onClick = { onSelect(tab) },
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: MainTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (isSelected) JewelryColors.GoldPrimary else JewelryColors.TextMuted
    Column(
        modifier = modifier
            .width(96.dp)
            .clickable(
                // Sin ondas: la barra debe verse plana y limpia, como pide la design spec.
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(tab.iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(26.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(tab.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(2.dp)
                .background(
                    color = if (isSelected) JewelryColors.GoldPrimary else Color.Transparent,
                    shape = RoundedCornerShape(1.dp),
                ),
        )
    }
}
