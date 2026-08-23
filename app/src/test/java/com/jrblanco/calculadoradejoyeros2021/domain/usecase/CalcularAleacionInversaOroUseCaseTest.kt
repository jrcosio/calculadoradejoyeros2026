package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Modo inverso (§14): sin interfaz en esta versión, pero probado desde el principio.
 */
class CalcularAleacionInversaOroUseCaseTest {

    private val calcular = CalcularAleacionInversaOroUseCase()

    @Test
    fun `el ejemplo del documento - 20 gramos finales de amarillo 18K`() {
        val calculo = calcular(BigDecimal("20.000"), ColorOro.AMARILLO, LeyOro.LEY_18K)

        assertCerca("15.015015015", calculo.masaOrigen)
        assertCerca("4.984984985", calculo.ligaTotal)
        assertCerca("20.000000", calculo.masaFinal)
    }

    @Test
    fun `las 16 combinaciones devuelven la masa final pedida sin bajar de ley`() {
        val cienGramos = BigDecimal("100")

        ColorOro.entries.forEach { color ->
            LeyOro.entries.forEach { ley ->
                val etiqueta = "$color $ley"
                val calculo = calcular(cienGramos, color, ley)

                // La masa final es exactamente la pedida y cuadra con origen + liga.
                assertEquals("$etiqueta: masa final", 0, calculo.masaFinal.compareTo(cienGramos))
                assertEquals(
                    "$etiqueta: descuadre origen + liga",
                    0,
                    calculo.masaOrigen.add(calculo.ligaTotal).compareTo(cienGramos),
                )

                // Hace falta menos oro 999 que aleación final, y liga positiva.
                assertTrue("$etiqueta: origen", calculo.masaOrigen < cienGramos)
                assertTrue("$etiqueta: liga", calculo.ligaTotal > BigDecimal.ZERO)

                // La ley resultante nunca queda por debajo de la objetivo (§12).
                assertTrue(
                    "$etiqueta: ley ${calculo.leyTeorica} por debajo de ${ley.finura}",
                    calculo.leyTeorica >= ley.finura,
                )
            }
        }
    }

    @Test
    fun `la masa cero se rechaza`() {
        assertThrows(IllegalArgumentException::class.java) {
            calcular(BigDecimal.ZERO, ColorOro.AMARILLO, LeyOro.LEY_18K)
        }
    }

    @Test
    fun `la masa negativa se rechaza`() {
        assertThrows(IllegalArgumentException::class.java) {
            calcular(BigDecimal("-1"), ColorOro.BLANCO, LeyOro.LEY_14K)
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
