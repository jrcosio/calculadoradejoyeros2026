package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.precios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionMetal
import com.jrblanco.calculadoradejoyeros2021.domain.model.InstantaneaCotizaciones
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MotivoErrorCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.OrigenDatos
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.UnidadPrecio
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ConvertirCotizacionUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ObtenerCotizacionesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Sub-herramienta de precios: carga las cotizaciones al nacer (el ViewModel se crea al abrir la
 * sección, no antes), guarda la instantánea cruda y deriva de ella el estado formateado. Cambiar
 * de unidad o de metal re-deriva sin tocar la red; «Reintentar» vuelve a pasar por la política
 * de caché del repositorio.
 *
 * Es el primer ViewModel asíncrono del proyecto: lanza siempre con `dispatchers.main`, que es lo
 * que hace determinista el test con un `TestDispatcherProvider` sin tocar `Dispatchers.Main`.
 */
class PreciosMetalesViewModel(
    private val obtenerCotizaciones: ObtenerCotizacionesUseCase,
    private val convertirCotizacion: ConvertirCotizacionUseCase,
    private val analytics: AnalyticsRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreciosMetalesUiState())
    val uiState: StateFlow<PreciosMetalesUiState> = _uiState.asStateFlow()

    /** El dato crudo: permite re-derivar filas y detalle al cambiar unidad o metal sin red. */
    private var instantanea: InstantaneaCotizaciones? = null
    private var carga: Job? = null

    init {
        analytics.logScreenView(SCREEN_NAME)
        cargar(esReintento = false)
    }

    fun onUnidadSeleccionada(unidad: UnidadPrecio) {
        if (unidad == _uiState.value.unidad) return
        _uiState.update { derivar(instantanea, it.copy(unidad = unidad)) }
        analytics.logEvent(EVENT_UNIDAD, mapOf(PARAM_UNIDAD to unidad.analyticsId))
    }

    fun onMetalSeleccionado(metal: MetalCotizado) {
        if (metal == _uiState.value.seleccionado) return
        _uiState.update { derivar(instantanea, it.copy(seleccionado = metal)) }
        analytics.logEvent(EVENT_METAL, mapOf(PARAM_METAL to metal.analyticsId))
    }

    fun onReintentar() {
        cargar(esReintento = true)
    }

    private fun cargar(esReintento: Boolean) {
        if (carga?.isActive == true) return
        carga = viewModelScope.launch(dispatchers.main) {
            _uiState.update {
                it.copy(
                    fase = if (it.filas.isEmpty()) FasePrecios.CARGANDO else it.fase,
                    reintentando = esReintento,
                    avisoEspera = false,
                )
            }
            try {
                val nueva = obtenerCotizaciones()
                instantanea = nueva
                registrar(nueva)
                _uiState.update {
                    derivar(nueva, it.copy(reintentando = false))
                        .copy(avisoEspera = esReintento && nueva.origen == OrigenDatos.CACHE_EN_ESPERA)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Un fallo aquí no es de red (esos vienen clasificados en la instantánea): es un bug.
                analytics.recordError(e)
                _uiState.update {
                    it.copy(
                        fase = FasePrecios.ERROR,
                        errorGlobal = MotivoErrorCotizacion.DESCONOCIDO,
                        reintentando = false,
                        puedeReintentar = true,
                    )
                }
            }
        }
    }

    private fun derivar(instantanea: InstantaneaCotizaciones?, base: PreciosMetalesUiState): PreciosMetalesUiState {
        if (instantanea == null) return base
        val resultados = instantanea.resultados.values
        val errores = resultados.filterIsInstance<ResultadoCotizacion.Error>()
        val exitos = resultados.count { it is ResultadoCotizacion.Exito }
        val fase = when {
            exitos == 0 -> FasePrecios.ERROR
            instantanea.estaCompleta -> FasePrecios.LISTO
            else -> FasePrecios.PARCIAL
        }
        val errorGlobal = if (fase == FasePrecios.ERROR) motivoDominante(errores) ?: MotivoErrorCotizacion.DESCONOCIDO else null
        return base.copy(
            fase = fase,
            filas = MetalCotizado.entries.map { metal -> fila(metal, instantanea.resultados[metal], base.unidad) },
            detalle = detalle(instantanea, base.seleccionado, base.unidad),
            errorGlobal = errorGlobal,
            origen = instantanea.origen,
            ultimaConsultaEpochMillis = instantanea.instanteMasRecienteEpochMillis,
            // Sin credencial no hay nada que reintentar hasta otra build.
            puedeReintentar = fase != FasePrecios.LISTO && errorGlobal != MotivoErrorCotizacion.SIN_CREDENCIAL,
        )
    }

    private fun fila(metal: MetalCotizado, resultado: ResultadoCotizacion?, unidad: UnidadPrecio): FilaMetalPrecio =
        when (resultado) {
            is ResultadoCotizacion.Exito -> filaCon(metal, resultado.cotizacion, unidad, error = null, desactualizada = false)
            is ResultadoCotizacion.Error -> {
                val ultima = resultado.ultimaConocida
                if (ultima != null) {
                    filaCon(metal, ultima, unidad, error = resultado.motivo, desactualizada = true)
                } else {
                    FilaMetalPrecio(metal, null, null, null, null, resultado.motivo, desactualizada = false)
                }
            }
            null -> FilaMetalPrecio(metal, null, null, null, null, null, desactualizada = false)
        }

    private fun filaCon(
        metal: MetalCotizado,
        cotizacion: CotizacionMetal,
        unidad: UnidadPrecio,
        error: MotivoErrorCotizacion?,
        desactualizada: Boolean,
    ): FilaMetalPrecio {
        val convertida = convertirCotizacion(cotizacion, unidad)
        val fuente = convertida ?: cotizacion
        return FilaMetalPrecio(
            metal = metal,
            precioFormateado = fuente.precioPrincipal?.let(FormatoPrecios::importe),
            unidad = if (convertida != null) unidad else null,
            etiquetaUnidadOrigen = if (convertida == null) cotizacion.etiquetaUnidadOrigen else null,
            tendencia = fuente.tendencia,
            error = error,
            desactualizada = desactualizada,
        )
    }

    private fun detalle(instantanea: InstantaneaCotizaciones, metal: MetalCotizado, unidad: UnidadPrecio): DetalleMercado? {
        val cotizacion = instantanea.ultimaCotizacionConocida(metal) ?: return null
        val convertida = convertirCotizacion(cotizacion, unidad)
        val fuente = convertida ?: cotizacion
        return DetalleMercado(
            metal = metal,
            moneda = fuente.moneda,
            ask = FormatoPrecios.importe(fuente.ask),
            bid = FormatoPrecios.importe(fuente.bid),
            maximo = FormatoPrecios.importe(fuente.maximo),
            minimo = FormatoPrecios.importe(fuente.minimo),
            variacion = FormatoPrecios.variacion(fuente.variacion),
            variacionPorcentaje = FormatoPrecios.porcentaje(fuente.variacionPorcentaje),
            tendencia = fuente.tendencia,
            unidad = if (convertida != null) unidad else null,
            etiquetaUnidadOrigen = cotizacion.etiquetaUnidadOrigen,
            instanteMercadoEpochMillis = cotizacion.instanteMercadoEpochMillis,
            desactualizada = instantanea.resultados[metal] is ResultadoCotizacion.Error,
        )
    }

    private fun registrar(instantanea: InstantaneaCotizaciones) {
        val errores = instantanea.resultados.values.filterIsInstance<ResultadoCotizacion.Error>()
        val exitos = instantanea.resultados.values.count { it is ResultadoCotizacion.Exito }
        if (exitos > 0) {
            analytics.logEvent(
                EVENT_CARGADOS,
                mapOf(
                    PARAM_FUENTE to if (instantanea.origen == OrigenDatos.RED) "red" else "cache",
                    PARAM_PARCIAL to (!instantanea.estaCompleta).toString(),
                ),
            )
        } else {
            val motivo = motivoDominante(errores) ?: MotivoErrorCotizacion.DESCONOCIDO
            analytics.logEvent(EVENT_ERROR, mapOf(PARAM_MOTIVO to motivo.analyticsId))
        }
        // Lo inesperado va a Crashlytics; lo de red (sin conexión, 429…) no es un fallo de la app.
        errores.forEach { error ->
            val causa = error.causa ?: return@forEach
            if (error.motivo == MotivoErrorCotizacion.DESCONOCIDO || error.motivo == MotivoErrorCotizacion.RESPUESTA_INVALIDA) {
                analytics.recordError(causa)
            }
        }
    }

    private fun motivoDominante(errores: List<ResultadoCotizacion.Error>): MotivoErrorCotizacion? =
        errores.groupingBy { it.motivo }.eachCount().maxByOrNull { it.value }?.key

    private companion object {
        const val SCREEN_NAME = "herramientas_precios"
        const val EVENT_CARGADOS = "herramientas_precios_cargados"
        const val EVENT_ERROR = "herramientas_precios_error"
        const val EVENT_UNIDAD = "herramientas_unidad_cambiada"
        const val EVENT_METAL = "herramientas_metal_seleccionado"
        const val PARAM_FUENTE = "fuente"
        const val PARAM_PARCIAL = "parcial"
        const val PARAM_MOTIVO = "motivo"
        const val PARAM_UNIDAD = "unidad"
        const val PARAM_METAL = "metal"
    }
}
