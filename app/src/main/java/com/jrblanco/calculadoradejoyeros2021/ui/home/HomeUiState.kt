package com.jrblanco.calculadoradejoyeros2021.ui.home

/**
 * Estado que `HomeScreen` pinta. Inmutable y sin tipos de Android ni de Firebase.
 */
data class HomeUiState(
    val title: String = "",
    val isReady: Boolean = false,
)
