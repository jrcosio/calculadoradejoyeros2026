package com.jrblanco.calculadoradejoyeros2021.ui.favoritos

import com.jrblanco.calculadoradejoyeros2021.core.util.TestDispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.data.repository.FakeFavoritosRepository
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.Favorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata
import com.jrblanco.calculadoradejoyeros2021.domain.model.MaterialChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.ModoEntradaSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraClasica
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
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.GuardarFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ObservarFavoritosUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ObtenerFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ResumirFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas.MedidaChapa
import com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas.PesoChapasViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.oro.OroViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.plata.PlataViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.soldaduras.FamiliaSoldadura
import com.jrblanco.calculadoradejoyeros2021.ui.soldaduras.SoldaduraBaseViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.soldaduras.SoldadurasViewModel
import io.mockk.mockk
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * **El test que legitima la duplicación de [FormatoFavoritos].**
 *
 * `CLAUDE.md` prohíbe unificar las políticas de redondeo de las calculadoras: son documentos
 * técnicos distintos y en plata el truncado lo exige la Ley 17/1985. La consecuencia es que la
 * pantalla de Favoritos las repite, y una copia mal hecha sería un fallo silencioso — la tarjeta
 * mostraría una cifra y la calculadora otra.
 *
 * Aquí se ejecutan **los ViewModels reales de las cinco calculadoras** y el de Favoritos con las
 * mismas entradas, y se comparan las cadenas dígito a dígito. Si alguien copia `HALF_UP` en la rama
 * de plata, este test lo grita.
 */
class FavoritosParidadFormatoTest {

    private val analytics = mockk<AnalyticsRepository>(relaxed = true)
    private val favoritos = FakeFavoritosRepository()
    private val dispatchers = TestDispatcherProvider()

    private fun favoritosViewModel(entradas: EntradasFavorito): FavoritosViewModel {
        favoritos.flujo.value = listOf(Favorito(1L, 0L, entradas))
        return FavoritosViewModel(
            observarFavoritos = ObservarFavoritosUseCase(favoritos),
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
            borrarFavorito = BorrarFavoritoUseCase(favoritos),
            analytics = analytics,
            dispatchers = dispatchers,
        )
    }

    private fun tarjeta(entradas: EntradasFavorito): FavoritoUiModel =
        favoritosViewModel(entradas).uiState.value.favoritos.single()

    @Test
    fun `oro 18K blanco con 30 gramos - las tres cifras y el total coinciden`() = runTest {
        val calculadora = OroViewModel(
            calcularAleacion = CalcularAleacionOroUseCase(),
            guardarFavorito = GuardarFavoritoUseCase(favoritos),
            obtenerFavorito = ObtenerFavoritoUseCase(favoritos),
            analytics = analytics,
            dispatchers = dispatchers,
        )
        calculadora.onColorSeleccionado(ColorOro.BLANCO)
        calculadora.onCantidadCambiada("30")
        val esperado = calculadora.uiState.value.resultado!!

        val real = tarjeta(EntradasFavorito.Oro(BigDecimal("30"), ColorOro.BLANCO, LeyOro.LEY_18K))

        assertEquals(
            esperado.metales.map { it.gramosFormateados },
            real.lineas.map { it.valorFormateado },
        )
        assertEquals(esperado.totalFormateado, real.totalFormateado)
    }

    @Test
    fun `plata 950 con 100 gramos - el cobre es 5,157 y no 5,158`() = runTest {
        val calculadora = PlataViewModel(
            calcularAleacion = CalcularAleacionPlataUseCase(),
            guardarFavorito = GuardarFavoritoUseCase(favoritos),
            obtenerFavorito = ObtenerFavoritoUseCase(favoritos),
            analytics = analytics,
            dispatchers = dispatchers,
        )
        calculadora.onLeySeleccionada(LeyPlata.LEY_950)
        calculadora.onCantidadCambiada("100")
        val esperado = calculadora.uiState.value.resultado!!

        val real = tarjeta(EntradasFavorito.Plata(BigDecimal("100"), LeyPlata.LEY_950))

        assertEquals(esperado.cobreFormateado, real.lineas.single().valorFormateado)
        assertEquals(esperado.totalFormateado, real.totalFormateado)
        // Y el valor exacto, para que el test hable por sí solo si el de arriba se rompe.
        assertEquals("5,157", real.lineas.single().valorFormateado)
    }

    @Test
    fun `clasica floja en modo inverso - todas las cifras coinciden`() = runTest {
        val calculadora = SoldadurasViewModel(
            calcularLeyDesdeOro = CalcularSoldaduraLeyDesdeOroUseCase(),
            calcularLeyInversa = CalcularSoldaduraLeyInversaUseCase(),
            calcularClasica = CalcularSoldaduraClasicaUseCase(),
            calcularClasicaInversa = CalcularSoldaduraClasicaInversaUseCase(),
            calcularPlata = CalcularSoldaduraPlataUseCase(),
            calcularPlataInversa = CalcularSoldaduraPlataInversaUseCase(),
            guardarFavorito = GuardarFavoritoUseCase(favoritos),
            obtenerFavorito = ObtenerFavoritoUseCase(favoritos),
            analytics = analytics,
            dispatchers = dispatchers,
        )
        calculadora.onFamiliaSeleccionada(FamiliaSoldadura.CLASICA)
        calculadora.onModoCambiado(ModoEntradaSoldadura.PESO_FINAL)
        calculadora.onCantidadCambiada("8")
        val esperado = calculadora.uiState.value.resultado!!

        val real = tarjeta(
            EntradasFavorito.SoldaduraClasica(
                BigDecimal("8"),
                TipoSoldaduraClasica.FLOJA,
                ModoEntradaSoldadura.PESO_FINAL,
            ),
        )

        assertEquals(esperado.filas.map { it.gramosFormateados }, real.lineas.map { it.valorFormateado })
        assertEquals(esperado.totalFormateado, real.totalFormateado)
    }

    @Test
    fun `base en modo inverso con 10 gramos - las cinco cifras y el total coinciden`() = runTest {
        val calculadora = SoldaduraBaseViewModel(
            calcularBase = CalcularSoldaduraBaseUseCase(),
            calcularBaseInversa = CalcularSoldaduraBaseInversaUseCase(),
            guardarFavorito = GuardarFavoritoUseCase(favoritos),
            obtenerFavorito = ObtenerFavoritoUseCase(favoritos),
            analytics = analytics,
            dispatchers = dispatchers,
        )
        calculadora.onModoCambiado(ModoEntradaSoldadura.PESO_FINAL)
        calculadora.onCantidadCambiada("10")
        val esperado = calculadora.uiState.value.resultado!!

        val real = tarjeta(
            EntradasFavorito.SoldaduraBase(BigDecimal("10"), ModoEntradaSoldadura.PESO_FINAL),
        )

        assertEquals(esperado.filas.map { it.gramosFormateados }, real.lineas.map { it.valorFormateado })
        assertEquals(esperado.totalFormateado, real.totalFormateado)
    }

    @Test
    fun `base en modo directo con 10 gramos - la fila del oro no se pinta en ninguno de los dos`() = runTest {
        val calculadora = SoldaduraBaseViewModel(
            calcularBase = CalcularSoldaduraBaseUseCase(),
            calcularBaseInversa = CalcularSoldaduraBaseInversaUseCase(),
            guardarFavorito = GuardarFavoritoUseCase(favoritos),
            obtenerFavorito = ObtenerFavoritoUseCase(favoritos),
            analytics = analytics,
            dispatchers = dispatchers,
        )
        calculadora.onCantidadCambiada("10")
        val esperado = calculadora.uiState.value.resultado!!

        val real = tarjeta(
            EntradasFavorito.SoldaduraBase(BigDecimal("10"), ModoEntradaSoldadura.DESDE_METAL),
        )

        assertEquals(4, real.lineas.size)
        assertEquals(esperado.filas.map { it.gramosFormateados }, real.lineas.map { it.valorFormateado })
        assertEquals(esperado.totalFormateado, real.totalFormateado)
    }

    @Test
    fun `chapa de oro 18K 10 x 0,5 x 20 - el peso es 1,56 y el volumen coincide`() = runTest {
        val calculadora = PesoChapasViewModel(
            calcularPeso = CalcularPesoChapaUseCase(),
            guardarFavorito = GuardarFavoritoUseCase(favoritos),
            obtenerFavorito = ObtenerFavoritoUseCase(favoritos),
            analytics = analytics,
            dispatchers = dispatchers,
        )
        calculadora.onMedidaCambiada(MedidaChapa.ANCHO, "10")
        calculadora.onMedidaCambiada(MedidaChapa.ESPESOR, "0,5")
        calculadora.onMedidaCambiada(MedidaChapa.LARGO, "20")
        val esperado = calculadora.uiState.value.resultado!!

        val real = tarjeta(
            EntradasFavorito.Chapa(
                ancho = BigDecimal("10"),
                largo = BigDecimal("20"),
                espesor = BigDecimal("0.5"),
                material = MaterialChapa.ORO_18K,
            ),
        )

        assertEquals(esperado.pesoFormateado, real.totalFormateado)
        assertEquals(esperado.volumenFormateado, real.lineas[0].valorFormateado)
        assertEquals(esperado.metalFinoFormateado, real.lineas[1].valorFormateado)
        assertEquals("1,56", real.totalFormateado)
    }

    @Test
    fun `oro ley en modo directo - la base es la misma cifra en las dos pantallas`() = runTest {
        val calculadora = SoldadurasViewModel(
            calcularLeyDesdeOro = CalcularSoldaduraLeyDesdeOroUseCase(),
            calcularLeyInversa = CalcularSoldaduraLeyInversaUseCase(),
            calcularClasica = CalcularSoldaduraClasicaUseCase(),
            calcularClasicaInversa = CalcularSoldaduraClasicaInversaUseCase(),
            calcularPlata = CalcularSoldaduraPlataUseCase(),
            calcularPlataInversa = CalcularSoldaduraPlataInversaUseCase(),
            guardarFavorito = GuardarFavoritoUseCase(favoritos),
            obtenerFavorito = ObtenerFavoritoUseCase(favoritos),
            analytics = analytics,
            dispatchers = dispatchers,
        )
        calculadora.onFamiliaSeleccionada(FamiliaSoldadura.ORO_LEY)
        calculadora.onCantidadCambiada("10")
        val esperado = calculadora.uiState.value.resultado!!

        val real = tarjeta(
            EntradasFavorito.SoldaduraLey(
                cantidad = BigDecimal("10"),
                dureza = com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey.MUY_FLOJA,
                color = com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura.AMARILLO,
                modo = ModoEntradaSoldadura.DESDE_METAL,
            ),
        )

        assertEquals(esperado.filas.map { it.gramosFormateados }, real.lineas.map { it.valorFormateado })
        assertEquals(esperado.totalFormateado, real.totalFormateado)
    }
}
