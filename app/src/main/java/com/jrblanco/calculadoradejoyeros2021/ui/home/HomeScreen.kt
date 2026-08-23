package com.jrblanco.calculadoradejoyeros2021.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.components.JewelryBottomBar
import com.jrblanco.calculadoradejoyeros2021.ui.components.JewelryScaffold
import com.jrblanco.calculadoradejoyeros2021.ui.components.MainTab
import com.jrblanco.calculadoradejoyeros2021.ui.components.ModuleCard
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onModuleClick: (HomeModule) -> Unit,
    onTabSelect: (MainTab) -> Unit,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        uiState = uiState,
        onModuleClick = { module ->
            viewModel.onModuleClicked(module)
            onModuleClick(module)
        },
        onTabSelect = onTabSelect,
        onInfo = onInfo,
        modifier = modifier,
    )
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onModuleClick: (HomeModule) -> Unit,
    onTabSelect: (MainTab) -> Unit,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    JewelryScaffold(
        onInfo = onInfo,
        modifier = modifier,
        bottomBar = {
            JewelryBottomBar(selected = MainTab.HOME, onSelect = onTabSelect)
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(JewelrySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Md),
        ) {
            items(uiState.modules, key = { it.name }) { module ->
                val presentation = module.presentation()
                ModuleCard(
                    imageRes = presentation.imageRes,
                    imageDescription = stringResource(presentation.imageDescriptionRes),
                    title = stringResource(presentation.titleRes),
                    description = stringResource(presentation.descriptionRes),
                    accent = presentation.accent,
                    onClick = { onModuleClick(module) },
                )
            }
        }
    }
}

/**
 * Cómo se pinta cada módulo.
 *
 * Este mapeo vive aquí y no en [HomeModule] para que el enum siga libre de Android y su
 * ViewModel se pueda testear en la JVM.
 */
private data class ModulePresentation(
    val imageRes: Int,
    val imageDescriptionRes: Int,
    val titleRes: Int,
    val descriptionRes: Int,
    val accent: Color,
)

private fun HomeModule.presentation(): ModulePresentation = when (this) {
    HomeModule.ORO -> ModulePresentation(
        imageRes = R.drawable.modulo_oro,
        imageDescriptionRes = R.string.modulo_oro_imagen,
        titleRes = R.string.modulo_oro_titulo,
        descriptionRes = R.string.modulo_oro_descripcion,
        accent = JewelryColors.GoldPrimary,
    )
    HomeModule.PLATA -> ModulePresentation(
        imageRes = R.drawable.modulo_plata,
        imageDescriptionRes = R.string.modulo_plata_imagen,
        titleRes = R.string.modulo_plata_titulo,
        descriptionRes = R.string.modulo_plata_descripcion,
        accent = JewelryColors.SilverPrimary,
    )
    HomeModule.SOLDADURAS -> ModulePresentation(
        imageRes = R.drawable.modulo_soldaduras,
        imageDescriptionRes = R.string.modulo_soldaduras_imagen,
        titleRes = R.string.modulo_soldaduras_titulo,
        descriptionRes = R.string.modulo_soldaduras_descripcion,
        accent = JewelryColors.GoldPrimary,
    )
    HomeModule.HERRAMIENTAS -> ModulePresentation(
        imageRes = R.drawable.modulo_herramientas,
        imageDescriptionRes = R.string.modulo_herramientas_imagen,
        titleRes = R.string.modulo_herramientas_titulo,
        descriptionRes = R.string.modulo_herramientas_descripcion,
        accent = JewelryColors.TealPrimary,
    )
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun HomeContentPreview() {
    Calculadoradejoyeros2021Theme {
        HomeContent(
            uiState = HomeUiState(modules = HomeModule.entries),
            onModuleClick = {},
            onTabSelect = {},
            onInfo = {},
        )
    }
}
