package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraClasica
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Motor real, sin mocks: es puro y determinista. Valores esperados de §3 (FR-015). */
class CalcularSoldaduraClasicaUseCaseTest {

    private val calcular = CalcularSoldaduraClasicaUseCase()

    // --- Los casos de los mockups, entrando por el oro de la receta (SC-003) ---

    @Test
    fun `floja con 10 gramos de oro reparte 4 de plata y 2 de laton`() {
        val calculo = calcular(BigDecimal("10"), TipoSoldaduraClasica.FLOJA)

        assertEquals(
            listOf(MetalSoldadura.ORO_18K, MetalSoldadura.PLATA_FINA, MetalSoldadura.LATON),
            calculo.componentes.map { it.metal },
        )
        assertCerca("10", calculo.gramosDe(MetalSoldadura.ORO_18K))
        assertCerca("4", calculo.gramosDe(MetalSoldadura.PLATA_FINA))
        assertCerca("2", calculo.gramosDe(MetalSoldadura.LATON))
        assertCerca("16", calculo.total)
    }

    @Test
    fun `fuerte con 10 gramos de oro reparte 1 de plata 1 de cobre y 1 de laton`() {
        val calculo = calcular(BigDecimal("10"), TipoSoldaduraClasica.FUERTE)

        assertCerca("1", calculo.gramosDe(MetalSoldadura.PLATA_FINA))
        assertCerca("1", calculo.gramosDe(MetalSoldadura.COBRE))
        assertCerca("1", calculo.gramosDe(MetalSoldadura.LATON))
        assertCerca("13", calculo.total)
    }

    @Test
    fun `muy floja de ley con 7 gramos de oro es la equivalencia por siete del documento`() {
        // §3.4: 7 g de oro 24K → 0,70 / 1,12 / 1,26. El total es 10,08 g, NO un lote de 10.
        val calculo = calcular(BigDecimal("7"), TipoSoldaduraClasica.MUY_FLOJA_LEY)

        assertCerca("0.70", calculo.gramosDe(MetalSoldadura.PLATA_FINA))
        assertCerca("1.12", calculo.gramosDe(MetalSoldadura.LATON))
        assertCerca("1.26", calculo.gramosDe(MetalSoldadura.CADMIO))
        assertCerca("10.08", calculo.total)
    }

    // --- Propiedades de §10 ---

    @Test
    fun `duplicar la entrada duplica todos los componentes`() {
        TipoSoldaduraClasica.entries.forEach { tipo ->
            val simple = calcular(BigDecimal("3"), tipo)
            val doble = calcular(BigDecimal("6"), tipo)

            simple.componentes.zip(doble.componentes).forEach { (uno, dos) ->
                assertCerca(uno.gramos.multiply(BigDecimal("2")).toPlainString(), dos.gramos)
            }
        }
    }

    @Test
    fun `todos los componentes son positivos y suman el total en los tres tipos`() {
        TipoSoldaduraClasica.entries.forEach { tipo ->
            val calculo = calcular(BigDecimal("2.5"), tipo)

            assertTrue(calculo.componentes.all { it.gramos > BigDecimal.ZERO })
            val suma = calculo.componentes.fold(BigDecimal.ZERO) { acc, c -> acc.add(c.gramos) }
            assertTrue((suma - calculo.total).abs() < CalculoSoldadura.TOLERANCIA)
        }
    }

    // --- Validaciones (§8.1, TEST 10) ---

    @Test
    fun `el oro a cero o negativo se rechaza sin calcular`() {
        listOf("0", "-1").forEach { texto ->
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
