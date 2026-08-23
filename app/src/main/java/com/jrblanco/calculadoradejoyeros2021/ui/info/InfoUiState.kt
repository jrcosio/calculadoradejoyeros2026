package com.jrblanco.calculadoradejoyeros2021.ui.info

/**
 * Estado de la pantalla de información.
 *
 * [abriendoEnlace] es la guarda de FR-017: mientras hay una apertura en curso, las
 * pulsaciones siguientes se ignoran, de modo que una doble pulsación rápida no abre el
 * destino dos veces ni cuenta dos eventos. Se baja al volver a la pantalla.
 */
data class InfoUiState(
    val enlaces: List<InfoEnlace> = emptyList(),
    val abriendoEnlace: Boolean = false,
)
