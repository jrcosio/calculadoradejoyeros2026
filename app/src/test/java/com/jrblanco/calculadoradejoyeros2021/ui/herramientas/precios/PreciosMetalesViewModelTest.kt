package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.precios

import com.jrblanco.calculadoradejoyeros2021.core.util.TestDispatcherProvider
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

    private fun conCobre(cobre: com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionMetal): InstantaneaCotizaciones =
        InstantaneaCotizaciones(
            resultados = MetalCotizado.entries.associateWith { exito(it, obtenidoEn = t0) } +
                (MetalCotizado.COBRE to ResultadoCotizacion.Exito(cobre)),
            instanteIntentoEpochMillis = t0,
            origen = OrigenDatos.RED,
        )
}
