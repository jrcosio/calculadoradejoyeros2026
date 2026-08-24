package com.jrblanco.calculadoradejoyeros2021.ui.soldaduras

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraClasicaInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraClasicaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraLeyDesdeOroUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraLeyInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraPlataInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraPlataUseCase
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Los casos de uso entran reales, sin mock: el motor es puro y determinista, y así el
 * test verifica de paso que ViewModel y motor hablan el mismo idioma. Solo se mockea la
 * telemetría.
 */
class SoldadurasViewModelTest {

    private val analytics = mockk<AnalyticsRepository>(relaxed = true)

    private fun crearViewModel() = SoldadurasViewModel(
        calcularLeyDesdeOro = CalcularSoldaduraLeyDesdeOroUseCase(),
        calcularLeyInversa = CalcularSoldaduraLeyInversaUseCase(),
        calcularClasica = CalcularSoldaduraClasicaUseCase(),
        calcularClasicaInversa = CalcularSoldaduraClasicaInversaUseCase(),
        calcularPlata = CalcularSoldaduraPlataUseCase(),
        calcularPlataInversa = CalcularSoldaduraPlataInversaUseCase(),
        analytics = analytics,
    )

    private fun crearViewModelEnOroLey() = crearViewModel().apply {
        onFamiliaSeleccionada(FamiliaSoldadura.ORO_LEY)
    }

    // --- Primera visita (FR-002) ---

    @Test
    fun `el estado inicial es la primera visita - sin familia y sin resultado`() = runTest {
        crearViewModel().uiState.test {
            val estado = awaitItem()
            assertNull(estado.familia)
            assertEquals("", estado.cantidadTexto)
            assertEquals(ModoEntradaSoldadura.DESDE_METAL, estado.modo)
            assertNull(estado.resultado)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `registra la vista de pantalla al construirse con el nombre del placeholder`() {
        crearViewModel()
        verify(exactly = 1) { analytics.logScreenView("soldaduras") }
    }

    @Test
    fun `sin familia elegida no se calcula ni se registra nada aunque se teclee`() {
        val viewModel = crearViewModel()

        viewModel.onCantidadCambiada("10")

        assertNull(viewModel.uiState.value.resultado)
        verify(exactly = 0) { analytics.logEvent("soldaduras_calculado", any()) }
    }

    @Test
    fun `elegir familia deja su formulario limpio con los valores por defecto`() {
        val viewModel = crearViewModel()

        viewModel.onFamiliaSeleccionada(FamiliaSoldadura.ORO_LEY)

        val estado = viewModel.uiState.value
        assertEquals(FamiliaSoldadura.ORO_LEY, estado.familia)
        assertEquals("", estado.cantidadTexto)
        assertEquals(ColorOroSoldadura.AMARILLO, estado.colorOro)
        assertEquals(DurezaSoldaduraLey.MUY_FLOJA, estado.dureza)
        assertNull(estado.resultado)
    }

    // --- ORO LEY en modo directo, el flujo del mockup (SC-003) ---

    @Test
    fun `el caso del mockup - 2 gramos de oro muy floja piden 6,667 de base y 8,667 en total`() {
        val viewModel = crearViewModelEnOroLey()

        viewModel.onCantidadCambiada("2")

        val resultado = viewModel.uiState.value.resultado
        assertEquals(
            listOf(FilaSoldadura(IngredienteSoldadura.BASE, "6,667")),
            resultado?.filas,
        )
        assertEquals("8,667", resultado?.totalFormateado)
    }

    @Test
    fun `con dureza media 5 gramos de oro piden 5,000 de base y 10,000 en total`() {
        val viewModel = crearViewModelEnOroLey()

        viewModel.onCantidadCambiada("5")
        viewModel.onDurezaSeleccionada(DurezaSoldaduraLey.MEDIA)

        val resultado = viewModel.uiState.value.resultado
        assertEquals("5,000", resultado?.filas?.single()?.gramosFormateados)
        assertEquals("10,000", resultado?.totalFormateado)
    }

    @Test
    fun `cambiar el color no cambia ninguna cifra`() {
        val viewModel = crearViewModelEnOroLey()
        viewModel.onCantidadCambiada("2")
        val antes = viewModel.uiState.value.resultado

        viewModel.onColorSeleccionado(ColorOroSoldadura.BLANCO)

        assertEquals(antes, viewModel.uiState.value.resultado)
    }

    @Test
    fun `coma y punto producen el mismo resultado`() {
        val conComa = crearViewModelEnOroLey().apply { onCantidadCambiada("2,5") }
        val conPunto = crearViewModelEnOroLey().apply { onCantidadCambiada("2.5") }

        assertEquals(
            conComa.uiState.value.resultado,
            conPunto.uiState.value.resultado,
        )
    }

    @Test
    fun `las entradas invalidas no producen resultado`() {
        listOf("", "0", "-1", "abc", "1.2,3", "  ").forEach { texto ->
            val viewModel = crearViewModelEnOroLey()

            viewModel.onCantidadCambiada(texto)

            assertNull("con «$texto» no debe haber resultado", viewModel.uiState.value.resultado)
        }
    }

    @Test
    fun `una cantidad muy grande calcula y formatea sin perder la composicion`() {
        val viewModel = crearViewModelEnOroLey()

        viewModel.onCantidadCambiada("100000")
        viewModel.onDurezaSeleccionada(DurezaSoldaduraLey.MEDIA)

        val resultado = viewModel.uiState.value.resultado
        assertEquals("100000,000", resultado?.filas?.single()?.gramosFormateados)
        assertEquals("200000,000", resultado?.totalFormateado)
    }

    // --- Telemetría deduplicada (FR-027) ---

    @Test
    fun `teclear no duplica el evento de calculo`() {
        val viewModel = crearViewModelEnOroLey()

        viewModel.onCantidadCambiada("2")
        viewModel.onCantidadCambiada("25")
        viewModel.onCantidadCambiada("250")

        verify(exactly = 1) {
            analytics.logEvent(
                "soldaduras_calculado",
                mapOf(
                    "familia" to "oro_ley",
                    "modo" to "desde_metal",
                    "tipo" to "muy_floja",
                    "color" to "amarillo",
                ),
            )
        }
    }

    @Test
    fun `cambiar la dureza o el color estrena evento con sus parametros`() {
        val viewModel = crearViewModelEnOroLey()
        viewModel.onCantidadCambiada("2")

        viewModel.onDurezaSeleccionada(DurezaSoldaduraLey.MUY_FUERTE)
        viewModel.onColorSeleccionado(ColorOroSoldadura.ROSA)

        verify(exactly = 1) {
            analytics.logEvent(
                "soldaduras_calculado",
                mapOf(
                    "familia" to "oro_ley",
                    "modo" to "desde_metal",
                    "tipo" to "muy_fuerte",
                    "color" to "amarillo",
                ),
            )
        }
        verify(exactly = 1) {
            analytics.logEvent(
                "soldaduras_calculado",
                mapOf(
                    "familia" to "oro_ley",
                    "modo" to "desde_metal",
                    "tipo" to "muy_fuerte",
                    "color" to "rosa",
                ),
            )
        }
    }

    @Test
    fun `al volver la entrada a ser valida el calculo se registra de nuevo`() {
        val viewModel = crearViewModelEnOroLey()

        viewModel.onCantidadCambiada("2")
        viewModel.onCantidadCambiada("")
        viewModel.onCantidadCambiada("2")

        verify(exactly = 2) {
            analytics.logEvent(
                "soldaduras_calculado",
                mapOf(
                    "familia" to "oro_ley",
                    "modo" to "desde_metal",
                    "tipo" to "muy_floja",
                    "color" to "amarillo",
                ),
            )
        }
    }
}
