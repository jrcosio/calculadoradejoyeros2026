package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import java.math.BigDecimal
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Motor real, sin mocks. El modo de los mockups: base = oro / r (§5.4). */
class CalcularSoldaduraLeyDesdeOroUseCaseTest {

    private val calcular = CalcularSoldaduraLeyDesdeOroUseCase()

    @Test
    fun `el caso del mockup - 2 gramos de oro muy floja piden 6,67 de base`() {
        val calculo = calcular(
            BigDecimal("2"),
            DurezaSoldaduraLey.MUY_FLOJA,
            ColorOroSoldadura.AMARILLO,
        )

        assertCerca("6.666666666666667", calculo.base)
        assertCerca("8.666666666666667", calculo.total)
    }

    @Test
    fun `con dureza media la base iguala al oro`() {
        val calculo = calcular(
            BigDecimal("1"),
            DurezaSoldaduraLey.MEDIA,
            ColorOroSoldadura.AMARILLO,
        )

        assertCerca("1", calculo.base)
        assertCerca("2", calculo.total)
    }

    @Test
    fun `con dureza muy fuerte 3 gramos de oro piden 1 de base`() {
        val calculo = calcular(
            BigDecimal("3"),
            DurezaSoldaduraLey.MUY_FUERTE,
            ColorOroSoldadura.ROSA,
        )

        assertCerca("1", calculo.base)
        assertCerca("4", calculo.total)
    }

    @Test
    fun `a mas dureza menos base por gramo de oro`() {
        val bases = DurezaSoldaduraLey.entries.map { dureza ->
            calcular(BigDecimal("3"), dureza, ColorOroSoldadura.AMARILLO).base
        }

        bases.zipWithNext().forEach { (floja, fuerte) ->
            assertTrue("la base debe decrecer con la dureza", floja > fuerte)
        }
    }

    @Test
    fun `el oro a cero o negativo se rechaza sin calcular`() {
        listOf("0", "-2").forEach { texto ->
            assertThrows(IllegalArgumentException::class.java) {
                calcular(BigDecimal(texto), DurezaSoldaduraLey.MUY_FLOJA, ColorOroSoldadura.AMARILLO)
            }
        }
    }

    private fun assertCerca(esperado: String, real: BigDecimal) {
        val diferencia = (BigDecimal(esperado) - real).abs()
        assertTrue(
            "esperado $esperado y salió ${real.toPlainString()}",
            diferencia < BigDecimal("1E-6"),
        )
    }
}
