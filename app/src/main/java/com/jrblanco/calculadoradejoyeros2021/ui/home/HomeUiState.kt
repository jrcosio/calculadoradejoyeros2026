package com.jrblanco.calculadoradejoyeros2021.ui.home

/**
 * Estado del menú principal.
 *
 * La lista es fija en esta feature, pero se expone como estado y no como constante
 * porque es el ViewModel quien decide qué módulos se ofrecen: más adelante podría
 * filtrarlos por ajustes o por favoritos sin tocar la pantalla.
 */
data class HomeUiState(
    val modules: List<HomeModule> = emptyList(),
)
