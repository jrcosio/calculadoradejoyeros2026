package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Motor real, sin mocks. Modo desde la base disponible (§5.4): oro = base × r, exacto.
 * Sin UI en esta versión — existe y se prueba, precedente de la 005.
 */
class CalcularSoldaduraLeyUseCaseTest {

    private val calcular = CalcularSoldaduraLeyUseCase()

    @Test
    fun `test 7 - un gramo de base muy floja pide 0,3 de oro y pesa 1,3`() {
        val calculo = calcular(
            BigDecimal("1"),
            DurezaSoldaduraLey.MUY_FLOJA,
            ColorOroSoldadura.AMARILLO,
        )

        assertExacto("0.3", calculo.oro18K)
        assertExacto("1.3", calculo.total)
    }

    @Test
    fun `la tabla completa del documento con 1 gramo de base`() {
        // §5.4: totales 1,3 / 1,5 / 2 / 3 / 4.
        val esperados = mapOf(
            DurezaSoldaduraLey.MUY_FLOJA to ("0.3" to "1.3"),
            DurezaSoldaduraLey.FLOJA to ("0.5" to "1.5"),
            DurezaSoldaduraLey.MEDIA to ("1" to "2"),
            DurezaSoldaduraLey.FUERTE to ("2" to "3"),
            DurezaSoldaduraLey.MUY_FUERTE to ("3" to "4"),
        )

        esperados.forEach { (dureza, valores) ->
            val (oro, total) = valores
            val calculo = calcular(BigDecimal("1"), dureza, ColorOroSoldadura.AMARILLO)
            assertExacto(oro, calculo.oro18K)
            assertExacto(total, calculo.total)
        }
    }

    @Test
    fun `test 9 - el color blanco viaja al resultado con 0,5 de oro en floja`() {
        val calculo = calcular(BigDecimal("1"), DurezaSoldaduraLey.FLOJA, ColorOroSoldadura.BLANCO)

        assertEquals(ColorOroSoldadura.BLANCO, calculo.color)
        assertExacto("0.5", calculo.oro18K)
    }

    @Test
    fun `cambiar el color no cambia ningun peso`() {
        val porColor = ColorOroSoldadura.entries.map { color ->
            calcular(BigDecimal("2.5"), DurezaSoldaduraLey.FUERTE, color)
        }

        porColor.zipWithNext().forEach { (uno, otro) ->
            assertTrue(uno.base.compareTo(otro.base) == 0)
            assertTrue(uno.oro18K.compareTo(otro.oro18K) == 0)
            assertTrue(uno.total.compareTo(otro.total) == 0)
        }
    }

    @Test
    fun `la base a cero o negativa se rechaza sin calcular`() {
        listOf("0", "-1").forEach { texto ->
            assertThrows(IllegalArgumentException::class.java) {
                calcular(BigDecimal(texto), DurezaSoldaduraLey.MEDIA, ColorOroSoldadura.AMARILLO)
            }
        }
    }

    private fun assertExacto(esperado: String, real: BigDecimal) {
        assertTrue(
            "esperado $esperado y salió ${real.toPlainString()}",
            BigDecimal(esperado).compareTo(real) == 0,
        )
    }
}
