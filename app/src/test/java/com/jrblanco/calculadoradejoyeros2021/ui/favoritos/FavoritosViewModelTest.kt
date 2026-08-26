package com.jrblanco.calculadoradejoyeros2021.ui.favoritos

import com.jrblanco.calculadoradejoyeros2021.core.util.TestDispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.data.repository.FakeFavoritosRepository
import com.jrblanco.calculadoradejoyeros2021.domain.model.FavoritosDePrueba
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.BorrarFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularAleacionOroUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularAleacionPlataUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularPesoChapaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraBaseInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraBaseUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraClasicaInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraClasicaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraLeyDesdeOroUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraLeyInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraPlataInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraPlataUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ObservarFavoritosUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ResumirFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.ui.soldaduras.FamiliaSoldadura
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Casos de uso reales sobre un repositorio falso; solo se mockea la telemetría. El
 * `TestDispatcherProvider` hace que el `launch(dispatchers.main)` del ViewModel corra en el test.
 */
class FavoritosViewModelTest {

    private val analytics = mockk<AnalyticsRepository>(relaxed = true)
    private val repositorio = FakeFavoritosRepository()

    private fun crearViewModel() = FavoritosViewModel(
        observarFavoritos = ObservarFavoritosUseCase(repositorio),
        resumirFavorito = ResumirFavoritoUseCase(
            calcularOro = CalcularAleacionOroUseCase(),
            calcularPlata = CalcularAleacionPlataUseCase(),
            calcularLeyDesdeOro = CalcularSoldaduraLeyDesdeOroUseCase(),
            calcularLeyInversa = CalcularSoldaduraLeyInversaUseCase(),
            calcularClasica = CalcularSoldaduraClasicaUseCase(),
            calcularClasicaInversa = CalcularSoldaduraClasicaInversaUseCase(),
            calcularSoldaduraPlata = CalcularSoldaduraPlataUseCase(),
            calcularSoldaduraPlataInversa = CalcularSoldaduraPlataInversaUseCase(),
            calcularBase = CalcularSoldaduraBaseUseCase(),
            calcularBaseInversa = CalcularSoldaduraBaseInversaUseCase(),
            calcularChapa = CalcularPesoChapaUseCase(),
        ),
        borrarFavorito = BorrarFavoritoUseCase(repositorio),
        analytics = analytics,
        dispatchers = TestDispatcherProvider(),
    )

    // --- Telemetría ---

    @Test
    fun `registra la pantalla una sola vez con el nombre del placeholder`() = runTest {
        crearViewModel()

        verify(exactly = 1) { analytics.logScreenView("favoritos") }
    }

    @Test
    fun `abrir un favorito registra su seccion y nada mas`() = runTest {
        repositorio.flujo.value = listOf(FavoritosDePrueba.favorito())
        val viewModel = crearViewModel()

        viewModel.onFavoritoPulsado(viewModel.uiState.value.favoritos.single())

        // FR-036: el evento lleva el tipo y **no** las cantidades ni las medidas.
        verify(exactly = 1) { analytics.logEvent("favoritos_abierto", mapOf("tipo" to "oro")) }
    }

    @Test
    fun `borrar registra la seccion del favorito borrado`() = runTest {
        repositorio.flujo.value = listOf(
            FavoritosDePrueba.favorito(id = 1L, entradas = FavoritosDePrueba.chapa()),
        )
        val viewModel = crearViewModel()

        viewModel.onQuitarPulsado(viewModel.uiState.value.favoritos.single())
        viewModel.onConfirmarBorrado()

        verify(exactly = 1) { analytics.logEvent("favoritos_borrado", mapOf("tipo" to "chapa")) }
    }

    @Test
    fun `cancelar el borrado no emite ningun evento de borrado`() = runTest {
        repositorio.flujo.value = listOf(FavoritosDePrueba.favorito())
        val viewModel = crearViewModel()

        viewModel.onQuitarPulsado(viewModel.uiState.value.favoritos.single())
        viewModel.onCancelarBorrado()

        verify(exactly = 0) { analytics.logEvent("favoritos_borrado", any()) }
    }

    // --- Carga ---

    @Test
    fun `arranca cargando y no pinta nada hasta la primera emision`() = runTest {
        assertTrue(FavoritosUiState().cargando)
    }

    @Test
    fun `tras la primera emision deja de cargar aunque la lista este vacia`() = runTest {
        val viewModel = crearViewModel()

        assertFalse(viewModel.uiState.value.cargando)
        assertTrue(viewModel.uiState.value.favoritos.isEmpty())
    }

    // --- Mapeo ---

    @Test
    fun `mantiene el orden que da el repositorio`() = runTest {
        repositorio.flujo.value = listOf(
            FavoritosDePrueba.favorito(id = 3L),
            FavoritosDePrueba.favorito(id = 2L, entradas = FavoritosDePrueba.plata()),
            FavoritosDePrueba.favorito(id = 1L, entradas = FavoritosDePrueba.chapa()),
        )
        val viewModel = crearViewModel()

        assertEquals(listOf(3L, 2L, 1L), viewModel.uiState.value.favoritos.map { it.id })
    }

    @Test
    fun `un favorito de oro se mapea a su seccion con sus tres metales`() = runTest {
        repositorio.flujo.value = listOf(
            FavoritosDePrueba.favorito(entradas = FavoritosDePrueba.oro(masaOrigen = "30")),
        )
        val viewModel = crearViewModel()
        val tarjeta = viewModel.uiState.value.favoritos.single()

        assertEquals(TipoFavorito.ORO, tarjeta.tipo)
        assertEquals("30", (tarjeta.entradas as EntradasFavoritoUi.Oro).cantidad)
        assertEquals(
            listOf(ConceptoFavorito.PLATA_FINA, ConceptoFavorito.COBRE, ConceptoFavorito.PALADIO),
            tarjeta.lineas.map { it.concepto },
        )
    }

    @Test
    fun `un favorito de plata trunca el cobre - no lo redondea a la media`() = runTest {
        repositorio.flujo.value = listOf(
            FavoritosDePrueba.favorito(entradas = FavoritosDePrueba.plata(masaOrigen = "100")),
        )
        val viewModel = crearViewModel()
        val tarjeta = viewModel.uiState.value.favoritos.single()

        assertEquals("5,157", tarjeta.lineas.single().valorFormateado)
    }

    @Test
    fun `un favorito de chapa lleva volumen y metal fino y el peso a dos decimales`() = runTest {
        repositorio.flujo.value = listOf(
            FavoritosDePrueba.favorito(
                entradas = FavoritosDePrueba.chapa(ancho = "10", largo = "20", espesor = "0.5"),
            ),
        )
        val viewModel = crearViewModel()
        val tarjeta = viewModel.uiState.value.favoritos.single()

        assertEquals(TipoFavorito.CHAPA, tarjeta.tipo)
        assertEquals(
            listOf(ConceptoFavorito.VOLUMEN, ConceptoFavorito.METAL_FINO),
            tarjeta.lineas.map { it.concepto },
        )
        assertEquals("1,56", tarjeta.totalFormateado)
    }

    @Test
    fun `las tres familias de soldadura comparten seccion y la base tiene la suya`() = runTest {
        repositorio.flujo.value = listOf(
            FavoritosDePrueba.favorito(id = 4L, entradas = FavoritosDePrueba.soldaduraLey()),
            FavoritosDePrueba.favorito(id = 3L, entradas = FavoritosDePrueba.soldaduraClasica()),
            FavoritosDePrueba.favorito(id = 2L, entradas = FavoritosDePrueba.soldaduraPlata()),
            FavoritosDePrueba.favorito(id = 1L, entradas = FavoritosDePrueba.soldaduraBase()),
        )
        val viewModel = crearViewModel()

        assertEquals(
            listOf(
                TipoFavorito.SOLDADURA,
                TipoFavorito.SOLDADURA,
                TipoFavorito.SOLDADURA,
                TipoFavorito.SOLDADURA_BASE,
            ),
            viewModel.uiState.value.favoritos.map { it.tipo },
        )
        assertEquals(
            listOf(FamiliaSoldadura.ORO_LEY, FamiliaSoldadura.CLASICA, FamiliaSoldadura.PLATA),
            viewModel.uiState.value.favoritos.take(3)
                .map { (it.entradas as EntradasFavoritoUi.Soldadura).familia },
        )
    }

    @Test
    fun `la base en modo inverso produce las cinco lineas completas`() = runTest {
        repositorio.flujo.value = listOf(
            FavoritosDePrueba.favorito(
                entradas = FavoritosDePrueba.soldaduraBase(
                    modo = com.jrblanco.calculadoradejoyeros2021.domain.model.ModoEntradaSoldadura.PESO_FINAL,
                ),
            ),
        )
        val viewModel = crearViewModel()

        // El recorte a tres es de la tarjeta: el estado emite la lista completa.
        assertEquals(5, viewModel.uiState.value.favoritos.single().lineas.size)
    }

    // --- Diálogo de borrado ---

    @Test
    fun `quitar abre la pregunta y confirmar la cierra y borra`() = runTest {
        repositorio.flujo.value = listOf(FavoritosDePrueba.favorito(id = 8L))
        val viewModel = crearViewModel()
        val tarjeta = viewModel.uiState.value.favoritos.single()

        viewModel.onQuitarPulsado(tarjeta)
        assertEquals(tarjeta.id, viewModel.uiState.value.pendienteDeBorrar?.id)

        viewModel.onConfirmarBorrado()

        assertNull(viewModel.uiState.value.pendienteDeBorrar)
        assertEquals(listOf(8L), repositorio.borrados)
        assertTrue(viewModel.uiState.value.favoritos.isEmpty())
    }

    @Test
    fun `cancelar cierra la pregunta y no borra nada`() = runTest {
        repositorio.flujo.value = listOf(FavoritosDePrueba.favorito(id = 8L))
        val viewModel = crearViewModel()

        viewModel.onQuitarPulsado(viewModel.uiState.value.favoritos.single())
        viewModel.onCancelarBorrado()

        assertNull(viewModel.uiState.value.pendienteDeBorrar)
        assertTrue(repositorio.borrados.isEmpty())
        assertEquals(1, viewModel.uiState.value.favoritos.size)
    }

    @Test
    fun `confirmar sin pregunta abierta no borra nada`() = runTest {
        repositorio.flujo.value = listOf(FavoritosDePrueba.favorito(id = 8L))
        val viewModel = crearViewModel()

        viewModel.onConfirmarBorrado()

        assertTrue(repositorio.borrados.isEmpty())
    }

    @Test
    fun `la pregunta se cierra sola si ese favorito desaparece de la lista`() = runTest {
        repositorio.flujo.value = listOf(
            FavoritosDePrueba.favorito(id = 1L),
            FavoritosDePrueba.favorito(id = 2L, entradas = FavoritosDePrueba.plata()),
        )
        val viewModel = crearViewModel()
        viewModel.onQuitarPulsado(viewModel.uiState.value.favoritos.first { it.id == 1L })

        // El favorito desaparece por otra vía mientras la pregunta está abierta.
        repositorio.flujo.value = listOf(
            FavoritosDePrueba.favorito(id = 2L, entradas = FavoritosDePrueba.plata()),
        )

        assertNull(viewModel.uiState.value.pendienteDeBorrar)
    }

    @Test
    fun `la pregunta sobrevive a una emision que si conserva ese favorito`() = runTest {
        repositorio.flujo.value = listOf(FavoritosDePrueba.favorito(id = 1L))
        val viewModel = crearViewModel()
        viewModel.onQuitarPulsado(viewModel.uiState.value.favoritos.single())

        repositorio.flujo.value = listOf(
            FavoritosDePrueba.favorito(id = 2L, entradas = FavoritosDePrueba.plata()),
            FavoritosDePrueba.favorito(id = 1L),
        )

        assertEquals(1L, viewModel.uiState.value.pendienteDeBorrar?.id)
    }
}
