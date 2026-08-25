package com.jrblanco.calculadoradejoyeros2021.ui.idioma

import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp

/**
 * El idioma en el que se pinta la app.
 *
 * `null` no es «ninguno», es **«todavía no se sabe»**: la primera lectura de la preferencia es
 * asíncrona, y hasta que llega no se pinta nada. Así no hay un fotograma de texto en el idioma
 * equivocado (FR-013); el hueco lo cubre el `windowBackground` del tema, que ya es el azul de la
 * portada.
 */
data class IdiomaAppUiState(
    val idioma: IdiomaApp? = null,
)
