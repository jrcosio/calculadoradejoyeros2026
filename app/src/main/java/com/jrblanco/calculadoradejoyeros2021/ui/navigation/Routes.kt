package com.jrblanco.calculadoradejoyeros2021.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Rutas type-safe de Navigation Compose.
 *
 * Cada destino es un `@Serializable object` (sin argumentos) o `data class`
 * (con argumentos tipados). No se usan rutas como String.
 */
sealed interface Route {

    @Serializable
    data object Home : Route
}
