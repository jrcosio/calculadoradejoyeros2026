package com.jrblanco.calculadoradejoyeros2021.ui.idioma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ObservarIdiomaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * El idioma de la app entera, para la raíz de la composición. Solo observa: la elección la hace la
 * pantalla de Ajustes, y este ViewModel se entera por el mismo flujo.
 *
 * No emite telemetría: el cambio ya lo registra Ajustes, y contarlo dos veces falsearía la serie.
 *
 * Lanza con `dispatchers.main`, como el resto de los ViewModels asíncronos del proyecto, que es lo
 * que permite testearlo con `TestDispatcherProvider` sin tocar `Dispatchers.Main`.
 */
class IdiomaAppViewModel(
    private val observarIdioma: ObservarIdiomaUseCase,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IdiomaAppUiState())
    val uiState: StateFlow<IdiomaAppUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(dispatchers.main) {
            observarIdioma().collect { seleccion ->
                _uiState.value = IdiomaAppUiState(idioma = seleccion.efectivo)
            }
        }
    }
}
