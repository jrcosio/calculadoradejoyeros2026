package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.FavoritosDePrueba
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalLiga
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.ModoEntradaSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResumenFavorito
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Motores reales, sin mocks: el caso de uso solo despacha. Los valores esperados son los mismos
 * vectores que usan los tests de cada motor, para que si un motor cambia fallen los dos sitios.
 *
 * Lo que este test protege de verdad es la **regla de filas** de FR-022 de la 006, que aquí está
 * transcrita por segunda vez: en modo directo el metal de partida no se repite como fila.
 */
class ResumirFavoritoUseCaseTest {

    private val resumir = ResumirFavoritoUseCase(
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
    )

    // --- Oro y plata: sin modo ---

    @Test
    fun `oro amarillo 18K con 10 gramos da los mismos metales que su motor`() {
        val resumen = resumir(FavoritosDePrueba.oro(masaOrigen = "10.000", color = com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro.AMARILLO))
                as ResumenFavorito.Oro

        assertCerca("13.320000", resumen.masaFinal)
        assertCerca("2.191419142", resumen.metales.getValue(MetalLiga.PLATA_FINA))
        assertCerca("1.128580858", resumen.metales.getValue(MetalLiga.COBRE))
    }

    @Test
    fun `plata 950 con 100 gramos da el cobre y el total de su motor`() {
        val resumen = resumir(FavoritosDePrueba.plata(masaOrigen = "100")) as ResumenFavorito.Plata

        // La plata de partida es de 999‰, así que 100 g dan 99,9 de plata pura y el reparto sale
        // de 99.9 / 0.95. Mismos valores que el «caso 5» del test del motor.
        assertCerca("5.157894736842105", resumen.cobre)
        assertCerca("105.157894736842105", resumen.masaFinal)
    }

    // --- Soldadura de ley: la base sola en directo, base + oro en inverso ---

    @Test
    fun `oro ley en modo directo no repite el oro introducido`() {
        val resumen = resumir(FavoritosDePrueba.soldaduraLey(modo = ModoEntradaSoldadura.DESDE_METAL))
                as ResumenFavorito.SoldaduraLey

        assertNull("El oro de partida no se repite como fila (FR-022)", resumen.oro18K)
        assertTrue(resumen.base > BigDecimal.ZERO)
    }

    @Test
    fun `oro ley en modo inverso reparte base y oro de 18K`() {
        val resumen = resumir(FavoritosDePrueba.soldaduraLey(modo = ModoEntradaSoldadura.PESO_FINAL))
                as ResumenFavorito.SoldaduraLey

        assertNotNull("En inverso el oro sí es una fila", resumen.oro18K)
        assertCerca("10", resumen.total)
    }

    // --- Clásica y de plata: drop(1) en directo ---

    @Test
    fun `clasica en modo directo descarta el oro de entrada`() {
        val directo = resumir(FavoritosDePrueba.soldaduraClasica(modo = ModoEntradaSoldadura.DESDE_METAL))
                as ResumenFavorito.Soldadura
        val inverso = resumir(FavoritosDePrueba.soldaduraClasica(modo = ModoEntradaSoldadura.PESO_FINAL))
                as ResumenFavorito.Soldadura

        assertEquals(
            "El directo tiene exactamente una fila menos que el inverso",
            inverso.componentes.size - 1,
            directo.componentes.size,
        )
        assertEquals(inverso.componentes.drop(1).map { it.metal }, directo.componentes.map { it.metal })
    }

    @Test
    fun `plata en modo directo deja solo el laton`() {
        val resumen = resumir(FavoritosDePrueba.soldaduraPlata(modo = ModoEntradaSoldadura.DESDE_METAL))
                as ResumenFavorito.Soldadura

        assertEquals(listOf(MetalSoldadura.LATON), resumen.componentes.map { it.metal })
    }

    @Test
    fun `plata en modo inverso reparte plata fina y laton`() {
        val resumen = resumir(FavoritosDePrueba.soldaduraPlata(modo = ModoEntradaSoldadura.PESO_FINAL))
                as ResumenFavorito.Soldadura

        assertEquals(
            listOf(MetalSoldadura.PLATA_FINA, MetalSoldadura.LATON),
            resumen.componentes.map { it.metal },
        )
        assertCerca("10", resumen.total)
    }

    // --- BASE: se filtra por metal, no por posición ---

    @Test
    fun `base en modo directo filtra el oro de 24K y conserva los otros cuatro`() {
        val resumen = resumir(FavoritosDePrueba.soldaduraBase(modo = ModoEntradaSoldadura.DESDE_METAL))
                as ResumenFavorito.Soldadura

        assertEquals(
            listOf(
                MetalSoldadura.COBRE,
                MetalSoldadura.PLATA_FINA,
                MetalSoldadura.ZINC,
                MetalSoldadura.CADMIO,
            ),
            resumen.componentes.map { it.metal },
        )
        assertCerca("0.54", resumen.componentes.first { it.metal == MetalSoldadura.COBRE }.gramos)
        assertCerca("13.26", resumen.total)
    }

    @Test
    fun `base en modo inverso incluye el oro de 24K`() {
        val resumen = resumir(FavoritosDePrueba.soldaduraBase(modo = ModoEntradaSoldadura.PESO_FINAL))
                as ResumenFavorito.Soldadura

        assertTrue(resumen.componentes.any { it.metal == MetalSoldadura.ORO_24K })
        assertEquals(5, resumen.componentes.size)
        assertCerca("10", resumen.total)
    }

    // --- Chapa ---

    @Test
    fun `chapa devuelve peso volumen y metal fino sin redondear`() {
        val resumen = resumir(FavoritosDePrueba.chapa(ancho = "10", largo = "20", espesor = "0.5"))
                as ResumenFavorito.Chapa

        // 10 × 20 × 0.5 = 100 mm³ = 0.1 cm³; con densidad 15.58 del oro 18K → 1.558 g.
        assertCerca("0.1", resumen.volumenCm3)
        assertCerca("1.558", resumen.peso)
        assertTrue(resumen.metalFino < resumen.peso)
    }

    // --- Ninguna variante puede lanzar ---

    @Test
    fun `las siete variantes se resumen sin lanzar`() {
        FavoritosDePrueba.todas().forEach { entradas ->
            assertNotNull("Falló la variante ${entradas.analyticsId}", resumir(entradas))
        }
    }

    @Test
    fun `los dos modos de las cuatro variantes de soldadura se resumen sin lanzar`() {
        ModoEntradaSoldadura.entries.forEach { modo ->
            listOf(
                FavoritosDePrueba.soldaduraLey(modo = modo),
                FavoritosDePrueba.soldaduraClasica(modo = modo),
                FavoritosDePrueba.soldaduraPlata(modo = modo),
                FavoritosDePrueba.soldaduraBase(modo = modo),
            ).forEach { entradas: EntradasFavorito ->
                assertNotNull("Falló ${entradas.analyticsId} en $modo", resumir(entradas))
            }
        }
    }

    private fun assertCerca(esperado: String, real: BigDecimal, tolerancia: String = "0.001") {
        val diferencia = real.subtract(BigDecimal(esperado)).abs()
        assertTrue(
            "Esperado ~$esperado, obtenido $real (diferencia $diferencia)",
            diferencia <= BigDecimal(tolerancia),
        )
    }
}
