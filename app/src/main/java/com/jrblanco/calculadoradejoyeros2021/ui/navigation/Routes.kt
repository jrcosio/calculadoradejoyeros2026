package com.jrblanco.calculadoradejoyeros2021.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Rutas type-safe de Navigation Compose.
 *
 * Cada destino es un `@Serializable object` (sin argumentos) o `data class`
 * (con argumentos tipados). No se usan rutas como String.
 */
sealed interface Route {

    /** Portada. Destino inicial en cada arranque. */
    @Serializable
    data object Welcome : Route

    // --- Zonas principales: las tres de la barra inferior ---

    @Serializable
    data object Home : Route

    @Serializable
    data object Favoritos : Route

    @Serializable
    data object Ajustes : Route

    // --- Secciones de módulo: a pantalla completa, sin barra inferior ---

    @Serializable
    data object Oro : Route

    @Serializable
    data object Plata : Route

    @Serializable
    data object Soldaduras : Route

    @Serializable
    data object Herramientas : Route

    // --- Otros ---

    @Serializable
    data object AcercaDe : Route
}
