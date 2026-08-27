package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.precios

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MotivoErrorCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.OrigenDatos
import com.jrblanco.calculadoradejoyeros2021.domain.model.Tendencia
import com.jrblanco.calculadoradejoyeros2021.domain.model.UnidadPrecio
import com.jrblanco.calculadoradejoyeros2021.ui.EnIdiomaDeTest
import com.jrblanco.calculadoradejoyeros2021.ui.contextoDeTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Monta [PreciosMetalesContent] directo, sin Koin, con estados precocinados. */
class PreciosMetalesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexto = contextoDeTest()
    private fun texto(id: Int) = contexto.getString(id)
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
            EnIdiomaDeTest {
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
        // El símbolo se pinta dos veces —en la fila y en la píldora del detalle—, así que se
        // acota a la fila del oro en vez de exigir que sea único.
        composeRule.onNode(hasText("AU") and hasText(texto(R.string.metal_oro))).assertExists()
    }

    @Test
    fun listo_pintaLaNotaYLaFuente() {
        montar(estadoListo)

        composeRule.onNodeWithText(texto(R.string.precios_nota_orientativos)).assertExists()
        composeRule.onNodeWithText(
            texto(R.string.precios_fuente, texto(R.string.precios_fuente_nombre)),
        ).assertExists()
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

        // Cada etiqueta de unidad aparece dos veces: en su chip y como valor de la celda
        // «Unidad» del detalle. Se desambigua por semántica —solo el chip es pulsable— en vez
        // de por texto.
        listOf(
            R.string.precios_unidad_gramo,
            R.string.precios_unidad_kilo,
            R.string.precios_unidad_onza,
        ).forEach { unidad ->
            composeRule.onNode(hasText(texto(unidad)) and hasClickAction()).assertExists()
        }
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
            // `onFirst` y no `onNodeWithText`: «Unidad» es el texto de dos claves distintas
            // —`precios_seccion_unidad`, la cabecera del selector, y `precios_detalle_unidad`, la
            // etiqueta de la celda—, así que lo que se prueba es que la etiqueta está, no que sea
            // única en la pantalla.
        ).forEach { composeRule.onAllNodesWithText(texto(it)).onFirst().assertExists() }
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
