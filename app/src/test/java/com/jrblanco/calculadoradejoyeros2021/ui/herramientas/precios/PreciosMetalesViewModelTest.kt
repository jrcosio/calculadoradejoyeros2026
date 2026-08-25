package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.precios

import com.jrblanco.calculadoradejoyeros2021.core.util.TestDispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.domain.model.ConversorUnidadesPrecio
import com.jrblanco.calculadoradejoyeros2021.data.repository.FakeCotizacionesRepository
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.cotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.exito
import com.jrblanco.calculadoradejoyeros2021.domain.model.InstantaneaCotizaciones
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MotivoErrorCotizacion
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

    // --- US5: fallos, espera y reintento ---

    @Test
    fun `un metal fallido deja la fase parcial con su motivo y permite reintentar`() = runTest {
        repositorio.respuesta = CotizacionesDePrueba.instantaneaParcial(obtenidoEn = t0).copy(origen = OrigenDatos.RED)

        val estado = crearViewModel().uiState.value

        assertEquals(FasePrecios.PARCIAL, estado.fase)
        val rodio = estado.filas.single { it.metal == MetalCotizado.RODIO }
        assertEquals(MotivoErrorCotizacion.SIN_CONEXION, rodio.error)
        assertNull(rodio.precioFormateado)
        assertFalse(rodio.desactualizada)
        assertEquals(4, estado.filas.count { it.precioFormateado != null })
        assertTrue(estado.puedeReintentar)
        assertNull(estado.errorGlobal)
        verify(exactly = 1) {
            analytics.logEvent("herramientas_precios_cargados", mapOf("fuente" to "red", "parcial" to "true"))
        }
    }

    @Test
    fun `un fallo con ultima conocida muestra el precio desactualizado`() = runTest {
        val antigua = cotizacion(metal = MetalCotizado.RODIO, obtenidoEn = t0 - 3_600_000L)
        repositorio.respuesta = InstantaneaCotizaciones(
            resultados = MetalCotizado.entries.associateWith { exito(it, obtenidoEn = t0) } +
                (MetalCotizado.RODIO to CotizacionesDePrueba.error(MetalCotizado.RODIO, ultimaConocida = antigua)),
            instanteIntentoEpochMillis = t0,
            origen = OrigenDatos.RED,
        )
        val viewModel = crearViewModel()

        val rodio = viewModel.uiState.value.filas.single { it.metal == MetalCotizado.RODIO }
        assertEquals("148,10", rodio.precioFormateado)
        assertTrue(rodio.desactualizada)
        assertEquals(MotivoErrorCotizacion.SIN_CONEXION, rodio.error)

        viewModel.onMetalSeleccionado(MetalCotizado.RODIO)
        assertTrue(viewModel.uiState.value.detalle!!.desactualizada)
    }

    @Test
    fun `si fallan los cinco la fase es error con el motivo dominante`() = runTest {
        repositorio.respuesta = InstantaneaCotizaciones(
            resultados = MetalCotizado.entries.associateWith { CotizacionesDePrueba.error(it, MotivoErrorCotizacion.SIN_CONEXION) } +
                (MetalCotizado.ORO to CotizacionesDePrueba.error(MetalCotizado.ORO, MotivoErrorCotizacion.SERVIDOR)),
            instanteIntentoEpochMillis = t0,
            origen = OrigenDatos.RED,
        )

        val estado = crearViewModel().uiState.value

        assertEquals(FasePrecios.ERROR, estado.fase)
        assertEquals(MotivoErrorCotizacion.SIN_CONEXION, estado.errorGlobal)
        assertTrue(estado.puedeReintentar)
        assertTrue(estado.filas.all { it.precioFormateado == null })
        assertNull(estado.ultimaConsultaEpochMillis)
        assertNull(estado.detalle)
        verify(exactly = 1) { analytics.logEvent("herramientas_precios_error", mapOf("motivo" to "sin_conexion")) }
        verify(exactly = 0) { analytics.logEvent("herramientas_precios_cargados", any()) }
    }

    @Test
    fun `con cinco errores y ultimas conocidas se ven los precios antiguos`() = runTest {
        repositorio.respuesta = InstantaneaCotizaciones(
            resultados = MetalCotizado.entries.associateWith {
                CotizacionesDePrueba.error(it, ultimaConocida = cotizacion(metal = it, obtenidoEn = t0 - 7_200_000L))
            },
            instanteIntentoEpochMillis = t0,
            origen = OrigenDatos.RED,
        )

        val estado = crearViewModel().uiState.value

        assertEquals(FasePrecios.ERROR, estado.fase)
        assertTrue(estado.filas.all { it.precioFormateado == "148,10" && it.desactualizada })
        assertNotNull(estado.detalle)
        assertTrue(estado.detalle!!.desactualizada)
    }

    @Test
    fun `sin credencial no se ofrece reintentar`() = runTest {
        repositorio.respuesta = InstantaneaCotizaciones(
            resultados = MetalCotizado.entries.associateWith { CotizacionesDePrueba.error(it, MotivoErrorCotizacion.SIN_CREDENCIAL) },
            instanteIntentoEpochMillis = t0,
            origen = OrigenDatos.RED,
        )

        val estado = crearViewModel().uiState.value

        assertEquals(FasePrecios.ERROR, estado.fase)
        assertEquals(MotivoErrorCotizacion.SIN_CREDENCIAL, estado.errorGlobal)
        assertFalse(estado.puedeReintentar)
    }

    @Test
    fun `reintentar vuelve a consultar y se ignora mientras hay una carga en curso`() = runTest {
        repositorio.respuesta = CotizacionesDePrueba.instantaneaParcial(obtenidoEn = t0).copy(origen = OrigenDatos.RED)
        val viewModel = crearViewModel()
        assertEquals(1, repositorio.llamadas)

        val puerta = CompletableDeferred<Unit>()
        repositorio.puerta = puerta
        viewModel.onReintentar()
        assertEquals(2, repositorio.llamadas)
        assertTrue(viewModel.uiState.value.reintentando)
        assertEquals(FasePrecios.PARCIAL, viewModel.uiState.value.fase)

        viewModel.onReintentar()
        assertEquals(2, repositorio.llamadas)

        repositorio.respuesta = completa()
        puerta.complete(Unit)
        assertFalse(viewModel.uiState.value.reintentando)
        assertEquals(FasePrecios.LISTO, viewModel.uiState.value.fase)
    }

    @Test
    fun `reintentar demasiado pronto avisa de la espera y el siguiente exito la quita`() = runTest {
        val parcial = CotizacionesDePrueba.instantaneaParcial(obtenidoEn = t0)
        repositorio.respuesta = parcial.copy(origen = OrigenDatos.RED)
        val viewModel = crearViewModel()
        assertFalse(viewModel.uiState.value.avisoEspera)

        repositorio.respuesta = parcial.copy(origen = OrigenDatos.CACHE_EN_ESPERA)
        viewModel.onReintentar()
        assertTrue(viewModel.uiState.value.avisoEspera)
        assertEquals(FasePrecios.PARCIAL, viewModel.uiState.value.fase)

        repositorio.respuesta = completa()
        viewModel.onReintentar()
        assertFalse(viewModel.uiState.value.avisoEspera)
        assertEquals(FasePrecios.LISTO, viewModel.uiState.value.fase)
    }

    @Test
    fun `una excepcion inesperada va a crashlytics y deja error desconocido conservando las filas`() = runTest {
        repositorio.respuesta = completa()
        val viewModel = crearViewModel()

        repositorio.excepcion = IllegalStateException("bug")
        viewModel.onReintentar()

        val estado = viewModel.uiState.value
        assertEquals(FasePrecios.ERROR, estado.fase)
        assertEquals(MotivoErrorCotizacion.DESCONOCIDO, estado.errorGlobal)
        assertEquals(5, estado.filas.count { it.precioFormateado != null })
        assertTrue(estado.puedeReintentar)
        assertFalse(estado.reintentando)
        verify(exactly = 1) { analytics.recordError(any()) }
    }

    @Test
    fun `las respuestas invalidas con causa van a crashlytics y los fallos de red no`() = runTest {
        val causa = IllegalStateException("json raro")
        repositorio.respuesta = InstantaneaCotizaciones(
            resultados = MetalCotizado.entries.associateWith { exito(it, obtenidoEn = t0) } +
                (MetalCotizado.RODIO to ResultadoCotizacion.Error(MetalCotizado.RODIO, MotivoErrorCotizacion.RESPUESTA_INVALIDA, null, causa)) +
                (MetalCotizado.COBRE to ResultadoCotizacion.Error(MetalCotizado.COBRE, MotivoErrorCotizacion.SIN_CONEXION, null, IllegalStateException("red"))),
            instanteIntentoEpochMillis = t0,
            origen = OrigenDatos.RED,
        )

        crearViewModel()

        verify(exactly = 1) { analytics.recordError(causa) }
        verify(exactly = 1) { analytics.recordError(any()) }
    }

    @Test
    fun `el cobre por libra se convierte a la unidad elegida como los demas`() = runTest {
        repositorio.respuesta = conCobre(
            cotizacion(metal = MetalCotizado.COBRE, mid = "5.612946820240344", ask = "5.613238349729337", bid = "5.612655290751351",
                unidadOrigen = UnidadPrecio.LIBRA, etiquetaUnidadOrigen = "POUND", obtenidoEn = t0),
        )
        val viewModel = crearViewModel()

        val enGramos = viewModel.uiState.value.filas.single { it.metal == MetalCotizado.COBRE }
        assertEquals("0,0124", enGramos.precioFormateado)
        assertEquals(UnidadPrecio.GRAMO, enGramos.unidad)

        viewModel.onUnidadSeleccionada(UnidadPrecio.KILO)
        assertEquals("12,37", viewModel.uiState.value.filas.single { it.metal == MetalCotizado.COBRE }.precioFormateado)
    }

    private fun conCobre(cobre: com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionMetal): InstantaneaCotizaciones =
        InstantaneaCotizaciones(
            resultados = MetalCotizado.entries.associateWith { exito(it, obtenidoEn = t0) } +
                (MetalCotizado.COBRE to ResultadoCotizacion.Exito(cobre)),
            instanteIntentoEpochMillis = t0,
            origen = OrigenDatos.RED,
        )
}
