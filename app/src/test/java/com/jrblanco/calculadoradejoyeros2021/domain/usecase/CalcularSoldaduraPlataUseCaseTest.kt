package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraPlata
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Motor real, sin mocks. Sin división en este modo (§4.2): los valores son exactos y se
 * comparan con `compareTo`, no con tolerancia.
 */
class CalcularSoldaduraPlataUseCaseTest {

    private val calcular = CalcularSoldaduraPlataUseCase()

    @Test
    fun `test 4 - 25 gramos muy floja dan 18,75 de laton y 43,75 de total`() {
        val calculo = calcular(BigDecimal("25"), TipoSoldaduraPlata.MUY_FLOJA)

        assertExacto("25", calculo.componentes[0].gramos)
        assertEquals(MetalSoldadura.PLATA_FINA, calculo.componentes[0].metal)
        assertExacto("18.75", calculo.componentes[1].gramos)
        assertEquals(MetalSoldadura.LATON, calculo.componentes[1].metal)
        assertExacto("43.75", calculo.total)
    }

    @Test
    fun `test 5 - 25 gramos fuerte dan 7,50 de laton y 32,50 de total`() {
        val calculo = calcular(BigDecimal("25"), TipoSoldaduraPlata.FUERTE)

        assertExacto("7.50", calculo.componentes[1].gramos)
        assertExacto("32.50", calculo.total)
    }

    @Test
    fun `la tabla completa del documento con 25 gramos`() {
        // §4.2: el porcentaje es laton respecto a la plata, no sobre el peso final.
        val esperados = mapOf(
            TipoSoldaduraPlata.MUY_FLOJA to ("18.75" to "43.75"),
            TipoSoldaduraPlata.FLOJA to ("12.50" to "37.50"),
            TipoSoldaduraPlata.NORMAL to ("10.00" to "35.00"),
            TipoSoldaduraPlata.FUERTE to ("7.50" to "32.50"),
        )

        esperados.forEach { (tipo, valores) ->
            val (laton, total) = valores
            val calculo = calcular(BigDecimal("25"), tipo)
            assertExacto(laton, calculo.componentes[1].gramos)
            assertExacto(total, calculo.total)
        }
    }

    @Test
    fun `duplicar la plata duplica el laton y el total`() {
        TipoSoldaduraPlata.entries.forEach { tipo ->
            val simple = calcular(BigDecimal("7.77"), tipo)
            val doble = calcular(BigDecimal("15.54"), tipo)

            assertExacto(
                simple.componentes[1].gramos.multiply(BigDecimal("2")).toPlainString(),
                doble.componentes[1].gramos,
            )
        }
    }

    @Test
    fun `la plata a cero o negativa se rechaza sin calcular`() {
        listOf("0", "-25").forEach { texto ->
            assertThrows(IllegalArgumentException::class.java) {
                calcular(BigDecimal(texto), TipoSoldaduraPlata.MUY_FLOJA)
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
