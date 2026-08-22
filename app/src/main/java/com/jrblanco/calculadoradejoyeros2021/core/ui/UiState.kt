package com.jrblanco.calculadoradejoyeros2021.core.ui

/**
 * Estado genérico que expone un ViewModel a su pantalla.
 *
 * La View solo lee este tipo: nunca recibe excepciones ni tipos de `data/`.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>
}
