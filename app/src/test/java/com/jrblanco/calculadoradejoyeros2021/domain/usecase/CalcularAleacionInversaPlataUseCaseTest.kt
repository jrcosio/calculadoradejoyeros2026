package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Modo inverso (§22): sin interfaz en esta versión, pero probado desde el principio.
 */
class CalcularAleacionInversaPlataUseCaseTest {

    private val calcular = CalcularAleacionInversaPlataUseCase()

    @Test
    fun `el ejemplo del documento - 100 gramos finales de plata 925`() {
        val calculo = calcular(BigDecimal("100"), LeyPlata.LEY_925)

        assertCerca("92.592592592592593", calculo.masaOrigen)
        assertCerca("7.407407407407407", calculo.cobre)
        assertCerca("100.000000", calculo.masaFinal)
        assertTrue(calculo.leyTeorica >= LeyPlata.LEY_925.finura)
    }

    @Test
    fun `las cuatro leyes con 100 gramos finales`() {
        val esperados = mapOf(
            LeyPlata.LEY_950 to ("95.095095095095096" to "4.904904904904904"),
            LeyPlata.LEY_925 to ("92.592592592592593" to "7.407407407407407"),
            LeyPlata.LEY_900 to ("90.090090090090091" to "9.909909909909909"),
            LeyPlata.LEY_800 to ("80.080080080080081" to "19.919919919919919"),
        )

        esperados.forEach { (ley, valores) ->
            val (origen, cobre) = valores
            val calculo = calcular(BigDecimal("100"), ley)
            assertCerca(origen, calculo.masaOrigen)
            assertCerca(cobre, calculo.cobre)
        }
    }

    @Test
    fun `las cuatro leyes devuelven la masa final pedida sin bajar de ley`() {
        listOf("1", "50", "100", "1250.75").forEach { texto ->
            val deseada = BigDecimal(texto)
            LeyPlata.entries.forEach { ley ->
                val etiqueta = "$deseada g finales de $ley"
                val calculo = calcular(deseada, ley)

                // La masa final es exactamente la pedida y cuadra con origen + cobre.
                assertEquals("$etiqueta: masa final", 0, calculo.masaFinal.compareTo(deseada))
                assertEquals(
                    "$etiqueta: descuadre origen + cobre",
                    0,
                    calculo.masaOrigen.add(calculo.cobre).compareTo(deseada),
                )

                // Hace falta menos plata fina que aleación final, y cobre positivo.
                assertTrue("$etiqueta: origen", calculo.masaOrigen < deseada)
                assertTrue("$etiqueta: cobre", calculo.cobre > BigDecimal.ZERO)

                // La ley resultante nunca queda por debajo de la objetivo (§20).
                assertTrue(
                    "$etiqueta: ley ${calculo.leyTeorica} por debajo de ${ley.finura}",
                    calculo.leyTeorica >= ley.finura,
                )
            }
        }
    }

    @Test
    fun `el inverso deshace al directo`() {
        val directo = CalcularAleacionPlataUseCase()

        LeyPlata.entries.forEach { ley ->
            val ida = directo(BigDecimal("40"), ley)
            val vuelta = calcular(ida.masaFinal, ley)

            // Volver desde el peso final devuelve la plata de partida, salvo la
            // billonésima que separa el redondeo a la baja del de al alza.
            assertTrue(
                "$ley: ida 40 g → ${ida.masaFinal} g → vuelta ${vuelta.masaOrigen} g",
                (vuelta.masaOrigen - BigDecimal("40")).abs() < BigDecimal("1E-9"),
            )
        }
    }

    @Test
    fun `la masa cero se rechaza`() {
        assertThrows(IllegalArgumentException::class.java) {
            calcular(BigDecimal.ZERO, LeyPlata.LEY_925)
        }
    }

    @Test
    fun `la masa negativa se rechaza`() {
        assertThrows(IllegalArgumentException::class.java) {
            calcular(BigDecimal("-1"), LeyPlata.LEY_800)
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
