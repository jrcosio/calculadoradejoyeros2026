package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.core.util.parsearDecimalPositivo
import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.FamiliaChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.MaterialChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularPesoChapaUseCase
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

/**
 * Calculadora de peso de chapas: recalcula en cada cambio y solo muestra resultado con las tres
 * medidas válidas (sin botón de calcular). El redondeo de vista es el del documento técnico —
 * peso a **dos** decimales a la media (1,558 g → «1,56 g»)— y no el de las otras calculadoras:
 * las densidades son orientativas y un tercer decimal sería precisión aparente.
 *
 * Cambiar de familia o de ley conserva las medidas: la geometría no depende del metal.
 */
class PesoChapasViewModel(
    private val calcularPeso: CalcularPesoChapaUseCase,
    private val guardarFavorito: GuardarFavoritoUseCase,
    private val obtenerFavorito: ObtenerFavoritoUseCase,
    private val analytics: AnalyticsRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PesoChapasUiState())
    val uiState: StateFlow<PesoChapasUiState> = _uiState.asStateFlow()

    /** Deduplicación de telemetría: el recálculo es por pulsación de tecla. */
    private var ultimoMaterialRegistrado: MaterialChapa? = null

    /** Guarda contra la reentrada de [cargarFavorito]; ver el KDoc del homólogo en oro. */
    private var favoritoAplicado = false

    init {
        analytics.logScreenView(SCREEN_NAME)
    }

    fun onFamiliaSeleccionada(familia: FamiliaChapa) {
        if (familia == _uiState.value.material.familia) return
        recalcular { it.copy(material = MaterialChapa.porDefecto(familia)) }
    }

    fun onMaterialSeleccionado(material: MaterialChapa) = recalcular { it.copy(material = material) }

    fun onMedidaCambiada(medida: MedidaChapa, texto: String) =
        recalcular { it.copy(medidas = it.medidas + (medida to texto)) }

    fun onLimpiar() {
        ultimoMaterialRegistrado = null
        _uiState.value = PesoChapasUiState()
    }

    /**
     * Guarda la chapa que hay en pantalla. Sin las tres medidas válidas y en rango no hay nada que
     * guardar: se avisa igual que si el campo estuviera vacío.
     */
    fun onGuardarFavoritos() {
        val estado = _uiState.value
        val medidas = MedidaChapa.entries.associateWith { medida ->
            parsearDecimalPositivo(estado.medidas[medida].orEmpty())
                ?.takeIf { it <= medida.maximoMm }
        }
        val ancho = medidas[MedidaChapa.ANCHO]
        val largo = medidas[MedidaChapa.LARGO]
        val espesor = medidas[MedidaChapa.ESPESOR]
        if (ancho == null || largo == null || espesor == null) {
            avisar(AvisoFavorito.SIN_DATOS)
            return
        }

        viewModelScope.launch(dispatchers.main) {
            val resultado = guardarFavorito(
                EntradasFavorito.Chapa(
                    ancho = ancho,
                    largo = largo,
                    espesor = espesor,
                    material = estado.material,
                ),
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

    /** Rellena la sub-herramienta con un favorito. Idempotente; lo que no cuadra se ignora. */
    fun cargarFavorito(id: Long) {
        if (favoritoAplicado) return
        favoritoAplicado = true

        viewModelScope.launch(dispatchers.main) {
            val entradas = obtenerFavorito(id)?.entradas as? EntradasFavorito.Chapa ?: return@launch
            ultimoMaterialRegistrado = null
            // Material y las tres medidas de un golpe: nada de encadenar los setters públicos.
            recalcular {
                PesoChapasUiState(
                    material = entradas.material,
                    medidas = mapOf(
                        MedidaChapa.ANCHO to FormatoFavoritos.cantidadEntrada(entradas.ancho),
                        MedidaChapa.ESPESOR to FormatoFavoritos.cantidadEntrada(entradas.espesor),
                        MedidaChapa.LARGO to FormatoFavoritos.cantidadEntrada(entradas.largo),
                    ),
                )
            }
        }
    }

    private fun avisar(aviso: AvisoFavorito) {
        _uiState.value = _uiState.value.copy(avisoFavorito = aviso)
    }

    private fun recalcular(cambio: (PesoChapasUiState) -> PesoChapasUiState) {
        // El aviso se apaga en cuanto el joyero toca algo.
        val estado = cambio(_uiState.value).copy(avisoFavorito = null)
        val valores = MedidaChapa.entries.associateWith { medida ->
            parsearDecimalPositivo(estado.medidas[medida].orEmpty())
        }
        // Fuera de rango (§11.4) cuenta como no válida: se marca y no hay resultado.
        val fueraDeRango = valores.filter { (medida, valor) -> valor != null && valor > medida.maximoMm }.keys
        val validos = valores.mapValues { (medida, valor) -> valor?.takeIf { medida !in fueraDeRango } }
        val ancho = validos[MedidaChapa.ANCHO]
        val largo = validos[MedidaChapa.LARGO]
        val espesor = validos[MedidaChapa.ESPESOR]

        val dibujo = DibujoChapaUiState(
            proporciones = ProporcionesChapa.desde(ancho, largo, espesor),
            etiquetaAncho = ancho?.let(::formatearMedida),
            etiquetaEspesor = espesor?.let(::formatearMedida),
            etiquetaLargo = largo?.let(::formatearMedida),
            completa = ancho != null && largo != null && espesor != null,
        )

        val resultado = if (ancho != null && largo != null && espesor != null) {
            val calculo = calcularPeso(ancho, largo, espesor, estado.material)
            registrarCalculo(estado.material)
            formatear(calculo)
        } else {
            ultimoMaterialRegistrado = null
            null
        }

        _uiState.value = estado.copy(fueraDeRango = fueraDeRango, dibujo = dibujo, resultado = resultado)
    }

    private fun formatear(calculo: CalculoChapa): ResultadoChapa = ResultadoChapa(
        pesoFormateado = calculo.peso.setScale(ESCALA_PESO, RoundingMode.HALF_UP).aTexto(),
        volumenFormateado = calculo.volumenCm3.setScale(ESCALA_VOLUMEN, RoundingMode.HALF_UP).aTexto(),
        densidadFormateada = calculo.densidad.aTexto(),
        purezaFormateada = calculo.material.finura.movePointRight(2).setScale(ESCALA_PUREZA, RoundingMode.HALF_UP).aTexto(),
        metalFinoFormateado = calculo.metalFino.setScale(ESCALA_FINO, RoundingMode.HALF_UP).aTexto(),
    )

    /** Cota de la ilustración, sin unidad: «10,00»; la vista añade «mm». */
    private fun formatearMedida(valor: BigDecimal): String =
        valor.setScale(ESCALA_MM, RoundingMode.HALF_UP).aTexto()

    private fun BigDecimal.aTexto(): String = toPlainString().replace('.', ',')

    private fun registrarCalculo(material: MaterialChapa) {
        if (material == ultimoMaterialRegistrado) return
        ultimoMaterialRegistrado = material
        analytics.logEvent(
            EVENT_CALCULO,
            mapOf(PARAM_MATERIAL to material.familia.analyticsId, PARAM_LEY to material.analyticsId),
        )
    }

    private companion object {
        const val SCREEN_NAME = "herramientas_chapas"
        const val EVENT_CALCULO = "herramientas_chapa_calculada"
        const val EVENT_FAVORITO = "herramientas_chapa_favorito_guardado"
        const val PARAM_RESULTADO = "resultado"
        const val PARAM_MATERIAL = "material"
        const val PARAM_LEY = "ley"
        const val ESCALA_PESO = 2
        const val ESCALA_VOLUMEN = 3
        const val ESCALA_FINO = 3
        const val ESCALA_PUREZA = 1
        const val ESCALA_MM = 2
    }
}
