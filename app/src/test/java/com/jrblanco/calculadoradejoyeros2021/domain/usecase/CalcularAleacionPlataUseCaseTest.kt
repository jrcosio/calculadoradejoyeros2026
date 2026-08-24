package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoPlata
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata
import java.math.BigDecimal
import java.math.RoundingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El motor es puro y determinista: se prueba sin mocks, contra los valores del documento
 * técnico `ESPECIFICACION_CALCULADORA_ALEACIONES_PLATA.md` (§ citados).
 */
class CalcularAleacionPlataUseCaseTest {

    private val calcular = CalcularAleacionPlataUseCase()

    // --- Los cuatro casos de prueba obligatorios (§21), con 10 g de Ag999 ---

    @Test
    fun `test A - 10 gramos hacia 950`() {
        val calculo = calcular(BigDecimal("10.000"), LeyPlata.LEY_950)

        assertCerca("9.990000", calculo.plataPura)
        assertCerca("0.515789473684210", calculo.cobre)
        assertCerca("10.515789473684210", calculo.masaFinal)
        assertTrue(calculo.leyTeorica >= LeyPlata.LEY_950.finura)
    }

    @Test
    fun `test B - 10 gramos hacia 925`() {
        val calculo = calcular(BigDecimal("10.000"), LeyPlata.LEY_925)

        assertCerca("9.990000", calculo.plataPura)
        assertCerca("0.800000", calculo.cobre)
        assertCerca("10.800000", calculo.masaFinal)
        assertCerca("0.925000", calculo.leyTeorica)
    }

    @Test
    fun `test C - 10 gramos hacia 900`() {
        val calculo = calcular(BigDecimal("10.000"), LeyPlata.LEY_900)

        assertCerca("1.100000", calculo.cobre)
        assertCerca("11.100000", calculo.masaFinal)
        assertCerca("0.900000", calculo.leyTeorica)
    }

    @Test
    fun `test D - 10 gramos hacia 800`() {
        val calculo = calcular(BigDecimal("10.000"), LeyPlata.LEY_800)

        assertCerca("2.487500", calculo.cobre)
        assertCerca("12.487500", calculo.masaFinal)
        assertCerca("0.800000", calculo.leyTeorica)
    }

    // --- Tabla de taller (§7): por cada 100 g de Ag999 ---

    @Test
    fun `la tabla de taller se reproduce con 100 gramos`() {
        val cien = BigDecimal("100")

        with(calcular(cien, LeyPlata.LEY_950)) {
            assertCerca("99.900000", plataPura)
            assertCerca("5.157894736842105", cobre)
            assertCerca("105.157894736842105", masaFinal)
        }
        with(calcular(cien, LeyPlata.LEY_925)) {
            assertCerca("8.000000", cobre)
            assertCerca("108.000000", masaFinal)
        }
        with(calcular(cien, LeyPlata.LEY_900)) {
            assertCerca("11.000000", cobre)
            assertCerca("111.000000", masaFinal)
        }
        with(calcular(cien, LeyPlata.LEY_800)) {
            assertCerca("24.875000", cobre)
            assertCerca("124.875000", masaFinal)
        }
    }

    // --- Coeficientes exactos desde Ag999 (§6) ---

    @Test
    fun `el cobre por gramo de plata fina son los coeficientes del documento`() {
        val esperados = mapOf(
            LeyPlata.LEY_950 to "0.0515789473684210",
            LeyPlata.LEY_925 to "0.0800000000000000",
            LeyPlata.LEY_900 to "0.1100000000000000",
            LeyPlata.LEY_800 to "0.2487500000000000",
        )

        esperados.forEach { (ley, coeficiente) ->
            val calculo = calcular(BigDecimal("1"), ley)
            assertCerca(coeficiente, calculo.cobre)
        }
    }

    @Test
    fun `el caso del mockup - 25 gramos hacia 925 son 2 gramos de cobre`() {
        val calculo = calcular(BigDecimal("25"), LeyPlata.LEY_925)

        assertCerca("2.000000", calculo.cobre)
        assertCerca("27.000000", calculo.masaFinal)
    }

    // --- Invariantes de las cuatro leyes (§20) ---

    @Test
    fun `las cuatro leyes cumplen los invariantes del documento`() {
        listOf("0.5", "10", "17.35", "100", "2500").forEach { texto ->
            val masa = BigDecimal(texto)
            LeyPlata.entries.forEach { ley ->
                val etiqueta = "$masa g hacia $ley"
                val calculo = calcular(masa, ley)

                // 1. El cobre a añadir es positivo.
                assertTrue("$etiqueta: cobre no positivo", calculo.cobre > BigDecimal.ZERO)

                // 2. La masa final es exactamente origen + cobre.
                assertEquals(
                    "$etiqueta: masa final descuadrada",
                    0,
                    calculo.masaFinal.compareTo(calculo.masaOrigen.add(calculo.cobre)),
                )

                // 3. La plata pura es la masa de partida al 99,9 %, nunca al 100 % (§4).
                assertEquals(
                    "$etiqueta: plata pura mal calculada",
                    0,
                    calculo.plataPura.compareTo(masa.multiply(BigDecimal("0.999"))),
                )

                // 4. La ley teórica nunca queda por debajo de la objetivo.
                assertTrue(
                    "$etiqueta: ley ${calculo.leyTeorica} por debajo de ${ley.finura}",
                    calculo.leyTeorica >= ley.finura,
                )

                // 5. Hace falta menos plata fina que aleación final.
                assertTrue("$etiqueta: masa final no crece", calculo.masaFinal > masa)

                // 6. El redondeo de vista no altera el valor interno (§21).
                val antes = BigDecimal(calculo.cobre.toPlainString())
                calculo.cobre.setScale(3, RoundingMode.DOWN)
                assertEquals("$etiqueta: el redondeo mutó el valor", 0, calculo.cobre.compareTo(antes))
            }
        }
    }

    /**
     * La propiedad que exige §20 y mide SC-003: pesar el cobre **truncado a la resolución
     * de la balanza** (0,001 g, §18) nunca deja la aleación por debajo de la ley objetivo.
     * Con redondeo a la media fallaría en la mitad de los casos de §21.
     */
    @Test
    fun `el cobre truncado a milesimas nunca baja de la ley objetivo`() {
        listOf("0.5", "1", "7.77", "10", "12.35", "100", "333.333").forEach { texto ->
            val masa = BigDecimal(texto)
            LeyPlata.entries.forEach { ley ->
                val calculo = calcular(masa, ley)
                val cobrePesable = calculo.cobre.setScale(3, RoundingMode.DOWN)
                val leyPractica = calculo.plataPura.divide(
                    masa.add(cobrePesable),
                    CalculoPlata.ESCALA,
                    RoundingMode.DOWN,
                )

                assertTrue(
                    "$masa g hacia $ley: con $cobrePesable g de cobre la ley práctica " +
                        "($leyPractica) queda por debajo de ${ley.finura}",
                    leyPractica >= ley.finura,
                )
            }
        }
    }

    @Test
    fun `una masa minuscula conserva la precision aunque no sea pesable`() {
        val calculo = calcular(BigDecimal("0.001"), LeyPlata.LEY_925)

        assertCerca("0.00008", calculo.cobre)
        assertTrue(calculo.cobre > BigDecimal.ZERO)
        assertTrue(calculo.leyTeorica >= LeyPlata.LEY_925.finura)
    }

    // --- Validaciones (§26) ---

    @Test
    fun `la masa cero se rechaza`() {
        assertThrows(IllegalArgumentException::class.java) {
            calcular(BigDecimal.ZERO, LeyPlata.LEY_925)
        }
    }

    @Test
    fun `la masa negativa se rechaza`() {
        assertThrows(IllegalArgumentException::class.java) {
            calcular(BigDecimal("-5"), LeyPlata.LEY_925)
        }
    }

    @Test
    fun `solo 950 y 900 son leyes tecnicas`() {
        assertTrue(LeyPlata.LEY_950.esSoloTecnica)
        assertTrue(LeyPlata.LEY_900.esSoloTecnica)
        assertTrue(!LeyPlata.LEY_925.esSoloTecnica)
        assertTrue(!LeyPlata.LEY_800.esSoloTecnica)
    }

    @Test
    fun `las cuatro finuras son menores que la de origen`() {
        LeyPlata.entries.forEach { ley ->
            assertTrue("$ley no baja de 999", ley.finura < CalculoPlata.FINURA_ORIGEN)
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
