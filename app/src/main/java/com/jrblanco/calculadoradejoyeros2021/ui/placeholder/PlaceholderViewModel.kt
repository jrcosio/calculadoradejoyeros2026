package com.jrblanco.calculadoradejoyeros2021.ui.placeholder

import androidx.lifecycle.ViewModel
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository

/**
 * Telemetría de las pantallas de andamiaje (FR-012).
 *
 * El nombre llega por método y no por constructor a propósito: inyectarlo con
 * parámetros de Koin obligaría a meter `String` entre los tipos externos de
 * `KoinModulesTest` y debilitaría la verificación del grafo. Así el ViewModel se
 * registra como cualquier otro y el test lo sigue cubriendo entero.
 */
class PlaceholderViewModel(
    private val analytics: AnalyticsRepository,
) : ViewModel() {

    private var registrada = false

    /** Idempotente: las recomposiciones no deben duplicar el registro. */
    fun onScreenShown(screenName: String) {
        if (registrada) return
        registrada = true
        analytics.logScreenView(screenName)
    }
}
