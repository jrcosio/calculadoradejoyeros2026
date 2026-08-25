package com.jrblanco.calculadoradejoyeros2021.ui.plata

import androidx.lifecycle.ViewModel
import com.jrblanco.calculadoradejoyeros2021.core.util.parsearDecimalPositivo
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularAleacionPlataUseCase
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlataViewModel(
    private val calcularAleacion: CalcularAleacionPlataUseCase,
    private val analytics: AnalyticsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlataUiState())
    val uiState: StateFlow<PlataUiState> = _uiState.asStateFlow()

    /**
     * Deduplicación de telemetría: el recálculo es por pulsación de tecla y registrar cada
     * una sería ruido. Se emite `plata_calculado` cuando un cálculo válido estrena ley o
     * cuando la entrada vuelve a ser válida.
     */
    private var ultimaLeyRegistrada: LeyPlata? = null

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

    /** Favoritos aún no existe: solo telemetría. El aviso efímero lo pone la vista. */
    fun onGuardarFavoritos() {
        analytics.logEvent(EVENT_FAVORITOS)
    }

    private fun recalcular(cambio: (PlataUiState) -> PlataUiState) {
        val estado = cambio(_uiState.value)
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
        const val EVENT_FAVORITOS = "plata_favoritos_proximamente"
        const val PARAM_LEY = "ley"
    }
}
