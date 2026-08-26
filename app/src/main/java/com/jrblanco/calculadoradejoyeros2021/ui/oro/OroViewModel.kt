package com.jrblanco.calculadoradejoyeros2021.ui.oro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.core.util.parsearDecimalPositivo
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularAleacionOroUseCase
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

class OroViewModel(
    private val calcularAleacion: CalcularAleacionOroUseCase,
    private val guardarFavorito: GuardarFavoritoUseCase,
    private val obtenerFavorito: ObtenerFavoritoUseCase,
    private val analytics: AnalyticsRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OroUiState())
    val uiState: StateFlow<OroUiState> = _uiState.asStateFlow()

    /**
     * Deduplicación de telemetría: el recálculo es por pulsación de tecla y registrar
     * cada una sería ruido. Se emite `oro_calculado` cuando un cálculo válido estrena
     * combinación ley×color o cuando la entrada vuelve a ser válida.
     */
    private var ultimaCombinacionRegistrada: Pair<LeyOro, ColorOro>? = null

    /**
     * Guarda contra la reentrada de [cargarFavorito]. El ViewModel sobrevive al cambio de
     * configuración pero la composición no, así que un cambio de tamaño de letra, de tema o de
     * idioma del sistema relanzaría el efecto de la pantalla y **machacaría lo que el joyero llevara
     * editado**. Mismo papel que `registrada` en el antiguo `PlaceholderViewModel`.
     */
    private var favoritoAplicado = false

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

    /**
     * Guarda el cálculo que hay en pantalla. El resultado no se sabe en el momento del clic —el
     * guardado va a almacenamiento— así que viaja por el estado y la vista lo convierte en un Toast.
     */
    fun onGuardarFavoritos() {
        val masa = parsearCantidad(_uiState.value.cantidadTexto)
        if (masa == null) {
            avisar(AvisoFavorito.SIN_DATOS)
            return
        }

        val estado = _uiState.value
        viewModelScope.launch(dispatchers.main) {
            val resultado = guardarFavorito(
                EntradasFavorito.Oro(masaOrigen = masa, color = estado.color, ley = estado.ley),
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

    /** La vista consume el aviso: sin esto, guardar dos veces seguidas no volvería a avisar. */
    fun onAvisoFavoritoMostrado() {
        if (_uiState.value.avisoFavorito == null) return
        _uiState.value = _uiState.value.copy(avisoFavorito = null)
    }

    /**
     * Rellena la calculadora con un favorito guardado. Idempotente a propósito (ver
     * [favoritoAplicado]); un id que ya no existe o de otro tipo se ignora en silencio, porque
     * quedarse en el estado inicial es un destino perfectamente válido y no es un fallo de la app.
     */
    fun cargarFavorito(id: Long) {
        if (favoritoAplicado) return
        favoritoAplicado = true

        viewModelScope.launch(dispatchers.main) {
            val entradas = obtenerFavorito(id)?.entradas as? EntradasFavorito.Oro ?: return@launch
            aplicar(entradas)
        }
    }

    /** Estado completo en una sola asignación, sin pasar por los setters públicos. */
    private fun aplicar(entradas: EntradasFavorito.Oro) {
        ultimaCombinacionRegistrada = null
        val estado = OroUiState(
            cantidadTexto = FormatoFavoritos.cantidadEntrada(entradas.masaOrigen),
            ley = entradas.ley,
            color = entradas.color,
        )
        _uiState.value = estado.copy(resultado = calcular(estado))
    }

    private fun avisar(aviso: AvisoFavorito) {
        _uiState.value = _uiState.value.copy(avisoFavorito = aviso)
    }

    private fun recalcular(cambio: (OroUiState) -> OroUiState) {
        // El aviso se apaga en cuanto el joyero toca algo: `copy` lo arrastraría si no.
        val estado = cambio(_uiState.value).copy(avisoFavorito = null)
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

    /** Coma y punto valen (§16): se normalizan antes de parsear. Inválido o ≤ 0 → null. Delegado en `core/util/Decimales.kt`. */
    private fun parsearCantidad(texto: String): BigDecimal? = parsearDecimalPositivo(texto)

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
        const val EVENT_FAVORITO = "oro_favorito_guardado"
        const val PARAM_RESULTADO = "resultado"
        const val PARAM_LEY = "ley"
        const val PARAM_COLOR = "color"
    }
}
