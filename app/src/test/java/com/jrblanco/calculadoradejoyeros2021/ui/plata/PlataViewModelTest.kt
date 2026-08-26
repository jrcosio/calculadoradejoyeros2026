package com.jrblanco.calculadoradejoyeros2021.ui.plata

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.core.util.TestDispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.data.repository.FakeFavoritosRepository
import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoPlata
import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.FavoritosDePrueba
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularAleacionPlataUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.GuardarFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ObtenerFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.ui.favoritos.AvisoFavorito
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El caso de uso entra real, sin mock: el motor es puro y determinista, y así el test
 * verifica de paso que ViewModel y motor hablan el mismo idioma. Solo se mockea la
 * telemetría.
 */
class PlataViewModelTest {

    private val analytics = mockk<AnalyticsRepository>(relaxed = true)
    private val favoritos = FakeFavoritosRepository()

    private fun crearViewModel() = PlataViewModel(
        calcularAleacion = CalcularAleacionPlataUseCase(),
        guardarFavorito = GuardarFavoritoUseCase(favoritos),
        obtenerFavorito = ObtenerFavoritoUseCase(favoritos),
        analytics = analytics,
        dispatchers = TestDispatcherProvider(),
    )

    @Test
    fun `el estado inicial es campo vacio con 925 y sin resultado`() = runTest {
        crearViewModel().uiState.test {
            val estado = awaitItem()
            assertEquals("", estado.cantidadTexto)
            assertEquals(LeyPlata.LEY_925, estado.ley)
            assertNull(estado.resultado)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `registra la vista de pantalla al construirse con el nombre del placeholder`() {
        crearViewModel()
        verify(exactly = 1) { analytics.logScreenView("plata") }
    }

    // --- Los cuatro casos obligatorios de §21, con 10 g de plata fina ---

    @Test
    fun `los cuatro casos del documento se muestran truncados a milesimas`() {
        val esperados = mapOf(
            LeyPlata.LEY_950 to ("0,515" to "10,515"),
            LeyPlata.LEY_925 to ("0,800" to "10,800"),
            LeyPlata.LEY_900 to ("1,100" to "11,100"),
            LeyPlata.LEY_800 to ("2,487" to "12,487"),
        )

        esperados.forEach { (ley, valores) ->
            val (cobre, total) = valores
            val viewModel = crearViewModel()
            viewModel.onCantidadCambiada("10")
            viewModel.onLeySeleccionada(ley)

            val resultado = viewModel.uiState.value.resultado
            assertEquals("cobre de $ley", cobre, resultado?.cobreFormateado)
            assertEquals("total de $ley", total, resultado?.totalFormateado)
        }
    }

    @Test
    fun `el caso del mockup - 25 gramos hacia 925 muestran 2 gramos de cobre`() {
        val viewModel = crearViewModel()

        viewModel.onCantidadCambiada("25")

        val resultado = viewModel.uiState.value.resultado
        assertEquals("2,000", resultado?.cobreFormateado)
        assertEquals("27,000", resultado?.totalFormateado)
    }

    // --- La desviación deliberada: truncar, no redondear a la media (FR-011, SC-003) ---

    @Test
    fun `100 gramos hacia 950 muestran 5,157 y no 5,158`() {
        val viewModel = crearViewModel()

        viewModel.onCantidadCambiada("100")
        viewModel.onLeySeleccionada(LeyPlata.LEY_950)

        val resultado = viewModel.uiState.value.resultado
        // Con HALF_UP saldría «5,158» y pesar esa cantidad daría 949,999‰: por debajo de
        // la ley objetivo, que la Ley 17/1985 no permite. Es el ejemplo de §17 y §19.
        assertEquals("5,157", resultado?.cobreFormateado)
        assertEquals("105,157", resultado?.totalFormateado)
    }

    @Test
    fun `pesar el cobre que muestra la pantalla nunca baja de la ley objetivo`() {
        val cantidades = listOf("0.5", "1", "7.77", "10", "12.35", "100", "333.333")

        cantidades.forEach { texto ->
            LeyPlata.entries.forEach { ley ->
                val viewModel = crearViewModel()
                viewModel.onCantidadCambiada(texto)
                viewModel.onLeySeleccionada(ley)

                val mostrado = viewModel.uiState.value.resultado!!.cobreFormateado
                // Se reconstruye desde la CADENA formateada, que es lo que el joyero pesa.
                val cobrePesado = BigDecimal(mostrado.replace(',', '.'))
                val masa = BigDecimal(texto)
                val plataPura = masa.multiply(CalculoPlata.FINURA_ORIGEN)
                val leyPractica = plataPura.divide(
                    masa.add(cobrePesado),
                    CalculoPlata.ESCALA,
                    RoundingMode.DOWN,
                )

                assertTrue(
                    "$texto g hacia $ley: pesando $mostrado g de cobre la ley práctica " +
                        "($leyPractica) queda por debajo de ${ley.finura}",
                    leyPractica >= ley.finura,
                )
            }
        }
    }

    @Test
    fun `un cobre que no llega a la milesima se muestra como cero`() {
        val viewModel = crearViewModel()

        // 0,001 g hacia 925‰ pide 0,00008 g de cobre: por debajo de lo que pesa una
        // balanza de milésimas, así que la pantalla muestra 0,000 y el joyero lo entiende.
        viewModel.onCantidadCambiada("0,001")

        val resultado = viewModel.uiState.value.resultado
        assertEquals("0,000", resultado?.cobreFormateado)
        assertEquals("0,001", resultado?.totalFormateado)
    }

    // --- Parseo y validación (§26) ---

    @Test
    fun `la coma y el punto decimal producen el mismo resultado`() {
        val conComa = crearViewModel().apply { onCantidadCambiada("12,35") }
        val conPunto = crearViewModel().apply { onCantidadCambiada("12.35") }

        assertEquals(
            conComa.uiState.value.resultado,
            conPunto.uiState.value.resultado,
        )
        assertEquals("0,988", conComa.uiState.value.resultado?.cobreFormateado)
    }

    @Test
    fun `las entradas invalidas no producen resultado`() {
        val viewModel = crearViewModel()

        listOf("", "0", "-1", "abc", "1.2,3", "  ").forEach { texto ->
            viewModel.onCantidadCambiada(texto)
            assertNull("«$texto» no debería producir resultado", viewModel.uiState.value.resultado)
        }
    }

    @Test
    fun `cambiar la ley recalcula sin tocar la cantidad`() {
        val viewModel = crearViewModel()
        viewModel.onCantidadCambiada("10")

        viewModel.onLeySeleccionada(LeyPlata.LEY_800)

        assertEquals("10", viewModel.uiState.value.cantidadTexto)
        assertEquals("2,487", viewModel.uiState.value.resultado?.cobreFormateado)
    }

    // --- Leyes técnicas (US2) ---

    @Test
    fun `solo 950 y 900 son leyes tecnicas y calculan con normalidad`() {
        assertTrue(LeyPlata.LEY_950.esSoloTecnica)
        assertTrue(LeyPlata.LEY_900.esSoloTecnica)
        assertFalse(LeyPlata.LEY_925.esSoloTecnica)
        assertFalse(LeyPlata.LEY_800.esSoloTecnica)

        val viewModel = crearViewModel()
        viewModel.onCantidadCambiada("100")
        viewModel.onLeySeleccionada(LeyPlata.LEY_900)

        assertEquals("11,000", viewModel.uiState.value.resultado?.cobreFormateado)
        assertEquals("111,000", viewModel.uiState.value.resultado?.totalFormateado)
    }

    // --- Telemetría (FR-019) ---

    @Test
    fun `teclear la cantidad no duplica la telemetria de calculo`() {
        val viewModel = crearViewModel()

        viewModel.onCantidadCambiada("1")
        viewModel.onCantidadCambiada("10")
        viewModel.onCantidadCambiada("100")

        verify(exactly = 1) { analytics.logEvent("plata_calculado", mapOf("ley" to "925")) }
    }

    @Test
    fun `cada ley nueva registra su propio evento`() {
        val viewModel = crearViewModel()

        viewModel.onCantidadCambiada("10")
        viewModel.onLeySeleccionada(LeyPlata.LEY_950)
        viewModel.onLeySeleccionada(LeyPlata.LEY_800)

        verify(exactly = 1) { analytics.logEvent("plata_calculado", mapOf("ley" to "925")) }
        verify(exactly = 1) { analytics.logEvent("plata_calculado", mapOf("ley" to "950")) }
        verify(exactly = 1) { analytics.logEvent("plata_calculado", mapOf("ley" to "800")) }
    }

    // --- Limpiar (US3) y favoritos (US4) ---

    @Test
    fun `limpiar devuelve el estado inicial y rearma la telemetria`() {
        val viewModel = crearViewModel()
        viewModel.onCantidadCambiada("10")
        viewModel.onLeySeleccionada(LeyPlata.LEY_800)

        viewModel.onLimpiar()

        assertEquals(PlataUiState(), viewModel.uiState.value)

        viewModel.onCantidadCambiada("10")
        verify(exactly = 2) {
            // 1º al teclear 10 con 925, 2º tras limpiar: se vuelve a la misma ley.
            analytics.logEvent("plata_calculado", mapOf("ley" to "925"))
        }
    }

    @Test
    fun `limpiar deja la pantalla lista para un calculo nuevo`() {
        val viewModel = crearViewModel()
        viewModel.onCantidadCambiada("10")
        viewModel.onLimpiar()

        viewModel.onCantidadCambiada("25")

        assertEquals("2,000", viewModel.uiState.value.resultado?.cobreFormateado)
    }

    // --- Favoritos (009) ---

    @Test
    fun `guardar un calculo nuevo lo manda al repositorio y avisa`() = runTest {
        val viewModel = crearViewModel()
        viewModel.onCantidadCambiada("100")

        viewModel.onGuardarFavoritos()

        assertEquals(
            EntradasFavorito.Plata(BigDecimal("100"), LeyPlata.LEY_925),
            favoritos.guardados.single(),
        )
        assertEquals(AvisoFavorito.GUARDADO, viewModel.uiState.value.avisoFavorito)
        verify(exactly = 1) { analytics.logEvent("plata_favorito_guardado", mapOf("resultado" to "nuevo")) }
    }

    @Test
    fun `guardar lo mismo dos veces avisa de que ya estaba`() = runTest {
        val viewModel = crearViewModel()
        viewModel.onCantidadCambiada("100")
        viewModel.onGuardarFavoritos()

        favoritos.resultadoGuardar = ResultadoGuardado.YaExistia(1L)
        viewModel.onGuardarFavoritos()

        assertEquals(AvisoFavorito.REPETIDO, viewModel.uiState.value.avisoFavorito)
        verify(exactly = 1) { analytics.logEvent("plata_favorito_guardado", mapOf("resultado" to "repetido")) }
    }

    @Test
    fun `guardar con el campo vacio pide completar el calculo`() = runTest {
        val viewModel = crearViewModel()

        viewModel.onGuardarFavoritos()

        assertTrue(favoritos.guardados.isEmpty())
        assertEquals(AvisoFavorito.SIN_DATOS, viewModel.uiState.value.avisoFavorito)
    }

    @Test
    fun `cargar un favorito rellena la ley y la cantidad y es idempotente`() = runTest {
        favoritos.flujo.value = listOf(
            FavoritosDePrueba.favorito(
                id = 2L,
                entradas = EntradasFavorito.Plata(BigDecimal("250"), LeyPlata.LEY_800),
            ),
        )
        val viewModel = crearViewModel()

        viewModel.cargarFavorito(2L)

        assertEquals("250", viewModel.uiState.value.cantidadTexto)
        assertEquals(LeyPlata.LEY_800, viewModel.uiState.value.ley)
        assertNotNull(viewModel.uiState.value.resultado)

        viewModel.onCantidadCambiada("12")
        viewModel.cargarFavorito(2L)

        assertEquals("12", viewModel.uiState.value.cantidadTexto)
    }

    @Test
    fun `volver a una entrada valida vuelve a registrar el calculo`() {
        val viewModel = crearViewModel()

        viewModel.onCantidadCambiada("10")
        viewModel.onCantidadCambiada("")
        viewModel.onCantidadCambiada("10")

        verify(exactly = 2) { analytics.logEvent("plata_calculado", mapOf("ley" to "925")) }
    }
}
