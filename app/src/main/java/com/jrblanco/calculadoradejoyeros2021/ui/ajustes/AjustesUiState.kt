package com.jrblanco.calculadoradejoyeros2021.ui.ajustes

import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp

/**
 * Lo que necesita la pantalla de Ajustes: qué eligió el joyero y qué ha detectado del dispositivo.
 *
 * No lleva la lista de opciones: son `IdiomaApp.entries` más la fila «Automático», y el orden lo
 * da el enum. [sistema] viaja como enum y no como texto porque su nombre lo pinta la vista con el
 * recurso que le toca, como en el resto del proyecto.
 */
data class AjustesUiState(
    /** `null` = «Automático» marcado: manda el dispositivo. */
    val elegido: IdiomaApp? = null,
    val sistema: IdiomaApp = IdiomaApp.PREDETERMINADO,
)
