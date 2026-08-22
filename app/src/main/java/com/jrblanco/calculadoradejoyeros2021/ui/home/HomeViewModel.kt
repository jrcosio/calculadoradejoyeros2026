package com.jrblanco.calculadoradejoyeros2021.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel de referencia del proyecto.
 *
 * Contrato que debe cumplir todo ViewModel:
 * - expone un único `StateFlow` inmutable; nada de `LiveData` ni de estado mutable público
 * - recibe sus dependencias por constructor (Koin), nunca las busca él
 * - no importa nada de `androidx.compose.*` ni de `com.google.firebase.*`
 */
class HomeViewModel(
    private val analytics: AnalyticsRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        onScreenOpened()
    }

    private fun onScreenOpened() {
        viewModelScope.launch {
            withContext(dispatchers.io) {
                analytics.logScreenView(SCREEN_NAME)
            }
            _uiState.update { it.copy(title = "Calculadora de Joyeros", isReady = true) }
        }
    }

    private companion object {
        const val SCREEN_NAME = "home"
    }
}
