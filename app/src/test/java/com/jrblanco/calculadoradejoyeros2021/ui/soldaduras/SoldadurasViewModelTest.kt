package com.jrblanco.calculadoradejoyeros2021.ui.soldaduras

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraClasica
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraPlata
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

    // --- CLÁSICA en modo directo, los casos de los mockups (SC-003) ---

    @Test
    fun `clasica floja con 10 gramos de oro muestra 4,000 de plata y 2,000 de laton`() {
        val viewModel = crearViewModel()
        viewModel.onFamiliaSeleccionada(FamiliaSoldadura.CLASICA)

        viewModel.onCantidadCambiada("10")

        val resultado = viewModel.uiState.value.resultado
        // Sin fila del oro introducido (FR-022).
        assertEquals(
            listOf(
                FilaSoldadura(IngredienteSoldadura.PLATA_FINA, "4,000"),
                FilaSoldadura(IngredienteSoldadura.LATON, "2,000"),
            ),
            resultado?.filas,
        )
        assertEquals("16,000", resultado?.totalFormateado)
    }

    @Test
    fun `clasica fuerte con 10 gramos de oro muestra tres filas de 1,000`() {
        val viewModel = crearViewModel()
        viewModel.onFamiliaSeleccionada(FamiliaSoldadura.CLASICA)
        viewModel.onTipoClasicaSeleccionado(TipoSoldaduraClasica.FUERTE)

        viewModel.onCantidadCambiada("10")

        val resultado = viewModel.uiState.value.resultado
        assertEquals(
            listOf("1,000", "1,000", "1,000"),
            resultado?.filas?.map { it.gramosFormateados },
        )
        assertEquals("13,000", resultado?.totalFormateado)
    }

    @Test
    fun `clasica muy floja de ley con 10 gramos de oro fino muestra 1,000 1,600 y 1,800`() {
        val viewModel = crearViewModel()
        viewModel.onFamiliaSeleccionada(FamiliaSoldadura.CLASICA)
        viewModel.onTipoClasicaSeleccionado(TipoSoldaduraClasica.MUY_FLOJA_LEY)

        viewModel.onCantidadCambiada("10")

        val resultado = viewModel.uiState.value.resultado
        assertEquals(
            listOf(
                FilaSoldadura(IngredienteSoldadura.PLATA_FINA, "1,000"),
                FilaSoldadura(IngredienteSoldadura.LATON, "1,600"),
                FilaSoldadura(IngredienteSoldadura.CADMIO, "1,800"),
            ),
            resultado?.filas,
        )
        assertEquals("14,400", resultado?.totalFormateado)
    }

    @Test
    fun `cambiar el tipo de clasica recalcula y estrena evento sin color`() {
        val viewModel = crearViewModel()
        viewModel.onFamiliaSeleccionada(FamiliaSoldadura.CLASICA)
        viewModel.onCantidadCambiada("10")

        viewModel.onTipoClasicaSeleccionado(TipoSoldaduraClasica.FUERTE)

        assertEquals("13,000", viewModel.uiState.value.resultado?.totalFormateado)
        // Las clásicas no llevan color (§8.1): el evento tampoco.
        verify(exactly = 1) {
            analytics.logEvent(
                "soldaduras_calculado",
                mapOf("familia" to "clasica", "modo" to "desde_metal", "tipo" to "fuerte"),
            )
        }
    }

    @Test
    fun `cambiar de familia arranca el formulario limpio`() {
        val viewModel = crearViewModelEnOroLey()
        viewModel.onCantidadCambiada("10")

        viewModel.onFamiliaSeleccionada(FamiliaSoldadura.CLASICA)

        val estado = viewModel.uiState.value
        assertEquals(FamiliaSoldadura.CLASICA, estado.familia)
        assertEquals("", estado.cantidadTexto)
        assertEquals(ModoEntradaSoldadura.DESDE_METAL, estado.modo)
        assertNull(estado.resultado)
    }

    // --- PLATA en modo directo (SC-001, SC-003) ---

    @Test
    fun `test 4 formateado - 25 gramos de plata muy floja muestran 18,750 de laton y 43,750`() {
        val viewModel = crearViewModel()
        viewModel.onFamiliaSeleccionada(FamiliaSoldadura.PLATA)

        viewModel.onCantidadCambiada("25")

        val resultado = viewModel.uiState.value.resultado
        // Sin fila de la plata introducida (FR-022): solo el latón.
        assertEquals(
            listOf(FilaSoldadura(IngredienteSoldadura.LATON, "18,750")),
            resultado?.filas,
        )
        assertEquals("43,750", resultado?.totalFormateado)
    }

    @Test
    fun `test 5 formateado - 25 gramos de plata fuerte muestran 7,500 de laton y 32,500`() {
        val viewModel = crearViewModel()
        viewModel.onFamiliaSeleccionada(FamiliaSoldadura.PLATA)
        viewModel.onTipoPlataSeleccionado(TipoSoldaduraPlata.FUERTE)

        viewModel.onCantidadCambiada("25")

        val resultado = viewModel.uiState.value.resultado
        assertEquals("7,500", resultado?.filas?.single()?.gramosFormateados)
        assertEquals("32,500", resultado?.totalFormateado)
    }

    @Test
    fun `la tabla de plata del documento con 25 gramos en los cuatro tipos`() {
        val esperados = mapOf(
            TipoSoldaduraPlata.MUY_FLOJA to ("18,750" to "43,750"),
            TipoSoldaduraPlata.FLOJA to ("12,500" to "37,500"),
            TipoSoldaduraPlata.NORMAL to ("10,000" to "35,000"),
            TipoSoldaduraPlata.FUERTE to ("7,500" to "32,500"),
        )

        esperados.forEach { (tipo, valores) ->
            val (laton, total) = valores
            val viewModel = crearViewModel()
            viewModel.onFamiliaSeleccionada(FamiliaSoldadura.PLATA)
            viewModel.onTipoPlataSeleccionado(tipo)
            viewModel.onCantidadCambiada("25")

            val resultado = viewModel.uiState.value.resultado
            assertEquals("latón de $tipo", laton, resultado?.filas?.single()?.gramosFormateados)
            assertEquals("total de $tipo", total, resultado?.totalFormateado)
        }
    }

    // --- Modo peso final deseado (US5, §2.3, FR-003, FR-023) ---

    @Test
    fun `cambiar de modo vacia cantidad y resultado y conserva las selecciones`() {
        val viewModel = crearViewModelEnOroLey()
        viewModel.onDurezaSeleccionada(DurezaSoldaduraLey.MEDIA)
        viewModel.onColorSeleccionado(ColorOroSoldadura.ROSA)
        viewModel.onCantidadCambiada("10")

        viewModel.onModoCambiado(ModoEntradaSoldadura.PESO_FINAL)

        val estado = viewModel.uiState.value
        assertEquals(ModoEntradaSoldadura.PESO_FINAL, estado.modo)
        assertEquals("", estado.cantidadTexto)
        assertNull(estado.resultado)
        assertEquals(FamiliaSoldadura.ORO_LEY, estado.familia)
        assertEquals(DurezaSoldaduraLey.MEDIA, estado.dureza)
        assertEquals(ColorOroSoldadura.ROSA, estado.colorOro)
    }

    @Test
    fun `oro ley inverso - 10 gramos media reparten 5,000 de base y 5,000 de oro`() {
        val viewModel = crearViewModelEnOroLey()
        viewModel.onModoCambiado(ModoEntradaSoldadura.PESO_FINAL)
        viewModel.onDurezaSeleccionada(DurezaSoldaduraLey.MEDIA)

        viewModel.onCantidadCambiada("10")

        val resultado = viewModel.uiState.value.resultado
        // En inverso se pintan todas las filas, incluido el oro del color (FR-022).
        assertEquals(
            listOf(
                FilaSoldadura(IngredienteSoldadura.BASE, "5,000"),
                FilaSoldadura(IngredienteSoldadura.ORO_18K, "5,000"),
            ),
            resultado?.filas,
        )
        assertEquals("10,000", resultado?.totalFormateado)
    }

    @Test
    fun `test 1 formateado - clasica floja inversa con 8 gramos recupera la receta patron`() {
        val viewModel = crearViewModel()
        viewModel.onFamiliaSeleccionada(FamiliaSoldadura.CLASICA)
        viewModel.onModoCambiado(ModoEntradaSoldadura.PESO_FINAL)

        viewModel.onCantidadCambiada("8")

        val resultado = viewModel.uiState.value.resultado
        assertEquals(
            listOf(
                FilaSoldadura(IngredienteSoldadura.ORO_18K, "5,000"),
                FilaSoldadura(IngredienteSoldadura.PLATA_FINA, "2,000"),
                FilaSoldadura(IngredienteSoldadura.LATON, "1,000"),
            ),
            resultado?.filas,
        )
        assertEquals("8,000", resultado?.totalFormateado)
    }

    @Test
    fun `clasica muy floja de ley inversa con 10 gramos documenta la nota de redondeo`() {
        val viewModel = crearViewModel()
        viewModel.onFamiliaSeleccionada(FamiliaSoldadura.CLASICA)
        viewModel.onTipoClasicaSeleccionado(TipoSoldaduraClasica.MUY_FLOJA_LEY)
        viewModel.onModoCambiado(ModoEntradaSoldadura.PESO_FINAL)

        viewModel.onCantidadCambiada("10")

        val resultado = viewModel.uiState.value.resultado
        // La suma visible es 9,999: una milésima menos que el total pedido, por el
        // redondeo de vista sobre la división infinita 10÷1,44. Es exactamente el caso
        // de la nota de §8.3 (FR-021): ningún ingrediente se ajusta para cuadrarla.
        assertEquals(
            listOf("6,944", "0,694", "1,111", "1,250"),
            resultado?.filas?.map { it.gramosFormateados },
        )
        assertEquals("10,000", resultado?.totalFormateado)
    }

    @Test
    fun `plata inversa - 10 gramos muy floja reparten 5,714 de plata y 4,286 de laton`() {
        val viewModel = crearViewModel()
        viewModel.onFamiliaSeleccionada(FamiliaSoldadura.PLATA)
        viewModel.onModoCambiado(ModoEntradaSoldadura.PESO_FINAL)

        viewModel.onCantidadCambiada("10")

        val resultado = viewModel.uiState.value.resultado
        assertEquals(
            listOf(
                FilaSoldadura(IngredienteSoldadura.PLATA_FINA, "5,714"),
                FilaSoldadura(IngredienteSoldadura.LATON, "4,286"),
            ),
            resultado?.filas,
        )
        assertEquals("10,000", resultado?.totalFormateado)
    }

    @Test
    fun `el modo viaja en el evento de calculo`() {
        val viewModel = crearViewModelEnOroLey()
        viewModel.onModoCambiado(ModoEntradaSoldadura.PESO_FINAL)

        viewModel.onCantidadCambiada("10")

        verify(exactly = 1) {
            analytics.logEvent(
                "soldaduras_calculado",
                mapOf(
                    "familia" to "oro_ley",
                    "modo" to "peso_final",
                    "tipo" to "muy_floja",
                    "color" to "amarillo",
                ),
            )
        }
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

    // --- Limpiar y favoritos (US6, FR-024) ---

    @Test
    fun `limpiar vuelve al formulario inicial conservando la familia`() {
        val viewModel = crearViewModelEnOroLey()
        viewModel.onDurezaSeleccionada(DurezaSoldaduraLey.FUERTE)
        viewModel.onColorSeleccionado(ColorOroSoldadura.BLANCO)
        viewModel.onCantidadCambiada("10")

        viewModel.onLimpiar()

        assertEquals(
            SoldadurasUiState(familia = FamiliaSoldadura.ORO_LEY),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `tras limpiar el mismo calculo vuelve a registrarse`() {
        val viewModel = crearViewModelEnOroLey()
        viewModel.onCantidadCambiada("2")

        viewModel.onLimpiar()
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

    @Test
    fun `guardar favoritos solo emite su evento y no altera el estado`() {
        val viewModel = crearViewModelEnOroLey()
        viewModel.onCantidadCambiada("2")
        val antes = viewModel.uiState.value

        viewModel.onGuardarFavoritos()

        assertEquals(antes, viewModel.uiState.value)
        verify(exactly = 1) { analytics.logEvent("soldaduras_favoritos_proximamente") }
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
