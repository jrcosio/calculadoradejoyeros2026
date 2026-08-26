package com.jrblanco.calculadoradejoyeros2021.ui.herramientas

import androidx.lifecycle.ViewModel
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * El armazón de Herramientas solo sabe qué sub-herramienta está elegida. Cada sub-herramienta
 * tiene su propio ViewModel, creado al abrirla por primera vez: así la API de precios no se
 * toca hasta que el joyero pulsa PRECIO METALES.
 */
class HerramientasViewModel(
    private val analytics: AnalyticsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HerramientasUiState())
    val uiState: StateFlow<HerramientasUiState> = _uiState.asStateFlow()

    init {
        // El mismo nombre que emitía el placeholder: conserva la serie histórica.
        analytics.logScreenView(SCREEN_NAME)
    }

    fun onSubherramientaSeleccionada(subherramienta: Subherramienta) {
        if (subherramienta == _uiState.value.subherramienta) return
        _uiState.value = HerramientasUiState(subherramienta = subherramienta)
        analytics.logEvent(EVENT_SUBHERRAMIENTA, mapOf(PARAM_SUBHERRAMIENTA to subherramienta.analyticsId))
    }

    /**
     * Abre la sub-herramienta de chapas porque llega un favorito, no porque el joyero la haya
     * elegido — y por eso **no emite `herramientas_subherramienta`**: ese evento mide una decisión
     * suya, y contaminarlo con aperturas de favorito corrompería la métrica.
     *
     * Idempotente, como el resto de las cargas de favorito.
     */
    fun abrirFavoritoDeChapa() {
        if (_uiState.value.subherramienta == Subherramienta.CHAPAS) return
        _uiState.value = HerramientasUiState(subherramienta = Subherramienta.CHAPAS)
    }

    private companion object {
        const val SCREEN_NAME = "herramientas"
        const val EVENT_SUBHERRAMIENTA = "herramientas_subherramienta"
        const val PARAM_SUBHERRAMIENTA = "subherramienta"
    }
}
