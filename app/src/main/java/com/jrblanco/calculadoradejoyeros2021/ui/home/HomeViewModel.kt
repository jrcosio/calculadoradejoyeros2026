package com.jrblanco.calculadoradejoyeros2021.ui.home

import androidx.lifecycle.ViewModel
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(
    private val analytics: AnalyticsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(modules = HomeModule.entries))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        analytics.logScreenView(SCREEN_NAME)
    }

    fun onModuleClicked(module: HomeModule) {
        analytics.logEvent(EVENT_MODULE, mapOf(PARAM_MODULE to module.analyticsId))
    }

    private companion object {
        const val SCREEN_NAME = "home"
        const val EVENT_MODULE = "home_modulo_abierto"
        const val PARAM_MODULE = "modulo"
    }
}
