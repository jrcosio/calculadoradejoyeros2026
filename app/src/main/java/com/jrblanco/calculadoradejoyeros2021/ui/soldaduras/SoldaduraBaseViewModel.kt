package com.jrblanco.calculadoradejoyeros2021.ui.soldaduras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.core.util.parsearDecimalPositivo
import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.ModoEntradaSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraBaseInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraBaseUseCase
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

class SoldaduraBaseViewModel(
    private val calcularBase: CalcularSoldaduraBaseUseCase,
    private val calcularBaseInversa: CalcularSoldaduraBaseInversaUseCase,
    private val guardarFavorito: GuardarFavoritoUseCase,
    private val obtenerFavorito: ObtenerFavoritoUseCase,
    private val analytics: AnalyticsRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoldaduraBaseUiState())
    val uiState: StateFlow<SoldaduraBaseUiState> = _uiState.asStateFlow()

    /**
     * Deduplicación de telemetría: se emite `soldadura_base_calculado` cuando un cálculo
     * válido estrena modo o cuando la entrada vuelve a ser válida; nunca por tecla y
     * nunca con la cantidad (FR-027).
     */
    private var ultimoModoRegistrado: ModoEntradaSoldadura? = null

    /** Guarda contra la reentrada de [cargarFavorito]; ver el KDoc del homólogo en oro. */
    private var favoritoAplicado = false

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
        // El aviso se apaga en cuanto el joyero toca algo.
        val estado = _uiState.value.copy(cantidadTexto = texto, avisoFavorito = null)
        _uiState.value = estado.copy(resultado = calcular(estado))
    }

    /** Vuelve al estado inicial; el siguiente cálculo válido vuelve a registrarse. */
    fun onLimpiar() {
        ultimoModoRegistrado = null
        _uiState.value = SoldaduraBaseUiState()
    }

    /** Guarda el cálculo que hay en pantalla; el resultado llega por el estado. */
    fun onGuardarFavoritos() {
        val cantidad = parsearCantidad(_uiState.value.cantidadTexto)
        if (cantidad == null) {
            avisar(AvisoFavorito.SIN_DATOS)
            return
        }

        val modo = _uiState.value.modo
        viewModelScope.launch(dispatchers.main) {
            val resultado = guardarFavorito(
                EntradasFavorito.SoldaduraBase(cantidad = cantidad, modo = modo),
            )
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

    /**
     * Rellena la pantalla con un favorito. El modo y la cantidad van en **una sola asignación**: la
     * ruta pública `onModoCambiado` vacía la cantidad por FR-023, así que encadenarlas la perdería.
     */
    fun cargarFavorito(id: Long) {
        if (favoritoAplicado) return
        favoritoAplicado = true

        viewModelScope.launch(dispatchers.main) {
            val entradas = obtenerFavorito(id)?.entradas as? EntradasFavorito.SoldaduraBase
                ?: return@launch
            ultimoModoRegistrado = null
            val estado = SoldaduraBaseUiState(
                modo = entradas.modo,
                cantidadTexto = FormatoFavoritos.cantidadEntrada(entradas.cantidad),
            )
            _uiState.value = estado.copy(resultado = calcular(estado))
        }
    }

    private fun avisar(aviso: AvisoFavorito) {
        _uiState.value = _uiState.value.copy(avisoFavorito = aviso)
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
        const val EVENT_FAVORITO = "soldadura_base_favorito_guardado"
        const val PARAM_RESULTADO = "resultado"
        const val PARAM_MODO = "modo"
    }
}
