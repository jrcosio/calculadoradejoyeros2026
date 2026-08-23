package com.jrblanco.calculadoradejoyeros2021.ui.oro

import androidx.lifecycle.ViewModel
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularAleacionOroUseCase
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OroViewModel(
    private val calcularAleacion: CalcularAleacionOroUseCase,
    private val analytics: AnalyticsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OroUiState())
    val uiState: StateFlow<OroUiState> = _uiState.asStateFlow()

    /**
     * Deduplicación de telemetría: el recálculo es por pulsación de tecla y registrar
     * cada una sería ruido. Se emite `oro_calculado` cuando un cálculo válido estrena
     * combinación ley×color o cuando la entrada vuelve a ser válida.
     */
    private var ultimaCombinacionRegistrada: Pair<LeyOro, ColorOro>? = null

    init {
        // El mismo nombre que emitía el placeholder: conserva la serie histórica.
        analytics.logScreenView(SCREEN_NAME)
    }

    fun onCantidadCambiada(texto: String) = recalcular { it.copy(cantidadTexto = texto) }

    fun onLeySeleccionada(ley: LeyOro) = recalcular { it.copy(ley = ley) }

    fun onColorSeleccionado(color: ColorOro) = recalcular { it.copy(color = color) }

    /** Vuelve al estado inicial; el siguiente cálculo válido vuelve a registrarse. */
    fun onLimpiar() {
        ultimaCombinacionRegistrada = null
        _uiState.value = OroUiState()
    }

    /** Favoritos aún no existe: solo telemetría. El aviso efímero lo pone la vista. */
    fun onGuardarFavoritos() {
        analytics.logEvent(EVENT_FAVORITOS)
    }

    private fun recalcular(cambio: (OroUiState) -> OroUiState) {
        val estado = cambio(_uiState.value)
        _uiState.value = estado.copy(resultado = calcular(estado))
    }

    private fun calcular(estado: OroUiState): ResultadoOro? {
        val masa = parsearCantidad(estado.cantidadTexto)
        if (masa == null) {
            // Entrada inválida: sin resultados y sin error visible. Al volver a ser
            // válida, el cálculo se registra de nuevo.
            ultimaCombinacionRegistrada = null
            return null
        }

        val calculo = calcularAleacion(masa, estado.color, estado.ley)
        registrarCalculo(estado.ley, estado.color)

        return ResultadoOro(
            metales = calculo.metales.entries
                .sortedBy { it.key }
                .map { (metal, gramos) -> MetalCalculado(metal, formatearGramos(gramos)) },
            totalFormateado = formatearGramos(calculo.masaFinal),
        )
    }

    /** Coma y punto valen (§16): se normalizan antes de parsear. Inválido o ≤ 0 → null. */
    private fun parsearCantidad(texto: String): BigDecimal? =
        texto.trim()
            .replace(',', '.')
            .toBigDecimalOrNull()
            ?.takeIf { it > BigDecimal.ZERO }

    private fun registrarCalculo(ley: LeyOro, color: ColorOro) {
        val combinacion = ley to color
        if (combinacion == ultimaCombinacionRegistrada) return
        ultimaCombinacionRegistrada = combinacion
        analytics.logEvent(
            EVENT_CALCULO,
            mapOf(PARAM_LEY to ley.analyticsId, PARAM_COLOR to color.analyticsId),
        )
    }

    /**
     * Redondeo exclusivo de presentación, con la media del propio documento (§17) y
     * coma decimal española. Nunca realimenta el cálculo (§21).
     */
    private fun formatearGramos(valor: BigDecimal): String =
        valor.setScale(3, RoundingMode.HALF_UP).toPlainString().replace('.', ',')

    private companion object {
        const val SCREEN_NAME = "oro"
        const val EVENT_CALCULO = "oro_calculado"
        const val EVENT_FAVORITOS = "oro_favoritos_proximamente"
        const val PARAM_LEY = "ley"
        const val PARAM_COLOR = "color"
    }
}
