package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraPlata
import java.math.BigDecimal
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Motor real, sin mocks. Valores esperados de §4.3. */
class CalcularSoldaduraPlataInversaUseCaseTest {

    private val calcular = CalcularSoldaduraPlataInversaUseCase()

    @Test
    fun `el ejemplo del documento - 10 gramos muy floja reparten 5,714 de plata y 4,285 de laton`() {
        val calculo = calcular(BigDecimal("10"), TipoSoldaduraPlata.MUY_FLOJA)

        assertCerca("5.714285714285714", calculo.componentes[0].gramos)
        assertCerca("4.285714285714286", calculo.componentes[1].gramos)
    }

    @Test
    fun `el total recupera exactamente el peso pedido en los cuatro tipos`() {
        // El laton sale por resta, no de una segunda division: plata + laton == pedido.
        TipoSoldaduraPlata.entries.forEach { tipo ->
            val calculo = calcular(BigDecimal("100"), tipo)

            assertTrue(
                "el total de $tipo no recupera el peso pedido: ${calculo.total}",
                calculo.total.compareTo(BigDecimal("100")) == 0,
            )
        }
    }

    @Test
    fun `el inverso deshace al directo`() {
        val directo = CalcularSoldaduraPlataUseCase()

        TipoSoldaduraPlata.entries.forEach { tipo ->
            val ida = directo(BigDecimal("25"), tipo)
            val vuelta = calcular(ida.total, tipo)

            assertCerca("25", vuelta.componentes[0].gramos)
        }
    }

    @Test
    fun `el peso a cero o negativo se rechaza sin calcular`() {
        listOf("0", "-10").forEach { texto ->
            assertThrows(IllegalArgumentException::class.java) {
                calcular(BigDecimal(texto), TipoSoldaduraPlata.FLOJA)
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
