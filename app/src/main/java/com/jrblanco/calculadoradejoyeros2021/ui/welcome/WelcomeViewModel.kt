package com.jrblanco.calculadoradejoyeros2021.ui.welcome

import androidx.lifecycle.ViewModel
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository

/**
 * ViewModel de la portada.
 *
 * **Desviación declarada del principio II de la constitución.** La norma dice que todo
 * ViewModel expone un único `StateFlow`; este no expone ninguno. La portada es
 * completamente estática: no hay nada que observar. Inventar un `WelcomeUiState` con un
 * campo artificial cumpliría la letra de la norma y empeoraría el código.
 *
 * El ViewModel existe porque hay una responsabilidad real que un Composable no puede
 * asumir sin saltarse la separación de capas: la telemetría. Queda registrada en
 * `specs/001-pantalla-inicio/plan.md`.
 */
class WelcomeViewModel(
    private val analytics: AnalyticsRepository,
) : ViewModel() {

    init {
        analytics.logScreenView(SCREEN_NAME)
    }

    fun onStartClicked() {
        analytics.logEvent(EVENT_START)
    }

    private companion object {
        const val SCREEN_NAME = "welcome"
        const val EVENT_START = "welcome_comenzar"
    }
}
