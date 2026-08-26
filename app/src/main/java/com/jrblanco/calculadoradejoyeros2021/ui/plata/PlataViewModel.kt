package com.jrblanco.calculadoradejoyeros2021.ui.plata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.core.util.parsearDecimalPositivo
import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularAleacionPlataUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.GuardarFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ObtenerFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.ui.favoritos.AvisoFavorito
import com.jrblanco.calculadoradejoyeros2021.ui.favoritos.FormatoFavoritos
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlataViewModel(
    private val calcularAleacion: CalcularAleacionPlataUseCase,
    private val guardarFavorito: GuardarFavoritoUseCase,
    private val obtenerFavorito: ObtenerFavoritoUseCase,
    private val analytics: AnalyticsRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlataUiState())
    val uiState: StateFlow<PlataUiState> = _uiState.asStateFlow()

    /**
     * Deduplicación de telemetría: el recálculo es por pulsación de tecla y registrar cada
     * una sería ruido. Se emite `plata_calculado` cuando un cálculo válido estrena ley o
     * cuando la entrada vuelve a ser válida.
     */
    private var ultimaLeyRegistrada: LeyPlata? = null

    /** Guarda contra la reentrada de [cargarFavorito]; ver el KDoc del homólogo en oro. */
    private var favoritoAplicado = false

    init {
        // El mismo nombre que emitía el placeholder: conserva la serie histórica.
        analytics.logScreenView(SCREEN_NAME)
    }

    fun onCantidadCambiada(texto: String) = recalcular { it.copy(cantidadTexto = texto) }

    fun onLeySeleccionada(ley: LeyPlata) = recalcular { it.copy(ley = ley) }

    /** Vuelve al estado inicial; el siguiente cálculo válido vuelve a registrarse. */
    fun onLimpiar() {
        ultimaLeyRegistrada = null
        _uiState.value = PlataUiState()
    }

    /** Guarda el cálculo que hay en pantalla; el resultado llega por el estado. */
    fun onGuardarFavoritos() {
        val masa = parsearCantidad(_uiState.value.cantidadTexto)
        if (masa == null) {
            avisar(AvisoFavorito.SIN_DATOS)
            return
        }

        val ley = _uiState.value.ley
        viewModelScope.launch(dispatchers.main) {
            val resultado = guardarFavorito(EntradasFavorito.Plata(masaOrigen = masa, ley = ley))
            avisar(
                if (resultado is ResultadoGuardado.Guardado) {
                    AvisoFavorito.GUARDADO
                } else {
                    AvisoFavorito.REPETIDO
                },
            )
            analytics.logEvent(EVENT_FAVORITO, mapOf(PARAM_RESULTADO to resultado.analyticsId))
        }
    }

    fun onAvisoFavoritoMostrado() {
        if (_uiState.value.avisoFavorito == null) return
        _uiState.value = _uiState.value.copy(avisoFavorito = null)
    }

    /** Rellena la calculadora con un favorito. Idempotente; lo que no cuadra se ignora en silencio. */
    fun cargarFavorito(id: Long) {
        if (favoritoAplicado) return
        favoritoAplicado = true

        viewModelScope.launch(dispatchers.main) {
            val entradas = obtenerFavorito(id)?.entradas as? EntradasFavorito.Plata ?: return@launch
            ultimaLeyRegistrada = null
            val estado = PlataUiState(
                cantidadTexto = FormatoFavoritos.cantidadEntrada(entradas.masaOrigen),
                ley = entradas.ley,
            )
            _uiState.value = estado.copy(resultado = calcular(estado))
        }
    }

    private fun avisar(aviso: AvisoFavorito) {
        _uiState.value = _uiState.value.copy(avisoFavorito = aviso)
    }

    private fun recalcular(cambio: (PlataUiState) -> PlataUiState) {
        // El aviso se apaga en cuanto el joyero toca algo.
        val estado = cambio(_uiState.value).copy(avisoFavorito = null)
        _uiState.value = estado.copy(resultado = calcular(estado))
    }

    private fun calcular(estado: PlataUiState): ResultadoPlata? {
        val masa = parsearCantidad(estado.cantidadTexto)
        if (masa == null) {
            // Entrada inválida: sin resultados y sin error visible. Al volver a ser
            // válida, el cálculo se registra de nuevo.
            ultimaLeyRegistrada = null
            return null
        }

        val calculo = calcularAleacion(masa, estado.ley)
        registrarCalculo(estado.ley)

        return ResultadoPlata(
            cobreFormateado = formatearGramos(calculo.cobre),
            totalFormateado = formatearGramos(calculo.masaFinal),
        )
    }

    /** Coma y punto valen (§26): se normalizan antes de parsear. Inválido o ≤ 0 → null. Delegado en `core/util/Decimales.kt`. */
    private fun parsearCantidad(texto: String): BigDecimal? = parsearDecimalPositivo(texto)

    private fun registrarCalculo(ley: LeyPlata) {
        if (ley == ultimaLeyRegistrada) return
        ultimaLeyRegistrada = ley
        analytics.logEvent(EVENT_CALCULO, mapOf(PARAM_LEY to ley.analyticsId))
    }

    /**
     * Redondeo exclusivo de presentación, con coma decimal española. Nunca realimenta el
     * cálculo (§21).
     *
     * **Trunca (`DOWN`), no redondea a la media**, y aquí está la única diferencia
     * deliberada con `OroViewModel`. La Ley 17/1985 no admite tolerancia en menos para
     * plata (§3, §16) y la cifra que se muestra es la que el joyero pesa: con `HALF_UP`,
     * 100 g hacia 950‰ mostrarían 5,158 g de cobre y la ley real caería a 949,999‰.
     * Truncar da 5,157 g y 950,008‰, que es el valor que el propio documento pone como
     * salida esperada en §17 y §19. Truncar a 3 decimales equivale además al «modo taller
     * seguro» de §16-§17 con la resolución de balanza de 0,001 g que §18 recomienda.
     */
    private fun formatearGramos(valor: BigDecimal): String =
        valor.setScale(3, RoundingMode.DOWN).toPlainString().replace('.', ',')

    private companion object {
        const val SCREEN_NAME = "plata"
        const val EVENT_CALCULO = "plata_calculado"
        const val EVENT_FAVORITO = "plata_favorito_guardado"
        const val PARAM_RESULTADO = "resultado"
        const val PARAM_LEY = "ley"
    }
}
