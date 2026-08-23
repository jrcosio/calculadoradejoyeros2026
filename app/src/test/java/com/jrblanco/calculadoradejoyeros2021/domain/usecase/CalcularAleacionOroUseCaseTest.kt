package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoAleacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalLiga
import com.jrblanco.calculadoradejoyeros2021.domain.model.RecetasOro
import java.math.BigDecimal
import java.math.RoundingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El motor es puro y determinista: se prueba sin mocks, contra los valores del
 * documento técnico `ESPECIFICACION_CALCULADORA_ALEACIONES_ORO.md` (§ citados).
 */
class CalcularAleacionOroUseCaseTest {

    private val calcular = CalcularAleacionOroUseCase()

    // --- Los cinco casos de prueba obligatorios (§13) ---

    @Test
    fun `caso 1 - amarillo 18K con 10 gramos`() {
        val calculo = calcular(BigDecimal("10.000"), ColorOro.AMARILLO, LeyOro.LEY_18K)

        assertCerca("9.990000", calculo.oroPuro)
        assertCerca("13.320000", calculo.masaFinal)
        assertCerca("3.320000", calculo.ligaTotal)
        assertCerca("2.191419142", calculo.metales.getValue(MetalLiga.PLATA_FINA))
        assertCerca("1.128580858", calculo.metales.getValue(MetalLiga.COBRE))
        assertCerca("0.750000000", calculo.leyTeorica)
    }

    @Test
    fun `caso 2 - blanco 18K con 10 gramos`() {
        val calculo = calcular(BigDecimal("10.000"), ColorOro.BLANCO, LeyOro.LEY_18K)

        assertCerca("3.320000", calculo.ligaTotal)
        assertCerca("1.469246925", calculo.metales.getValue(MetalLiga.PALADIO))
        assertCerca("1.312859286", calculo.metales.getValue(MetalLiga.PLATA_FINA))
        assertCerca("0.537893789", calculo.metales.getValue(MetalLiga.COBRE))
    }

    @Test
    fun `caso 3 - rosa 18K con 17,35 gramos`() {
        val calculo = calcular(BigDecimal("17.350"), ColorOro.ROSA, LeyOro.LEY_18K)

        assertCerca("17.332650", calculo.oroPuro)
        assertCerca("23.110200", calculo.masaFinal)
        assertCerca("5.760200", calculo.ligaTotal)
        assertCerca("0.645142400", calculo.metales.getValue(MetalLiga.PLATA_FINA))
        assertCerca("5.115057600", calculo.metales.getValue(MetalLiga.COBRE))
    }

    @Test
    fun `caso 4 - amarillo 14K con 25 gramos`() {
        val calculo = calcular(BigDecimal("25.000"), ColorOro.AMARILLO, LeyOro.LEY_14K)

        assertCerca("24.975000", calculo.oroPuro)
        assertCerca("42.692307692", calculo.masaFinal)
        assertCerca("17.692307692", calculo.ligaTotal)
        assertCerca("12.728278915", calculo.metales.getValue(MetalLiga.PLATA_FINA))
        assertCerca("4.964028777", calculo.metales.getValue(MetalLiga.COBRE))
    }

    @Test
    fun `caso 5 - rojo 9K con 5 gramos`() {
        val calculo = calcular(BigDecimal("5.000"), ColorOro.ROJO, LeyOro.LEY_9K)

        assertCerca("4.995000", calculo.oroPuro)
        assertCerca("13.320000", calculo.masaFinal)
        assertCerca("8.320000", calculo.ligaTotal)
        assertCerca("8.320000", calculo.metales.getValue(MetalLiga.COBRE))
        assertEquals(setOf(MetalLiga.COBRE), calculo.metales.keys)
    }

    // --- Las 16 combinaciones color×ley (§20) ---

    @Test
    fun `las 16 combinaciones cumplen los invariantes del documento`() {
        val cienGramos = BigDecimal("100")

        ColorOro.entries.forEach { color ->
            LeyOro.entries.forEach { ley ->
                val etiqueta = "$color $ley"
                val receta = RecetasOro.receta(color, ley)
                val calculo = calcular(cienGramos, color, ley)

                // 1. La liga es positiva.
                assertTrue("$etiqueta: liga no positiva", calculo.ligaTotal > BigDecimal.ZERO)

                // 2. Todos los metales añadidos son positivos (los no usados no existen).
                assertTrue(
                    "$etiqueta: metal no positivo",
                    calculo.metales.values.all { it > BigDecimal.ZERO },
                )

                // 3. Las proporciones de la receta suman 1 (tolerancia computacional).
                val sumaProporciones = receta.proporciones.values.fold(BigDecimal.ZERO, BigDecimal::add)
                assertTrue(
                    "$etiqueta: proporciones suman $sumaProporciones",
                    (sumaProporciones - BigDecimal.ONE).abs() < CalculoAleacion.TOLERANCIA,
                )

                // 4. La suma de metales coincide con la liga.
                val sumaMetales = calculo.metales.values.fold(BigDecimal.ZERO, BigDecimal::add)
                assertTrue(
                    "$etiqueta: metales ($sumaMetales) != liga (${calculo.ligaTotal})",
                    (sumaMetales - calculo.ligaTotal).abs() < CalculoAleacion.TOLERANCIA,
                )

                // 5. La masa final es exactamente origen + liga.
                assertEquals(
                    "$etiqueta: masa final descuadrada",
                    0,
                    calculo.masaFinal.compareTo(calculo.masaOrigen.add(calculo.ligaTotal)),
                )

                // 6. La ley teórica nunca queda por debajo de la objetivo.
                assertTrue(
                    "$etiqueta: ley ${calculo.leyTeorica} por debajo de ${ley.finura}",
                    calculo.leyTeorica >= ley.finura,
                )

                // 7. El redondeo de vista no altera el valor interno.
                calculo.metales.values.forEach { interno ->
                    val antes = BigDecimal(interno.toPlainString())
                    interno.setScale(3, RoundingMode.HALF_UP)
                    assertEquals("$etiqueta: el redondeo mutó el valor", 0, interno.compareTo(antes))
                }
            }
        }
    }

    // --- Muestreo de la tabla maestra (§8, valores de §7 a 6 decimales) ---

    @Test
    fun `la tabla maestra se reproduce con 100 gramos`() {
        val cien = BigDecimal("100")

        with(calcular(cien, ColorOro.AMARILLO, LeyOro.LEY_18K)) {
            assertCerca("33.200000", ligaTotal)
            assertCerca("21.914191", metales.getValue(MetalLiga.PLATA_FINA))
            assertCerca("11.285809", metales.getValue(MetalLiga.COBRE))
            assertCerca("133.200000", masaFinal)
        }
        with(calcular(cien, ColorOro.BLANCO, LeyOro.LEY_14K)) {
            assertCerca("54.646744", metales.getValue(MetalLiga.PLATA_FINA))
            assertCerca("16.122487", metales.getValue(MetalLiga.PALADIO))
        }
        with(calcular(cien, ColorOro.ROSA, LeyOro.LEY_12K)) {
            assertCerca("26.965192", metales.getValue(MetalLiga.PLATA_FINA))
            assertCerca("72.834808", metales.getValue(MetalLiga.COBRE))
        }
        with(calcular(cien, ColorOro.ROJO, LeyOro.LEY_9K)) {
            assertCerca("166.400000", metales.getValue(MetalLiga.COBRE))
            assertCerca("266.400000", masaFinal)
        }
    }

    @Test
    fun `el blanco 9K es solo plata y el rojo es solo cobre`() {
        val blanco9k = calcular(BigDecimal("10"), ColorOro.BLANCO, LeyOro.LEY_9K)
        assertEquals(setOf(MetalLiga.PLATA_FINA), blanco9k.metales.keys)

        LeyOro.entries.forEach { ley ->
            val rojo = calcular(BigDecimal("10"), ColorOro.ROJO, ley)
            assertEquals(setOf(MetalLiga.COBRE), rojo.metales.keys)
        }
    }

    // --- Validaciones (§16) ---

    @Test
    fun `la masa cero se rechaza`() {
        assertThrows(IllegalArgumentException::class.java) {
            calcular(BigDecimal.ZERO, ColorOro.AMARILLO, LeyOro.LEY_18K)
        }
    }

    @Test
    fun `la masa negativa se rechaza`() {
        assertThrows(IllegalArgumentException::class.java) {
            calcular(BigDecimal("-5"), ColorOro.AMARILLO, LeyOro.LEY_18K)
        }
    }

    @Test
    fun `una masa minuscula conserva la precision`() {
        val calculo = calcular(BigDecimal("0.001"), ColorOro.AMARILLO, LeyOro.LEY_18K)
        assertCerca("0.000332", calculo.ligaTotal)
        assertTrue(calculo.leyTeorica >= LeyOro.LEY_18K.finura)
    }

    private fun assertCerca(esperado: String, real: BigDecimal) {
        val diferencia = (BigDecimal(esperado) - real).abs()
        assertTrue(
            "esperado $esperado y salió ${real.toPlainString()}",
            diferencia < BigDecimal("1E-6"),
        )
    }
}
