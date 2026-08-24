package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraClasica
import java.math.BigDecimal
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Motor real, sin mocks. Valores esperados de §3 y §10 (TEST 1, TEST 2, TEST 3). */
class CalcularSoldaduraClasicaInversaUseCaseTest {

    private val calcular = CalcularSoldaduraClasicaInversaUseCase()

    @Test
    fun `test 1 - el lote patron flojo de 8 gramos recupera la receta`() {
        val calculo = calcular(BigDecimal("8"), TipoSoldaduraClasica.FLOJA)

        assertCerca("5", calculo.gramosDe(MetalSoldadura.ORO_18K))
        assertCerca("2", calculo.gramosDe(MetalSoldadura.PLATA_FINA))
        assertCerca("1", calculo.gramosDe(MetalSoldadura.LATON))
        assertCerca("8", calculo.total)
    }

    @Test
    fun `test 2 - el lote patron fuerte de 6,50 gramos recupera la receta`() {
        val calculo = calcular(BigDecimal("6.50"), TipoSoldaduraClasica.FUERTE)

        assertCerca("5", calculo.gramosDe(MetalSoldadura.ORO_18K))
        assertCerca("0.50", calculo.gramosDe(MetalSoldadura.PLATA_FINA))
        assertCerca("0.50", calculo.gramosDe(MetalSoldadura.COBRE))
        assertCerca("0.50", calculo.gramosDe(MetalSoldadura.LATON))
        assertCerca("6.50", calculo.total)
    }

    @Test
    fun `test 3 - la equivalencia de 10,08 gramos muy floja de ley recupera el por siete`() {
        val calculo = calcular(BigDecimal("10.08"), TipoSoldaduraClasica.MUY_FLOJA_LEY)

        assertCerca("7", calculo.gramosDe(MetalSoldadura.ORO_24K))
        assertCerca("0.70", calculo.gramosDe(MetalSoldadura.PLATA_FINA))
        assertCerca("1.12", calculo.gramosDe(MetalSoldadura.LATON))
        assertCerca("1.26", calculo.gramosDe(MetalSoldadura.CADMIO))
    }

    @Test
    fun `con division infinita la suma sigue coincidiendo con el peso pedido`() {
        // 10 ÷ 1,44 es periódico: la única división va a escala 15 y los componentes
        // salen de multiplicaciones exactas sobre ese factor, así que la suma interna
        // queda a menos de la tolerancia del peso pedido. La vista añade la nota de §8.3.
        val calculo = calcular(BigDecimal("10"), TipoSoldaduraClasica.MUY_FLOJA_LEY)

        assertCerca("6.944444444444444", calculo.gramosDe(MetalSoldadura.ORO_24K))
        assertCerca("0.694444444444444", calculo.gramosDe(MetalSoldadura.PLATA_FINA))
        assertCerca("1.111111111111111", calculo.gramosDe(MetalSoldadura.LATON))
        assertCerca("1.25", calculo.gramosDe(MetalSoldadura.CADMIO))
        assertTrue((calculo.total - BigDecimal("10")).abs() < CalculoSoldadura.TOLERANCIA)
    }

    @Test
    fun `el peso a cero o negativo se rechaza sin calcular`() {
        listOf("0", "-0.5").forEach { texto ->
            assertThrows(IllegalArgumentException::class.java) {
                calcular(BigDecimal(texto), TipoSoldaduraClasica.FLOJA)
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
