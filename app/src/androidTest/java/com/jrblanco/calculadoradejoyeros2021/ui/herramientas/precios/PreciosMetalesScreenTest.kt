package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.precios

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.OrigenDatos
import com.jrblanco.calculadoradejoyeros2021.domain.model.Tendencia
import com.jrblanco.calculadoradejoyeros2021.domain.model.UnidadPrecio
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
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
}
