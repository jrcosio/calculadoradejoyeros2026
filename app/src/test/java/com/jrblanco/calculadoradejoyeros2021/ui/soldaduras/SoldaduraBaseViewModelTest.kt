package com.jrblanco.calculadoradejoyeros2021.ui.soldaduras

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraBaseInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraBaseUseCase
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Los casos de uso entran reales, sin mock: el motor es puro y determinista. Solo se
 * mockea la telemetría.
 */
class SoldaduraBaseViewModelTest {

    private val analytics = mockk<AnalyticsRepository>(relaxed = true)

    private fun crearViewModel() = SoldaduraBaseViewModel(
        calcularBase = CalcularSoldaduraBaseUseCase(),
        calcularBaseInversa = CalcularSoldaduraBaseInversaUseCase(),
        analytics = analytics,
    )

    @Test
    fun `el estado inicial es modo desde el oro con campo vacio y sin resultado`() = runTest {
        crearViewModel().uiState.test {
            val estado = awaitItem()
            assertEquals(ModoEntradaSoldadura.DESDE_METAL, estado.modo)
            assertEquals("", estado.cantidadTexto)
            assertNull(estado.resultado)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `registra la vista de pantalla al construirse con su serie nueva`() {
        crearViewModel()
        verify(exactly = 1) { analytics.logScreenView("soldadura_base") }
    }

    @Test
    fun `test 6 formateado - 10 gramos de oro reparten la receta patron de la base`() {
        val viewModel = crearViewModel()

        viewModel.onCantidadCambiada("10")

        val resultado = viewModel.uiState.value.resultado
        // Los valores del documento (§5.2), NO los del mockup, que van intercambiados.
        assertEquals(
            listOf(
                FilaSoldadura(IngredienteSoldadura.COBRE, "0,540"),
                FilaSoldadura(IngredienteSoldadura.PLATA_FINA, "0,800"),
                FilaSoldadura(IngredienteSoldadura.ZINC, "0,920"),
                FilaSoldadura(IngredienteSoldadura.CADMIO, "1,000"),
            ),
            resultado?.filas,
        )
        assertEquals("13,260", resultado?.totalFormateado)
    }

    @Test
    fun `con 7 gramos la receta escala por 0,7`() {
        val viewModel = crearViewModel()

        viewModel.onCantidadCambiada("7")

        val resultado = viewModel.uiState.value.resultado
        assertEquals(
            listOf("0,378", "0,560", "0,644", "0,700"),
            resultado?.filas?.map { it.gramosFormateados },
        )
        assertEquals("9,282", resultado?.totalFormateado)
    }

    @Test
    fun `coma y punto producen el mismo resultado`() {
        val conComa = crearViewModel().apply { onCantidadCambiada("7,5") }
        val conPunto = crearViewModel().apply { onCantidadCambiada("7.5") }

        assertEquals(conComa.uiState.value.resultado, conPunto.uiState.value.resultado)
    }

    @Test
    fun `las entradas invalidas no producen resultado`() {
        listOf("", "0", "-1", "abc", "1.2,3", "  ").forEach { texto ->
            val viewModel = crearViewModel()

            viewModel.onCantidadCambiada(texto)

            assertNull("con «$texto» no debe haber resultado", viewModel.uiState.value.resultado)
        }
    }

    @Test
    fun `teclear no duplica el evento de calculo`() {
        val viewModel = crearViewModel()

        viewModel.onCantidadCambiada("10")
        viewModel.onCantidadCambiada("100")

        verify(exactly = 1) {
            analytics.logEvent("soldadura_base_calculado", mapOf("modo" to "desde_metal"))
        }
    }
}
