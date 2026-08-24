package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import java.math.BigDecimal
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Motor real, sin mocks. Modo desde el peso final (§5.4, TEST 8). */
class CalcularSoldaduraLeyInversaUseCaseTest {

    private val calcular = CalcularSoldaduraLeyInversaUseCase()

    @Test
    fun `test 8 - 10 gramos muy fuerte reparten 2,5 de base y 7,5 de oro`() {
        val calculo = calcular(
            BigDecimal("10"),
            DurezaSoldaduraLey.MUY_FUERTE,
            ColorOroSoldadura.AMARILLO,
        )

        assertCerca("2.5", calculo.base)
        assertCerca("7.5", calculo.oro18K)
        assertTrue(calculo.total.compareTo(BigDecimal("10")) == 0)
    }

    @Test
    fun `10 gramos muy floja reparten 7,692 de base y 2,307 de oro`() {
        // §5.4, tabla de T = 10: división infinita 10 ÷ 1,3.
        val calculo = calcular(
            BigDecimal("10"),
            DurezaSoldaduraLey.MUY_FLOJA,
            ColorOroSoldadura.BLANCO,
        )

        assertCerca("7.692307692307692", calculo.base)
        assertCerca("2.307692307692308", calculo.oro18K)
    }

    @Test
    fun `el total recupera exactamente el peso pedido en las cinco durezas`() {
        // El oro sale por resta, no de una segunda division: base + oro == pedido.
        DurezaSoldaduraLey.entries.forEach { dureza ->
            val calculo = calcular(BigDecimal("100"), dureza, ColorOroSoldadura.ROSA)

            assertTrue(
                "el total de $dureza no recupera el peso pedido: ${calculo.total}",
                calculo.total.compareTo(BigDecimal("100")) == 0,
            )
        }
    }

    @Test
    fun `el peso a cero o negativo se rechaza sin calcular`() {
        listOf("0", "-10").forEach { texto ->
            assertThrows(IllegalArgumentException::class.java) {
                calcular(BigDecimal(texto), DurezaSoldaduraLey.MEDIA, ColorOroSoldadura.AMARILLO)
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
