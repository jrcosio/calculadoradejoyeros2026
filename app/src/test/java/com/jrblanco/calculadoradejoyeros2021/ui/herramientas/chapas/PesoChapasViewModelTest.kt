package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas

import app.cash.turbine.test
import com.jrblanco.calculadoradejoyeros2021.core.util.TestDispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.data.repository.FakeFavoritosRepository
import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.FamiliaChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.FavoritosDePrueba
import com.jrblanco.calculadoradejoyeros2021.domain.model.MaterialChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularPesoChapaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.GuardarFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ObtenerFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.ui.favoritos.AvisoFavorito
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El caso de uso entra real, sin mock: el motor es puro y determinista. Solo se mockea la
 * telemetría. Los valores esperados son los de §7 del documento técnico.
 */
class PesoChapasViewModelTest {

    private val analytics = mockk<AnalyticsRepository>(relaxed = true)
    private val favoritos = FakeFavoritosRepository()

    private fun crearViewModel() = PesoChapasViewModel(
        calcularPeso = CalcularPesoChapaUseCase(),
        guardarFavorito = GuardarFavoritoUseCase(favoritos),
        obtenerFavorito = ObtenerFavoritoUseCase(favoritos),
        analytics = analytics,
        dispatchers = TestDispatcherProvider(),
    )

    private fun PesoChapasViewModel.teclearReferencia() {
        onMedidaCambiada(MedidaChapa.ANCHO, "10")
        onMedidaCambiada(MedidaChapa.ESPESOR, "0,5")
        onMedidaCambiada(MedidaChapa.LARGO, "20")
    }

    @Test
    fun `el estado inicial es oro 18K con los campos vacios y la chapa de referencia`() = runTest {
        crearViewModel().uiState.test {
            val estado = awaitItem()
            assertEquals(MaterialChapa.ORO_18K, estado.material)
            assertEquals(MedidaChapa.entries.associateWith { "" }, estado.medidas)
            assertNull(estado.resultado)
            assertEquals(DibujoChapaUiState(), estado.dibujo)
            assertTrue(estado.fueraDeRango.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `registra la vista de pantalla al construirse`() {
        crearViewModel()
        verify(exactly = 1) { analytics.logScreenView("herramientas_chapas") }
    }

    @Test
    fun `no hay resultado hasta que las tres medidas son validas`() {
        val viewModel = crearViewModel()

        viewModel.onMedidaCambiada(MedidaChapa.ANCHO, "10")
        viewModel.onMedidaCambiada(MedidaChapa.ESPESOR, "0,5")
        assertNull(viewModel.uiState.value.resultado)

        viewModel.onMedidaCambiada(MedidaChapa.LARGO, "20")
        val resultado = viewModel.uiState.value.resultado!!
        assertEquals("1,56", resultado.pesoFormateado)
        assertEquals("0,100", resultado.volumenFormateado)
        assertEquals("15,58", resultado.densidadFormateada)
        assertEquals("75,0", resultado.purezaFormateada)
        assertEquals("1,169", resultado.metalFinoFormateado)
    }

    @Test
    fun `los ocho materiales muestran la columna Mostrar de §7`() {
        val esperados = mapOf(
            MaterialChapa.ORO_18K to "1,56", MaterialChapa.ORO_14K to "1,31",
            MaterialChapa.ORO_12K to "1,28", MaterialChapa.ORO_9K to "1,12",
            MaterialChapa.PLATA_950 to "1,04", MaterialChapa.PLATA_925 to "1,04",
            MaterialChapa.PLATA_900 to "1,03", MaterialChapa.PLATA_800 to "1,01",
        )
        val viewModel = crearViewModel().apply { teclearReferencia() }
        esperados.forEach { (material, peso) ->
            viewModel.onMaterialSeleccionado(material)
            assertEquals(material.name, peso, viewModel.uiState.value.resultado?.pesoFormateado)
        }
        assertEquals("0,958", viewModel.uiState.value.let { viewModel.onMaterialSeleccionado(MaterialChapa.PLATA_925); viewModel.uiState.value.resultado?.metalFinoFormateado })
        assertEquals("92,5", viewModel.uiState.value.resultado?.purezaFormateada)
        assertEquals("10,36", viewModel.uiState.value.resultado?.densidadFormateada)
    }

    @Test
    fun `la coma y el punto decimal producen el mismo resultado`() {
        val conComa = crearViewModel().apply { teclearReferencia() }
        val conPunto = crearViewModel().apply {
            onMedidaCambiada(MedidaChapa.ANCHO, "10")
            onMedidaCambiada(MedidaChapa.ESPESOR, "0.5")
            onMedidaCambiada(MedidaChapa.LARGO, "20")
        }
        assertEquals(conComa.uiState.value.resultado, conPunto.uiState.value.resultado)
    }

    @Test
    fun `una medida invalida en cualquier campo quita el resultado sin marcar rango`() {
        MedidaChapa.entries.forEach { medida ->
            listOf("", "0", "-1", "abc", "1,2,3", "1.2.3", "  ").forEach { texto ->
                val viewModel = crearViewModel().apply { teclearReferencia() }
                viewModel.onMedidaCambiada(medida, texto)
                assertNull("$medida «$texto»", viewModel.uiState.value.resultado)
                assertTrue(viewModel.uiState.value.fueraDeRango.isEmpty())
            }
        }
    }

    @Test
    fun `los limites operativos se respetan y se marcan`() {
        val viewModel = crearViewModel().apply { teclearReferencia() }

        viewModel.onMedidaCambiada(MedidaChapa.ANCHO, "10000")
        assertTrue(viewModel.uiState.value.fueraDeRango.isEmpty())
        assertEquals("1558,00", viewModel.uiState.value.resultado?.pesoFormateado)

        viewModel.onMedidaCambiada(MedidaChapa.ANCHO, "10000,01")
        assertEquals(setOf(MedidaChapa.ANCHO), viewModel.uiState.value.fueraDeRango)
        assertNull(viewModel.uiState.value.resultado)

        viewModel.onMedidaCambiada(MedidaChapa.ANCHO, "10")
        viewModel.onMedidaCambiada(MedidaChapa.ESPESOR, "1000,5")
        assertEquals(setOf(MedidaChapa.ESPESOR), viewModel.uiState.value.fueraDeRango)

        viewModel.onMedidaCambiada(MedidaChapa.ESPESOR, "0,5")
        assertTrue(viewModel.uiState.value.fueraDeRango.isEmpty())
        assertEquals("1,56", viewModel.uiState.value.resultado?.pesoFormateado)
    }

    @Test
    fun `cambiar de familia conserva las medidas y fija la ley por defecto`() {
        val viewModel = crearViewModel().apply { teclearReferencia() }

        viewModel.onFamiliaSeleccionada(FamiliaChapa.PLATA)
        assertEquals(MaterialChapa.PLATA_925, viewModel.uiState.value.material)
        assertEquals("10", viewModel.uiState.value.medidas[MedidaChapa.ANCHO])
        assertEquals("1,04", viewModel.uiState.value.resultado?.pesoFormateado)

        viewModel.onMaterialSeleccionado(MaterialChapa.PLATA_800)
        viewModel.onFamiliaSeleccionada(FamiliaChapa.ORO)
        assertEquals(MaterialChapa.ORO_18K, viewModel.uiState.value.material)

        viewModel.onMaterialSeleccionado(MaterialChapa.ORO_9K)
        viewModel.onFamiliaSeleccionada(FamiliaChapa.ORO)
        assertEquals(MaterialChapa.ORO_9K, viewModel.uiState.value.material)
    }

    @Test
    fun `el dibujo lleva las cotas de las medidas validas y sabe si esta completo`() {
        val viewModel = crearViewModel().apply { teclearReferencia() }
        val dibujo = viewModel.uiState.value.dibujo
        assertEquals("10,00", dibujo.etiquetaAncho)
        assertEquals("0,50", dibujo.etiquetaEspesor)
        assertEquals("20,00", dibujo.etiquetaLargo)
        assertTrue(dibujo.completa)
        assertEquals(ProporcionesChapa.desde(BigDecimal("10"), BigDecimal("20"), BigDecimal("0.5")), dibujo.proporciones)

        viewModel.onMedidaCambiada(MedidaChapa.ANCHO, "")
        val incompleto = viewModel.uiState.value.dibujo
        assertNull(incompleto.etiquetaAncho)
        assertEquals("20,00", incompleto.etiquetaLargo)
        assertFalse(incompleto.completa)
        assertEquals(ProporcionesChapa.desde(null, BigDecimal("20"), BigDecimal("0.5")), incompleto.proporciones)
    }

    @Test
    fun `una medida fuera de rango no entra en el dibujo`() {
        val viewModel = crearViewModel().apply { teclearReferencia() }
        viewModel.onMedidaCambiada(MedidaChapa.LARGO, "20000")
        assertNull(viewModel.uiState.value.dibujo.etiquetaLargo)
        assertFalse(viewModel.uiState.value.dibujo.completa)
    }

    @Test
    fun `teclear no duplica la telemetria de calculo`() {
        val viewModel = crearViewModel().apply { teclearReferencia() }
        viewModel.onMedidaCambiada(MedidaChapa.LARGO, "25")
        viewModel.onMedidaCambiada(MedidaChapa.LARGO, "30")

        verify(exactly = 1) {
            analytics.logEvent("herramientas_chapa_calculada", mapOf("material" to "oro", "ley" to "18k"))
        }
    }

    @Test
    fun `cada material estrena su evento y volver a valido lo rearma`() {
        val viewModel = crearViewModel().apply { teclearReferencia() }
        viewModel.onMaterialSeleccionado(MaterialChapa.ORO_14K)
        viewModel.onFamiliaSeleccionada(FamiliaChapa.PLATA)
        viewModel.onMedidaCambiada(MedidaChapa.ANCHO, "")
        viewModel.onMedidaCambiada(MedidaChapa.ANCHO, "10")

        verify(exactly = 1) { analytics.logEvent("herramientas_chapa_calculada", mapOf("material" to "oro", "ley" to "18k")) }
        verify(exactly = 1) { analytics.logEvent("herramientas_chapa_calculada", mapOf("material" to "oro", "ley" to "14k")) }
        verify(exactly = 2) { analytics.logEvent("herramientas_chapa_calculada", mapOf("material" to "plata", "ley" to "925")) }
    }

    @Test
    fun `limpiar devuelve el estado inicial y rearma la telemetria`() {
        val viewModel = crearViewModel().apply { teclearReferencia() }

        viewModel.onLimpiar()
        assertEquals(PesoChapasUiState(), viewModel.uiState.value)

        viewModel.teclearReferencia()
        verify(exactly = 2) { analytics.logEvent("herramientas_chapa_calculada", mapOf("material" to "oro", "ley" to "18k")) }
    }

    // --- Favoritos (009) ---

    @Test
    fun `guardar manda las tres medidas y el material`() = runTest {
        val viewModel = crearViewModel()
        viewModel.teclearReferencia()

        viewModel.onGuardarFavoritos()

        assertEquals(
            EntradasFavorito.Chapa(
                ancho = BigDecimal("10"),
                largo = BigDecimal("20"),
                espesor = BigDecimal("0.5"),
                material = MaterialChapa.ORO_18K,
            ),
            favoritos.guardados.single(),
        )
        assertEquals(AvisoFavorito.GUARDADO, viewModel.uiState.value.avisoFavorito)
        verify(exactly = 1) {
            analytics.logEvent("herramientas_chapa_favorito_guardado", mapOf("resultado" to "nuevo"))
        }
    }

    @Test
    fun `guardar sin las tres medidas pide completar el calculo`() = runTest {
        val viewModel = crearViewModel()
        viewModel.onMedidaCambiada(MedidaChapa.ANCHO, "10")

        viewModel.onGuardarFavoritos()

        assertTrue(favoritos.guardados.isEmpty())
        assertEquals(AvisoFavorito.SIN_DATOS, viewModel.uiState.value.avisoFavorito)
    }

    @Test
    fun `guardar una medida fuera de rango pide completar el calculo`() = runTest {
        val viewModel = crearViewModel()
        viewModel.onMedidaCambiada(MedidaChapa.ANCHO, "20000")
        viewModel.onMedidaCambiada(MedidaChapa.ESPESOR, "0,5")
        viewModel.onMedidaCambiada(MedidaChapa.LARGO, "20")

        viewModel.onGuardarFavoritos()

        assertTrue(favoritos.guardados.isEmpty())
        assertEquals(AvisoFavorito.SIN_DATOS, viewModel.uiState.value.avisoFavorito)
    }

    @Test
    fun `cargar un favorito rellena material y medidas y es idempotente`() = runTest {
        favoritos.flujo.value = listOf(
            FavoritosDePrueba.favorito(
                id = 6L,
                entradas = EntradasFavorito.Chapa(
                    ancho = BigDecimal("12"),
                    largo = BigDecimal("40"),
                    espesor = BigDecimal("1.5"),
                    material = MaterialChapa.PLATA_925,
                ),
            ),
        )
        val viewModel = crearViewModel()

        viewModel.cargarFavorito(6L)

        val estado = viewModel.uiState.value
        assertEquals(MaterialChapa.PLATA_925, estado.material)
        assertEquals("12", estado.medidas[MedidaChapa.ANCHO])
        assertEquals("40", estado.medidas[MedidaChapa.LARGO])
        assertEquals("1,5", estado.medidas[MedidaChapa.ESPESOR])
        assertNotNull(estado.resultado)

        viewModel.onMedidaCambiada(MedidaChapa.ANCHO, "99")
        viewModel.cargarFavorito(6L)

        assertEquals("99", viewModel.uiState.value.medidas[MedidaChapa.ANCHO])
    }
}
