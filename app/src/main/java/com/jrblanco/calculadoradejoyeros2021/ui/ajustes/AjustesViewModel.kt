package com.jrblanco.calculadoradejoyeros2021.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.GuardarIdiomaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ObservarIdiomaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Ajustes: por ahora, el idioma de la app.
 *
 * El estado **no se escribe al guardar**: se escribe cuando el flujo lo confirma. Una sola
 * dirección, y lo que se ve marcado en pantalla es siempre lo que está guardado de verdad.
 */
class AjustesViewModel(
    private val observarIdioma: ObservarIdiomaUseCase,
    private val guardarIdioma: GuardarIdiomaUseCase,
    private val analytics: AnalyticsRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AjustesUiState())
    val uiState: StateFlow<AjustesUiState> = _uiState.asStateFlow()

    init {
        // El mismo nombre que emitía el placeholder: conserva la serie histórica.
        analytics.logScreenView(SCREEN_NAME)
        viewModelScope.launch(dispatchers.main) {
            observarIdioma().collect { seleccion ->
                _uiState.value = AjustesUiState(elegido = seleccion.elegido, sistema = seleccion.sistema)
            }
        }
    }

    /** Elegir el idioma que ya está elegido no es un cambio: no se guarda ni se registra. */
    fun onIdiomaSeleccionado(idioma: IdiomaApp) {
        if (idioma == _uiState.value.elegido) return
        elegir(idioma, idioma.analyticsId)
    }

    /** Devuelve el control al dispositivo. También se recuerda entre arranques. */
    fun onAutomaticoSeleccionado() {
        if (_uiState.value.elegido == null) return
        elegir(null, VALOR_AUTOMATICO)
    }

    private fun elegir(idioma: IdiomaApp?, valorTelemetria: String) {
        viewModelScope.launch(dispatchers.main) { guardarIdioma(idioma) }
        analytics.logEvent(EVENT_IDIOMA, mapOf(PARAM_IDIOMA to valorTelemetria))
    }

    private companion object {
        const val SCREEN_NAME = "ajustes"
        const val EVENT_IDIOMA = "ajustes_idioma"
        const val PARAM_IDIOMA = "idioma"

        /** Identificador estable de «Automático» para telemetría; no es un idioma. */
        const val VALOR_AUTOMATICO = "automatico"
    }
}
