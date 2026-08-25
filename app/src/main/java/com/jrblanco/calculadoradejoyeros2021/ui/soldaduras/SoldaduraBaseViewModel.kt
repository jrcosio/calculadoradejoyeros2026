package com.jrblanco.calculadoradejoyeros2021.ui.soldaduras

import androidx.lifecycle.ViewModel
import com.jrblanco.calculadoradejoyeros2021.core.util.parsearDecimalPositivo
import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraBaseInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraBaseUseCase
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SoldaduraBaseViewModel(
    private val calcularBase: CalcularSoldaduraBaseUseCase,
    private val calcularBaseInversa: CalcularSoldaduraBaseInversaUseCase,
    private val analytics: AnalyticsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoldaduraBaseUiState())
    val uiState: StateFlow<SoldaduraBaseUiState> = _uiState.asStateFlow()

    /**
     * Deduplicación de telemetría: se emite `soldadura_base_calculado` cuando un cálculo
     * válido estrena modo o cuando la entrada vuelve a ser válida; nunca por tecla y
     * nunca con la cantidad (FR-027).
     */
    private var ultimoModoRegistrado: ModoEntradaSoldadura? = null

    init {
        // Pantalla nueva: estrena su propia serie de telemetría.
        analytics.logScreenView(SCREEN_NAME)
    }

    /**
     * Cambiar de modo vacía cantidad y resultado (FR-023): un «10» tecleado como oro no
     * puede pasar a leerse como peso de base.
     */
    fun onModoCambiado(modo: ModoEntradaSoldadura) {
        ultimoModoRegistrado = null
        _uiState.value = SoldaduraBaseUiState(modo = modo)
    }

    fun onCantidadCambiada(texto: String) {
        val estado = _uiState.value.copy(cantidadTexto = texto)
        _uiState.value = estado.copy(resultado = calcular(estado))
    }

    /** Vuelve al estado inicial; el siguiente cálculo válido vuelve a registrarse. */
    fun onLimpiar() {
        ultimoModoRegistrado = null
        _uiState.value = SoldaduraBaseUiState()
    }

    /** Favoritos aún no existe: solo telemetría. El aviso efímero lo pone la vista. */
    fun onGuardarFavoritos() {
        analytics.logEvent(EVENT_FAVORITOS)
    }

    private fun calcular(estado: SoldaduraBaseUiState): ResultadoSoldaduraBase? {
        val cantidad = parsearCantidad(estado.cantidadTexto)
        if (cantidad == null) {
            // Entrada inválida: sin resultados y sin error visible.
            ultimoModoRegistrado = null
            return null
        }

        val calculo: CalculoSoldadura
        val filas: List<FilaSoldadura>

        when (estado.modo) {
            ModoEntradaSoldadura.DESDE_METAL -> {
                calculo = calcularBase(cantidad)
                // El oro introducido no se repite como fila (FR-022): solo la liga,
                // en el orden de §5.2.
                filas = calculo.componentes
                    .filter { it.metal != MetalSoldadura.ORO_24K }
                    .map { FilaSoldadura(it.metal.ingrediente, formatearGramos(it.gramos)) }
            }

            ModoEntradaSoldadura.PESO_FINAL -> {
                calculo = calcularBaseInversa(cantidad)
                filas = calculo.componentes
                    .map { FilaSoldadura(it.metal.ingrediente, formatearGramos(it.gramos)) }
            }
        }

        registrarCalculo(estado.modo)

        return ResultadoSoldaduraBase(
            filas = filas,
            totalFormateado = formatearGramos(calculo.total),
        )
    }

    /** Coma y punto valen (§8.1): se normalizan antes de parsear. Inválido o ≤ 0 → null. Delegado en `core/util/Decimales.kt`. */
    private fun parsearCantidad(texto: String): BigDecimal? = parsearDecimalPositivo(texto)

    private fun registrarCalculo(modo: ModoEntradaSoldadura) {
        if (modo == ultimoModoRegistrado) return
        ultimoModoRegistrado = modo
        analytics.logEvent(EVENT_CALCULO, mapOf(PARAM_MODO to modo.analyticsId))
    }

    /**
     * Redondeo exclusivo de presentación: a la media y con coma decimal, igual que en la
     * pantalla de soldaduras. Nunca realimenta el cálculo (§8.1, §8.3).
     */
    private fun formatearGramos(valor: BigDecimal): String =
        valor.setScale(3, RoundingMode.HALF_UP).toPlainString().replace('.', ',')

    private companion object {
        const val SCREEN_NAME = "soldadura_base"
        const val EVENT_CALCULO = "soldadura_base_calculado"
        const val EVENT_FAVORITOS = "soldadura_base_favoritos_proximamente"
        const val PARAM_MODO = "modo"
    }
}
