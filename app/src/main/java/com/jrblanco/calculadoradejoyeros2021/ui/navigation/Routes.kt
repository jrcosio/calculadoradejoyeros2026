package com.jrblanco.calculadoradejoyeros2021.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Rutas type-safe de Navigation Compose.
 *
 * Cada destino es un `@Serializable object` (sin argumentos) o `data class`
 * (con argumentos tipados). No se usan rutas como String.
 *
 * Las cinco secciones que se pueden abrir desde un favorito llevan `favoritoId`, nulable y con
 * `null` por defecto: el serializador de rutas lo emite como parámetro de consulta y aplica el
 * valor por defecto cuando no viene. **Nulable y no un valor centinela**, que es la doctrina del
 * proyecto: un `0L` que «significa ninguno» sería una restricción invisible sobre la clave primaria
 * del almacén.
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
    data class Oro(val favoritoId: Long? = null) : Route

    @Serializable
    data class Plata(val favoritoId: Long? = null) : Route

    @Serializable
    data class Soldaduras(val favoritoId: Long? = null) : Route

    /** La preparación de la soldadura BASE; se llega desde el formulario de ORO LEY. */
    @Serializable
    data class SoldaduraBase(val favoritoId: Long? = null) : Route

    @Serializable
    data class Herramientas(val favoritoId: Long? = null) : Route

    // --- Otros ---

    @Serializable
    data object AcercaDe : Route
}
