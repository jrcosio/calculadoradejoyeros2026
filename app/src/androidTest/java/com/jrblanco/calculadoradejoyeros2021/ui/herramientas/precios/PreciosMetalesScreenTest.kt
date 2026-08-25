package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.precios

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MotivoErrorCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.OrigenDatos
import com.jrblanco.calculadoradejoyeros2021.domain.model.Tendencia
import com.jrblanco.calculadoradejoyeros2021.domain.model.UnidadPrecio
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Monta [PreciosMetalesContent] directo, sin Koin, con estados precocinados. */
class PreciosMetalesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private fun texto(id: Int, vararg args: Any) = contexto.getString(id, *args)

    private val filas = listOf(
        FilaMetalPrecio(MetalCotizado.ORO, "148,10", UnidadPrecio.GRAMO, null, Tendencia.BAJA, null, false),
        FilaMetalPrecio(MetalCotizado.PLATA, "1,20", UnidadPrecio.GRAMO, null, Tendencia.SUBE, null, false),
        FilaMetalPrecio(MetalCotizado.COBRE, "0,0089", UnidadPrecio.GRAMO, null, Tendencia.PLANA, null, false),
        FilaMetalPrecio(MetalCotizado.PALADIO, "34,90", UnidadPrecio.GRAMO, null, Tendencia.SUBE, null, false),
        FilaMetalPrecio(MetalCotizado.RODIO, "152,30", UnidadPrecio.GRAMO, null, Tendencia.BAJA, null, false),
    )

    private val detalleOro = DetalleMercado(
        metal = MetalCotizado.ORO,
        moneda = "EUR",
        ask = "148,13",
        bid = "148,07",
        maximo = "151,03",
        minimo = "148,04",
        variacion = "-1,46",
        variacionPorcentaje = "-0,97",
        tendencia = Tendencia.BAJA,
        unidad = UnidadPrecio.GRAMO,
        etiquetaUnidadOrigen = "OUNCE",
        instanteMercadoEpochMillis = 1_787_665_680_000L,
        desactualizada = false,
    )

    private val estadoListo = PreciosMetalesUiState(
        fase = FasePrecios.LISTO,
        filas = filas,
        detalle = detalleOro,
        origen = OrigenDatos.RED,
        ultimaConsultaEpochMillis = 1_787_670_000_000L,
    )

    private fun montar(
        uiState: PreciosMetalesUiState,
        onUnidadSeleccionada: (UnidadPrecio) -> Unit = {},
        onMetalSeleccionado: (MetalCotizado) -> Unit = {},
        onReintentar: () -> Unit = {},
    ) {
        composeRule.setContent {
            Calculadoradejoyeros2021Theme {
                PreciosMetalesContent(
                    uiState = uiState,
                    onUnidadSeleccionada = onUnidadSeleccionada,
                    onMetalSeleccionado = onMetalSeleccionado,
                    onReintentar = onReintentar,
                )
            }
        }
    }

    @Test
    fun listo_pintaLosCincoMetalesConSusPrecios() {
        montar(estadoListo)

        listOf(R.string.metal_oro, R.string.metal_plata, R.string.metal_cobre, R.string.metal_paladio, R.string.metal_rodio)
            .forEach { composeRule.onAllNodesWithText(texto(it)).assertCountEquals(1) }
        composeRule.onNodeWithText("148,10").assertExists()
        composeRule.onNodeWithText("0,0089").assertExists()
        composeRule.onNodeWithText("152,30").assertExists()
        composeRule.onNodeWithText("AU").assertExists()
    }

    @Test
    fun listo_pintaLaNotaYLaFuente() {
        montar(estadoListo)

        composeRule.onNodeWithText(texto(R.string.precios_nota_orientativos)).assertExists()
        composeRule.onNodeWithText(texto(R.string.precios_fuente)).assertExists()
        composeRule.onAllNodesWithText(texto(R.string.precios_accion_reintentar)).assertCountEquals(0)
    }

    @Test
    fun cargando_pintaElIndicadorYNoLasFilas() {
        montar(PreciosMetalesUiState())

        composeRule.onNodeWithText(texto(R.string.precios_cargando)).assertExists()
        composeRule.onAllNodesWithText(texto(R.string.metal_oro)).assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.precios_mercado_titulo)).assertCountEquals(0)
    }

    // --- US2: unidad e información del mercado ---

    @Test
    fun listo_lasTresUnidadesEstanEnElSelector() {
        montar(estadoListo)

        composeRule.onNodeWithText(texto(R.string.precios_unidad_gramo)).assertExists()
        composeRule.onNodeWithText(texto(R.string.precios_unidad_kilo)).assertExists()
        composeRule.onNodeWithText(texto(R.string.precios_unidad_onza)).assertExists()
    }

    @Test
    fun pulsarKilo_propagaLaUnidad() {
        var elegida: UnidadPrecio? = null
        montar(estadoListo, onUnidadSeleccionada = { elegida = it })

        composeRule.onNodeWithText(texto(R.string.precios_unidad_kilo)).performClick()

        assertEquals(UnidadPrecio.KILO, elegida)
    }

    @Test
    fun pulsarLaFilaDePlata_propagaElMetal() {
        var elegido: MetalCotizado? = null
        montar(estadoListo, onMetalSeleccionado = { elegido = it })

        composeRule.onNodeWithText(texto(R.string.metal_plata)).performClick()

        assertEquals(MetalCotizado.PLATA, elegido)
    }

    @Test
    fun listo_laFilaSeleccionadaEsLaDelOro() {
        montar(estadoListo)

        composeRule.onNode(hasText(texto(R.string.metal_oro)) and isSelected()).assertExists()
        composeRule.onNode(hasText(texto(R.string.metal_plata)) and isSelected()).assertDoesNotExist()
    }

    @Test
    fun conDetalle_seVenLasOchoEtiquetasYSusValores() {
        montar(estadoListo)

        listOf(
            R.string.precios_detalle_ask, R.string.precios_detalle_bid, R.string.precios_detalle_maximo,
            R.string.precios_detalle_minimo, R.string.precios_detalle_variacion, R.string.precios_detalle_variacion_pct,
            R.string.precios_detalle_unidad, R.string.precios_detalle_actualizacion,
        ).forEach { composeRule.onNodeWithText(texto(it)).assertExists() }
        composeRule.onNodeWithText("148,13").assertExists()
        composeRule.onNodeWithText("148,07").assertExists()
        composeRule.onNodeWithText("-0,97").assertExists()
        composeRule.onNodeWithText(texto(R.string.precios_mercado_metal, texto(R.string.metal_oro), "AU")).assertExists()
    }

    // --- US5: fallos, espera y reintento ---

    private val filaRodioFallida = FilaMetalPrecio(MetalCotizado.RODIO, null, null, null, null, MotivoErrorCotizacion.SIN_CONEXION, false)

    @Test
    fun parcial_muestraElMotivoEnLaFilaElAvisoYReintentar() {
        montar(estadoListo.copy(fase = FasePrecios.PARCIAL, filas = filas.dropLast(1) + filaRodioFallida, puedeReintentar = true))

        composeRule.onNodeWithText(texto(R.string.precios_error_sin_conexion)).assertExists()
        composeRule.onNodeWithText(texto(R.string.precios_aviso_parcial)).assertExists()
        composeRule.onNodeWithText(texto(R.string.precios_accion_reintentar)).assertExists()
    }

    @Test
    fun pulsarReintentar_propaga() {
        var reintentado = false
        montar(estadoListo.copy(fase = FasePrecios.PARCIAL, filas = filas.dropLast(1) + filaRodioFallida, puedeReintentar = true), onReintentar = { reintentado = true })

        composeRule.onNodeWithText(texto(R.string.precios_accion_reintentar)).performClick()

        assertEquals(true, reintentado)
    }

    @Test
    fun errorConDatoAntiguo_muestraDesactualizadoYElAvisoCompuesto() {
        val antiguas = filas.map { it.copy(error = MotivoErrorCotizacion.SIN_CONEXION, desactualizada = true) }
        montar(
            estadoListo.copy(
                fase = FasePrecios.ERROR,
                errorGlobal = MotivoErrorCotizacion.SIN_CONEXION,
                filas = antiguas,
                detalle = detalleOro.copy(desactualizada = true),
                puedeReintentar = true,
            ),
        )

        composeRule.onNodeWithText(texto(R.string.precios_error_sin_conexion) + " " + texto(R.string.precios_aviso_desactualizado)).assertExists()
        composeRule.onAllNodesWithText(texto(R.string.precios_desactualizado)).assertCountEquals(6)
        composeRule.onNodeWithText("148,10").assertExists()
    }

    @Test
    fun avisoDeEspera_seMuestra() {
        montar(estadoListo.copy(avisoEspera = true))
        composeRule.onNodeWithText(texto(R.string.precios_aviso_espera)).assertExists()
    }

    @Test
    fun sinCredencial_seExplicaYNoHayReintentar() {
        val sinDato = MetalCotizado.entries.map { FilaMetalPrecio(it, null, null, null, null, MotivoErrorCotizacion.SIN_CREDENCIAL, false) }
        montar(PreciosMetalesUiState(fase = FasePrecios.ERROR, filas = sinDato, errorGlobal = MotivoErrorCotizacion.SIN_CREDENCIAL, puedeReintentar = false))

        // El motivo va una sola vez, en el aviso global; sin ningún precio no hay tarjeta de mercado.
        composeRule.onAllNodesWithText(texto(R.string.precios_error_sin_credencial)).assertCountEquals(1)
        composeRule.onAllNodesWithText(texto(R.string.precios_accion_reintentar)).assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.precios_mercado_titulo)).assertCountEquals(0)
    }
}
