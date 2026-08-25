package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.precios

import com.jrblanco.calculadoradejoyeros2021.core.util.TestDispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.domain.model.ConversorUnidadesPrecio
import com.jrblanco.calculadoradejoyeros2021.data.repository.FakeCotizacionesRepository
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.cotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.exito
import com.jrblanco.calculadoradejoyeros2021.domain.model.InstantaneaCotizaciones
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.OrigenDatos
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.Tendencia
import com.jrblanco.calculadoradejoyeros2021.domain.model.UnidadPrecio
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ConvertirCotizacionUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ObtenerCotizacionesUseCase
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Casos de uso reales sobre un repositorio falso; solo se mockea la telemetría. El
 * `TestDispatcherProvider` hace que el `launch(dispatchers.main)` del ViewModel corra en el test.
 */
class PreciosMetalesViewModelTest {

    private val t0 = 1_787_670_000_000L
    private val analytics = mockk<AnalyticsRepository>(relaxed = true)
    private val repositorio = FakeCotizacionesRepository()

    private fun crearViewModel() = PreciosMetalesViewModel(
        obtenerCotizaciones = ObtenerCotizacionesUseCase(repositorio),
        convertirCotizacion = ConvertirCotizacionUseCase(),
        analytics = analytics,
        dispatchers = TestDispatcherProvider(),
    )

    private fun completa(origen: OrigenDatos = OrigenDatos.RED) =
        CotizacionesDePrueba.instantaneaCompleta(obtenidoEn = t0).copy(origen = origen)

    @Test
    fun `arranca cargando y pasa a listo cuando el repositorio responde`() = runTest {
        val puerta = CompletableDeferred<Unit>()
        repositorio.puerta = puerta
        repositorio.respuesta = completa()

        val viewModel = crearViewModel()
        assertEquals(FasePrecios.CARGANDO, viewModel.uiState.value.fase)
        assertTrue(viewModel.uiState.value.filas.isEmpty())

        puerta.complete(Unit)

        assertEquals(FasePrecios.LISTO, viewModel.uiState.value.fase)
    }

    @Test
    fun `lista los cinco metales en orden con el precio por gramo`() = runTest {
        repositorio.respuesta = completa()

        val estado = crearViewModel().uiState.value

        assertEquals(FasePrecios.LISTO, estado.fase)
        assertEquals(MetalCotizado.entries, estado.filas.map { it.metal })
        assertEquals("148,10", estado.filas.first().precioFormateado)
        assertEquals(UnidadPrecio.GRAMO, estado.filas.first().unidad)
        assertEquals(Tendencia.BAJA, estado.filas.first().tendencia)
        assertEquals(UnidadPrecio.GRAMO, estado.unidad)
        assertEquals(OrigenDatos.RED, estado.origen)
        assertEquals(t0, estado.ultimaConsultaEpochMillis)
        assertNull(estado.errorGlobal)
        assertFalse(estado.puedeReintentar)
        assertFalse(estado.reintentando)
    }

    @Test
    fun `el detalle inicial es el del oro`() = runTest {
        repositorio.respuesta = completa()

        val detalle = crearViewModel().uiState.value.detalle

        assertNotNull(detalle)
        assertEquals(MetalCotizado.ORO, detalle!!.metal)
        assertEquals("148,13", detalle.ask)
        assertEquals("148,07", detalle.bid)
        assertEquals("-0,97", detalle.variacionPorcentaje)
        assertEquals(Tendencia.BAJA, detalle.tendencia)
        assertEquals(CotizacionesDePrueba.INSTANTE_MERCADO_MUESTRA, detalle.instanteMercadoEpochMillis)
        assertFalse(detalle.desactualizada)
    }

    @Test
    fun `los precios pequenos llevan cuatro decimales`() = runTest {
        repositorio.respuesta = conCobre(cotizacion(metal = MetalCotizado.COBRE, mid = "2.49", ask = "2.5", bid = "2.48", obtenidoEn = t0))

        val cobre = crearViewModel().uiState.value.filas.single { it.metal == MetalCotizado.COBRE }

        assertEquals("0,0801", cobre.precioFormateado)
    }

    @Test
    fun `una unidad de origen desconocida se muestra sin convertir`() = runTest {
        repositorio.respuesta = conCobre(
            cotizacion(metal = MetalCotizado.COBRE, mid = "2.49", unidadOrigen = null, etiquetaUnidadOrigen = "LB", obtenidoEn = t0),
        )

        val cobre = crearViewModel().uiState.value.filas.single { it.metal == MetalCotizado.COBRE }

        assertEquals("2,49", cobre.precioFormateado)
        assertNull(cobre.unidad)
        assertEquals("LB", cobre.etiquetaUnidadOrigen)
    }

    @Test
    fun `sin medio, venta ni compra la fila no tiene precio`() = runTest {
        repositorio.respuesta = conCobre(cotizacion(metal = MetalCotizado.COBRE, mid = "0", ask = "0", bid = "0", obtenidoEn = t0))

        val cobre = crearViewModel().uiState.value.filas.single { it.metal == MetalCotizado.COBRE }

        assertNull(cobre.precioFormateado)
        assertNull(cobre.error)
    }

    @Test
    fun `registra la pantalla y la carga desde la red`() = runTest {
        repositorio.respuesta = completa()

        crearViewModel()

        verify(exactly = 1) { analytics.logScreenView("herramientas_precios") }
        verify(exactly = 1) {
            analytics.logEvent("herramientas_precios_cargados", mapOf("fuente" to "red", "parcial" to "false"))
        }
    }

    @Test
    fun `desde la cache la fuente es cache y el estado lo dice`() = runTest {
        repositorio.respuesta = completa(origen = OrigenDatos.CACHE)

        val estado = crearViewModel().uiState.value

        assertEquals(OrigenDatos.CACHE, estado.origen)
        verify(exactly = 1) {
            analytics.logEvent("herramientas_precios_cargados", mapOf("fuente" to "cache", "parcial" to "false"))
        }
    }

    @Test
    fun `solo se consulta al repositorio una vez al nacer`() = runTest {
        repositorio.respuesta = completa()

        crearViewModel()

        assertEquals(1, repositorio.llamadas)
    }

    // --- US2: unidad e información del mercado ---

    @Test
    fun `cambiar a kilo y onza convierte todas las filas sin volver a consultar`() = runTest {
        repositorio.respuesta = completa()
        val viewModel = crearViewModel()

        viewModel.onUnidadSeleccionada(UnidadPrecio.KILO)
        assertEquals("148.099,20", viewModel.uiState.value.filas.first().precioFormateado)
        assertEquals(UnidadPrecio.KILO, viewModel.uiState.value.unidad)
        assertEquals(UnidadPrecio.KILO, viewModel.uiState.value.filas.first().unidad)

        viewModel.onUnidadSeleccionada(UnidadPrecio.ONZA_TROY)
        assertEquals("4.606,40", viewModel.uiState.value.filas.first().precioFormateado)

        viewModel.onUnidadSeleccionada(UnidadPrecio.GRAMO)
        assertEquals("148,10", viewModel.uiState.value.filas.first().precioFormateado)

        assertEquals(1, repositorio.llamadas)
        verify(exactly = 1) { analytics.logEvent("herramientas_unidad_cambiada", mapOf("unidad" to "kilo")) }
        verify(exactly = 1) { analytics.logEvent("herramientas_unidad_cambiada", mapOf("unidad" to "onza_troy")) }
        verify(exactly = 1) { analytics.logEvent("herramientas_unidad_cambiada", mapOf("unidad" to "gramo")) }
    }

    @Test
    fun `reseleccionar la misma unidad no emite telemetria`() = runTest {
        repositorio.respuesta = completa()
        val viewModel = crearViewModel()

        viewModel.onUnidadSeleccionada(UnidadPrecio.GRAMO)

        verify(exactly = 0) { analytics.logEvent("herramientas_unidad_cambiada", any()) }
    }

    @Test
    fun `en kilo el detalle convierte los importes pero no el porcentaje`() = runTest {
        repositorio.respuesta = completa()
        val viewModel = crearViewModel()

        viewModel.onUnidadSeleccionada(UnidadPrecio.KILO)
        val detalle = viewModel.uiState.value.detalle!!

        val askEsperado = FormatoPrecios.importe(
            ConversorUnidadesPrecio.convertir(java.math.BigDecimal("4607.4"), UnidadPrecio.ONZA_TROY, UnidadPrecio.KILO),
        )
        assertEquals(askEsperado, detalle.ask)
        assertEquals("148.131,35", detalle.ask)
        assertEquals("-0,97", detalle.variacionPorcentaje)
        assertEquals(UnidadPrecio.KILO, detalle.unidad)
        assertEquals("OUNCE", detalle.etiquetaUnidadOrigen)
    }

    @Test
    fun `pulsar un metal cambia el detalle y lo registra`() = runTest {
        repositorio.respuesta = completa()
        val viewModel = crearViewModel()

        viewModel.onMetalSeleccionado(MetalCotizado.PLATA)

        assertEquals(MetalCotizado.PLATA, viewModel.uiState.value.seleccionado)
        assertEquals(MetalCotizado.PLATA, viewModel.uiState.value.detalle?.metal)
        assertEquals(1, repositorio.llamadas)
        verify(exactly = 1) { analytics.logEvent("herramientas_metal_seleccionado", mapOf("metal" to "plata")) }

        viewModel.onMetalSeleccionado(MetalCotizado.PLATA)
        verify(exactly = 1) { analytics.logEvent("herramientas_metal_seleccionado", any()) }
    }

    @Test
    fun `un metal sin dato alguno no tiene detalle`() = runTest {
        repositorio.respuesta = InstantaneaCotizaciones(
            resultados = MetalCotizado.entries.filter { it != MetalCotizado.RODIO }.associateWith { exito(it, obtenidoEn = t0) },
            instanteIntentoEpochMillis = t0,
            origen = OrigenDatos.RED,
        )
        val viewModel = crearViewModel()

        viewModel.onMetalSeleccionado(MetalCotizado.RODIO)

        assertNull(viewModel.uiState.value.detalle)
        assertNull(viewModel.uiState.value.filas.single { it.metal == MetalCotizado.RODIO }.precioFormateado)
    }

    @Test
    fun `con unidad de origen desconocida el detalle va en origen y el selector no lo altera`() = runTest {
        repositorio.respuesta = conCobre(
            cotizacion(metal = MetalCotizado.COBRE, mid = "2.49", ask = "2.5", unidadOrigen = null, etiquetaUnidadOrigen = "LB", obtenidoEn = t0),
        )
        val viewModel = crearViewModel()
        viewModel.onMetalSeleccionado(MetalCotizado.COBRE)
        viewModel.onUnidadSeleccionada(UnidadPrecio.KILO)

        val detalle = viewModel.uiState.value.detalle!!
        assertEquals("2,50", detalle.ask)
        assertNull(detalle.unidad)
        assertEquals("LB", detalle.etiquetaUnidadOrigen)
        assertEquals("2,49", viewModel.uiState.value.filas.single { it.metal == MetalCotizado.COBRE }.precioFormateado)
    }

    private fun conCobre(cobre: com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionMetal): InstantaneaCotizaciones =
        InstantaneaCotizaciones(
            resultados = MetalCotizado.entries.associateWith { exito(it, obtenidoEn = t0) } +
                (MetalCotizado.COBRE to ResultadoCotizacion.Exito(cobre)),
            instanteIntentoEpochMillis = t0,
            origen = OrigenDatos.RED,
        )
}
