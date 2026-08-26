package com.jrblanco.calculadoradejoyeros2021.ui.soldaduras

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.core.util.TestDispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.data.repository.FakeFavoritosRepository
import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.FavoritosDePrueba
import com.jrblanco.calculadoradejoyeros2021.domain.model.ModoEntradaSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraBaseInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraBaseUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.GuardarFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ObtenerFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.ui.favoritos.AvisoFavorito
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Los casos de uso entran reales, sin mock: el motor es puro y determinista. Solo se
 * mockea la telemetría.
 */
class SoldaduraBaseViewModelTest {

    private val analytics = mockk<AnalyticsRepository>(relaxed = true)
    private val favoritos = FakeFavoritosRepository()

    private fun crearViewModel() = SoldaduraBaseViewModel(
        calcularBase = CalcularSoldaduraBaseUseCase(),
        calcularBaseInversa = CalcularSoldaduraBaseInversaUseCase(),
        guardarFavorito = GuardarFavoritoUseCase(favoritos),
        obtenerFavorito = ObtenerFavoritoUseCase(favoritos),
        analytics = analytics,
        dispatchers = TestDispatcherProvider(),
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

    // --- Modo peso de base deseado (US5, §5.2, FR-012, FR-023) ---

    @Test
    fun `cambiar de modo vacia cantidad y resultado`() {
        val viewModel = crearViewModel()
        viewModel.onCantidadCambiada("10")

        viewModel.onModoCambiado(ModoEntradaSoldadura.PESO_FINAL)

        val estado = viewModel.uiState.value
        assertEquals(ModoEntradaSoldadura.PESO_FINAL, estado.modo)
        assertEquals("", estado.cantidadTexto)
        assertNull(estado.resultado)
    }

    @Test
    fun `base inversa - 13,26 gramos recuperan los 10 de oro fino con todas las filas`() {
        val viewModel = crearViewModel()
        viewModel.onModoCambiado(ModoEntradaSoldadura.PESO_FINAL)

        viewModel.onCantidadCambiada("13,26")

        val resultado = viewModel.uiState.value.resultado
        // En inverso el oro introducido sí se pinta, el primero (FR-022).
        assertEquals(
            listOf(
                FilaSoldadura(IngredienteSoldadura.ORO_24K, "10,000"),
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
    fun `base inversa - 10 gramos documentan la nota de redondeo`() {
        val viewModel = crearViewModel()
        viewModel.onModoCambiado(ModoEntradaSoldadura.PESO_FINAL)

        viewModel.onCantidadCambiada("10")

        val resultado = viewModel.uiState.value.resultado
        // División infinita 10÷13,26: la suma visible queda en 9,999 y lo advierte la
        // nota de §8.3; ningún ingrediente se ajusta para cuadrarla (FR-021).
        assertEquals(
            listOf("7,541", "0,407", "0,603", "0,694", "0,754"),
            resultado?.filas?.map { it.gramosFormateados },
        )
        assertEquals("10,000", resultado?.totalFormateado)
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

    // --- Limpiar y favoritos (US6, FR-024) ---

    @Test
    fun `limpiar vuelve al estado inicial y rearma la telemetria`() {
        val viewModel = crearViewModel()
        viewModel.onCantidadCambiada("10")

        viewModel.onLimpiar()
        viewModel.onCantidadCambiada("10")

        assertEquals(SoldaduraBaseUiState(cantidadTexto = "10").modo, viewModel.uiState.value.modo)
        verify(exactly = 2) {
            analytics.logEvent("soldadura_base_calculado", mapOf("modo" to "desde_metal"))
        }
    }

    // --- Favoritos (009) ---

    @Test
    fun `guardar manda la cantidad y el modo`() = runTest {
        val viewModel = crearViewModel()
        viewModel.onCantidadCambiada("10")

        viewModel.onGuardarFavoritos()

        assertEquals(
            EntradasFavorito.SoldaduraBase(BigDecimal("10"), ModoEntradaSoldadura.DESDE_METAL),
            favoritos.guardados.single(),
        )
        assertEquals(AvisoFavorito.GUARDADO, viewModel.uiState.value.avisoFavorito)
        verify(exactly = 1) {
            analytics.logEvent("soldadura_base_favorito_guardado", mapOf("resultado" to "nuevo"))
        }
    }

    @Test
    fun `guardar con el campo vacio pide completar el calculo`() = runTest {
        val viewModel = crearViewModel()

        viewModel.onGuardarFavoritos()

        assertTrue(favoritos.guardados.isEmpty())
        assertEquals(AvisoFavorito.SIN_DATOS, viewModel.uiState.value.avisoFavorito)
    }

    @Test
    fun `cargar un favorito fija modo y cantidad de una vez`() = runTest {
        favoritos.flujo.value = listOf(
            FavoritosDePrueba.favorito(
                id = 5L,
                entradas = EntradasFavorito.SoldaduraBase(
                    BigDecimal("13"),
                    ModoEntradaSoldadura.PESO_FINAL,
                ),
            ),
        )
        val viewModel = crearViewModel()

        viewModel.cargarFavorito(5L)

        val estado = viewModel.uiState.value
        assertEquals(ModoEntradaSoldadura.PESO_FINAL, estado.modo)
        // `onModoCambiado` vacía la cantidad por FR-023: aquí no puede pasar.
        assertEquals("13", estado.cantidadTexto)
        assertNotNull(estado.resultado)
    }
}
