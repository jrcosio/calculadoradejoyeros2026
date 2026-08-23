package com.jrblanco.calculadoradejoyeros2021.ui.info

import androidx.lifecycle.ViewModel
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InfoViewModel(
    private val analytics: AnalyticsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InfoUiState(enlaces = InfoEnlace.entries))
    val uiState: StateFlow<InfoUiState> = _uiState.asStateFlow()

    init {
        analytics.logScreenView(SCREEN_NAME)
    }

    /**
     * Registra la apertura de un enlace y decide si hay que abrirlo.
     *
     * Devuelve `false` cuando ya hay una apertura en curso: es la guarda de FR-017, que
     * impide que una doble pulsación rápida abra el destino dos veces y cuente dos
     * eventos. `Modifier.clickable` no antirrebota por su cuenta.
     */
    fun onEnlacePulsado(enlace: InfoEnlace): Boolean {
        if (_uiState.value.abriendoEnlace) return false

        _uiState.update { it.copy(abriendoEnlace = true) }
        analytics.logEvent(EVENT_ENLACE, mapOf(PARAM_ENLACE to enlace.analyticsId))
        return true
    }

    /**
     * No había ninguna aplicación capaz de atender el enlace.
     *
     * Baja la guarda —no se ha ido nadie a ninguna parte, así que la pantalla no va a
     * recibir un nuevo `onPantallaVisible`— y deja el fallo registrado para poder
     * diagnosticarlo.
     */
    fun onEnlaceFallido(error: Throwable) {
        _uiState.update { it.copy(abriendoEnlace = false) }
        analytics.recordError(error)
    }

    /** La pantalla vuelve a estar en primer plano: se rehabilitan los accesos. */
    fun onPantallaVisible() {
        _uiState.update { it.copy(abriendoEnlace = false) }
    }

    private companion object {
        const val SCREEN_NAME = "acerca_de"
        const val EVENT_ENLACE = "acerca_de_enlace_abierto"
        const val PARAM_ENLACE = "enlace"
    }
}
