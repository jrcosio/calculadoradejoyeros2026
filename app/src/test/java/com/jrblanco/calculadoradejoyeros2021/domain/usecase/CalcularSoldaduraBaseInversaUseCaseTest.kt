package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalSoldadura
import java.math.BigDecimal
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Motor real, sin mocks. Valores esperados de §5.2, modo desde el peso de base. */
class CalcularSoldaduraBaseInversaUseCaseTest {

    private val calcular = CalcularSoldaduraBaseInversaUseCase()

    @Test
    fun `el peso patron de 13,26 gramos recupera los 10 de oro fino`() {
        val calculo = calcular(BigDecimal("13.26"))

        assertCerca("10", calculo.gramosDe(MetalSoldadura.ORO_24K))
        assertCerca("0.54", calculo.gramosDe(MetalSoldadura.COBRE))
        assertCerca("0.80", calculo.gramosDe(MetalSoldadura.PLATA_FINA))
        assertCerca("0.92", calculo.gramosDe(MetalSoldadura.ZINC))
        assertCerca("1.00", calculo.gramosDe(MetalSoldadura.CADMIO))
    }

    @Test
    fun `con division infinita la suma sigue coincidiendo con el peso pedido`() {
        // 10 ÷ 13,26 es infinita: única división a escala 15 y multiplicaciones exactas.
        val calculo = calcular(BigDecimal("10"))

        assertCerca("7.541478129713424", calculo.gramosDe(MetalSoldadura.ORO_24K))
        assertTrue((calculo.total - BigDecimal("10")).abs() < CalculoSoldadura.TOLERANCIA)
    }

    @Test
    fun `el inverso deshace al directo`() {
        val directo = CalcularSoldaduraBaseUseCase()

        val ida = directo(BigDecimal("7"))
        val vuelta = calcular(ida.total)

        assertCerca("7", vuelta.gramosDe(MetalSoldadura.ORO_24K))
    }

    @Test
    fun `el peso a cero o negativo se rechaza sin calcular`() {
        listOf("0", "-13.26").forEach { texto ->
            assertThrows(IllegalArgumentException::class.java) {
                calcular(BigDecimal(texto))
            }
        }
    }

    private fun CalculoSoldadura.gramosDe(metal: MetalSoldadura): BigDecimal =
        componentes.first { it.metal == metal }.gramos

    private fun assertCerca(esperado: String, real: BigDecimal) {
        val diferencia = (BigDecimal(esperado) - real).abs()
        assertTrue(
            "esperado $esperado y salió ${real.toPlainString()}",
            diferencia < BigDecimal("1E-6"),
        )
    }
}
