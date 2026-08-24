package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalSoldadura
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Motor real, sin mocks. Valores esperados de §5.2 — los del documento técnico, NO los
 * del mockup, que los muestra intercambiados (§12).
 */
class CalcularSoldaduraBaseUseCaseTest {

    private val calcular = CalcularSoldaduraBaseUseCase()

    @Test
    fun `test 6 - 10 gramos de oro fino reparten la receta patron de la base`() {
        val calculo = calcular(BigDecimal("10"))

        assertEquals(
            listOf(
                MetalSoldadura.ORO_24K,
                MetalSoldadura.COBRE,
                MetalSoldadura.PLATA_FINA,
                MetalSoldadura.ZINC,
                MetalSoldadura.CADMIO,
            ),
            calculo.componentes.map { it.metal },
        )
        assertCerca("10", calculo.gramosDe(MetalSoldadura.ORO_24K))
        assertCerca("0.54", calculo.gramosDe(MetalSoldadura.COBRE))
        assertCerca("0.80", calculo.gramosDe(MetalSoldadura.PLATA_FINA))
        assertCerca("0.92", calculo.gramosDe(MetalSoldadura.ZINC))
        assertCerca("1.00", calculo.gramosDe(MetalSoldadura.CADMIO))
        assertCerca("13.26", calculo.total)
    }

    @Test
    fun `con 7 gramos de oro la receta escala por 0,7`() {
        val calculo = calcular(BigDecimal("7"))

        assertCerca("0.378", calculo.gramosDe(MetalSoldadura.COBRE))
        assertCerca("0.56", calculo.gramosDe(MetalSoldadura.PLATA_FINA))
        assertCerca("0.644", calculo.gramosDe(MetalSoldadura.ZINC))
        assertCerca("0.7", calculo.gramosDe(MetalSoldadura.CADMIO))
        assertCerca("9.282", calculo.total)
    }

    @Test
    fun `la base no se corrige hacia 750 milesimas`() {
        // §5.2: el total es exactamente 13,26 × factor. Si alguien «corrigiera» los pesos
        // para que el oro fuera el 750‰ del total, esta igualdad dejaría de cumplirse.
        val calculo = calcular(BigDecimal("10"))

        assertTrue(
            "el total debe ser 13,26 exacto y salió ${calculo.total.toPlainString()}",
            calculo.total.compareTo(BigDecimal("13.26")) == 0,
        )
    }

    @Test
    fun `el oro a cero o negativo se rechaza sin calcular`() {
        listOf("0", "-10").forEach { texto ->
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
