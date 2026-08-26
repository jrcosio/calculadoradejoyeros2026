package com.jrblanco.calculadoradejoyeros2021.ui.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.Favorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalLiga
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResumenFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.BorrarFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ObservarFavoritosUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ResumirFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.ui.soldaduras.FamiliaSoldadura
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * La pestaña de Favoritos.
 *
 * Observa la lista y rehace las cifras de cada favorito con [resumirFavorito], porque lo guardado
 * son las entradas y no los resultados: así la tarjeta se lee siempre en el idioma elegido y con el
 * redondeo propio de su calculadora.
 *
 * El redondeo lo aplica [FormatoFavoritos], que **duplica a propósito** las políticas de las cuatro
 * calculadoras; el guardián de esa duplicación es `FavoritosParidadFormatoTest`.
 */
class FavoritosViewModel(
    private val observarFavoritos: ObservarFavoritosUseCase,
    private val resumirFavorito: ResumirFavoritoUseCase,
    private val borrarFavorito: BorrarFavoritoUseCase,
    private val analytics: AnalyticsRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritosUiState())
    val uiState: StateFlow<FavoritosUiState> = _uiState.asStateFlow()

    /**
     * El favorito sobre el que está abierta la pregunta de borrado, por id.
     *
     * Se guarda aquí y no en el estado, y el campo del `UiState` se **re-deriva** en cada emisión:
     * así, si el favorito desaparece de la lista mientras el diálogo está abierto, la pregunta se
     * cierra sola en vez de quedarse colgada sobre algo que ya no existe.
     */
    private var idPendienteDeBorrar: Long? = null

    init {
        // El mismo nombre que emitía el placeholder: conserva la serie histórica.
        analytics.logScreenView(SCREEN_NAME)
        viewModelScope.launch(dispatchers.main) {
            observarFavoritos().collect { favoritos -> _uiState.value = mapear(favoritos) }
        }
    }

    /** Solo telemetría: la navegación la hace la vista, como en Home. */
    fun onFavoritoPulsado(favorito: FavoritoUiModel) {
        analytics.logEvent(EVENT_ABIERTO, mapOf(PARAM_TIPO to favorito.tipo.analyticsId))
    }

    fun onQuitarPulsado(favorito: FavoritoUiModel) {
        idPendienteDeBorrar = favorito.id
        _uiState.value = _uiState.value.copy(pendienteDeBorrar = favorito)
    }

    fun onCancelarBorrado() {
        idPendienteDeBorrar = null
        _uiState.value = _uiState.value.copy(pendienteDeBorrar = null)
    }

    /**
     * Cierra la pregunta **de inmediato** y lanza el borrado sin esperarlo: si esperase, un
     * almacenamiento lento dejaría el diálogo clavado. La fila desaparece cuando el flujo reemita.
     */
    fun onConfirmarBorrado() {
        val favorito = _uiState.value.pendienteDeBorrar ?: return
        idPendienteDeBorrar = null
        _uiState.value = _uiState.value.copy(pendienteDeBorrar = null)

        analytics.logEvent(EVENT_BORRADO, mapOf(PARAM_TIPO to favorito.tipo.analyticsId))
        viewModelScope.launch(dispatchers.main) { borrarFavorito(favorito.id) }
    }

    private fun mapear(favoritos: List<Favorito>): FavoritosUiState {
        val tarjetas = favoritos.map(::aTarjeta)
        return FavoritosUiState(
            cargando = false,
            favoritos = tarjetas,
            // Si el favorito de la pregunta ya no está, la pregunta se cierra.
            pendienteDeBorrar = tarjetas.firstOrNull { it.id == idPendienteDeBorrar },
        )
    }

    private fun aTarjeta(favorito: Favorito): FavoritoUiModel {
        val resumen = resumirFavorito(favorito.entradas)
        return FavoritoUiModel(
            id = favorito.id,
            entradas = entradasUi(favorito.entradas),
            lineas = lineasDe(resumen),
            totalFormateado = totalDe(resumen),
            guardadoEnEpochMillis = favorito.guardadoEnEpochMillis,
        )
    }

    private fun entradasUi(entradas: EntradasFavorito): EntradasFavoritoUi = when (entradas) {
        is EntradasFavorito.Oro -> EntradasFavoritoUi.Oro(
            ley = entradas.ley,
            color = entradas.color,
            cantidad = FormatoFavoritos.cantidadEntrada(entradas.masaOrigen),
        )

        is EntradasFavorito.Plata -> EntradasFavoritoUi.Plata(
            ley = entradas.ley,
            cantidad = FormatoFavoritos.cantidadEntrada(entradas.masaOrigen),
        )

        is EntradasFavorito.SoldaduraLey -> EntradasFavoritoUi.Soldadura(
            familia = FamiliaSoldadura.ORO_LEY,
            modo = entradas.modo,
            cantidad = FormatoFavoritos.cantidadEntrada(entradas.cantidad),
            dureza = entradas.dureza,
            colorOro = entradas.color,
        )

        is EntradasFavorito.SoldaduraClasica -> EntradasFavoritoUi.Soldadura(
            familia = FamiliaSoldadura.CLASICA,
            modo = entradas.modo,
            cantidad = FormatoFavoritos.cantidadEntrada(entradas.cantidad),
            tipoClasica = entradas.tipo,
        )

        is EntradasFavorito.SoldaduraPlata -> EntradasFavoritoUi.Soldadura(
            familia = FamiliaSoldadura.PLATA,
            modo = entradas.modo,
            cantidad = FormatoFavoritos.cantidadEntrada(entradas.cantidad),
            tipoPlata = entradas.tipo,
        )

        is EntradasFavorito.SoldaduraBase -> EntradasFavoritoUi.SoldaduraBase(
            modo = entradas.modo,
            cantidad = FormatoFavoritos.cantidadEntrada(entradas.cantidad),
        )

        is EntradasFavorito.Chapa -> EntradasFavoritoUi.Chapa(
            material = entradas.material,
            ancho = FormatoFavoritos.cantidadEntrada(entradas.ancho),
            largo = FormatoFavoritos.cantidadEntrada(entradas.largo),
            espesor = FormatoFavoritos.cantidadEntrada(entradas.espesor),
        )
    }

    /** Cada calculadora con su política de redondeo, sin unificar (ver [FormatoFavoritos]). */
    private fun lineasDe(resumen: ResumenFavorito): List<LineaFavoritoUi> = when (resumen) {
        is ResumenFavorito.Oro -> resumen.metales.entries
            .sortedBy { it.key }
            .map { (metal, gramos) ->
                LineaFavoritoUi(metal.concepto, FormatoFavoritos.gramosMedia(gramos))
            }

        is ResumenFavorito.Plata -> listOf(
            LineaFavoritoUi(ConceptoFavorito.COBRE, FormatoFavoritos.gramosPlata(resumen.cobre)),
        )

        is ResumenFavorito.SoldaduraLey -> buildList {
            add(LineaFavoritoUi(ConceptoFavorito.BASE, FormatoFavoritos.gramosMedia(resumen.base)))
            resumen.oro18K?.let {
                add(LineaFavoritoUi(ConceptoFavorito.ORO_18K, FormatoFavoritos.gramosMedia(it)))
            }
        }

        is ResumenFavorito.Soldadura -> resumen.componentes.map { componente ->
            LineaFavoritoUi(componente.metal.concepto, FormatoFavoritos.gramosMedia(componente.gramos))
        }

        is ResumenFavorito.Chapa -> listOf(
            LineaFavoritoUi(ConceptoFavorito.VOLUMEN, FormatoFavoritos.tresDecimales(resumen.volumenCm3)),
            LineaFavoritoUi(ConceptoFavorito.METAL_FINO, FormatoFavoritos.tresDecimales(resumen.metalFino)),
        )
    }

    private fun totalDe(resumen: ResumenFavorito): String = when (resumen) {
        is ResumenFavorito.Oro -> FormatoFavoritos.gramosMedia(resumen.masaFinal)
        is ResumenFavorito.Plata -> FormatoFavoritos.gramosPlata(resumen.masaFinal)
        is ResumenFavorito.SoldaduraLey -> FormatoFavoritos.gramosMedia(resumen.total)
        is ResumenFavorito.Soldadura -> FormatoFavoritos.gramosMedia(resumen.total)
        // El peso de la chapa es el protagonista, y va a dos decimales.
        is ResumenFavorito.Chapa -> FormatoFavoritos.pesoChapa(resumen.peso)
    }

    private val MetalLiga.concepto: ConceptoFavorito
        get() = when (this) {
            MetalLiga.PLATA_FINA -> ConceptoFavorito.PLATA_FINA
            MetalLiga.COBRE -> ConceptoFavorito.COBRE
            MetalLiga.PALADIO -> ConceptoFavorito.PALADIO
        }

    private val MetalSoldadura.concepto: ConceptoFavorito
        get() = when (this) {
            MetalSoldadura.ORO_24K -> ConceptoFavorito.ORO_24K
            MetalSoldadura.ORO_18K -> ConceptoFavorito.ORO_18K
            MetalSoldadura.PLATA_FINA -> ConceptoFavorito.PLATA_FINA
            MetalSoldadura.LATON -> ConceptoFavorito.LATON
            MetalSoldadura.COBRE -> ConceptoFavorito.COBRE
            MetalSoldadura.ZINC -> ConceptoFavorito.ZINC
            MetalSoldadura.CADMIO -> ConceptoFavorito.CADMIO
        }

    private companion object {
        const val SCREEN_NAME = "favoritos"
        const val EVENT_ABIERTO = "favoritos_abierto"
        const val EVENT_BORRADO = "favoritos_borrado"
        const val PARAM_TIPO = "tipo"
    }
}
