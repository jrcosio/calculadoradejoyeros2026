package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** El conversor es puro: se prueba sin mocks contra valores calculados aparte con precisión 40. */
class ConversorUnidadesPrecioTest {

    @Test
    fun `una onza troy de precio vale exactamente un gramo`() {
        val resultado = ConversorUnidadesPrecio.convertir(BigDecimal("31.1034768"), UnidadPrecio.ONZA_TROY, UnidadPrecio.GRAMO)
        assertEquals(0, BigDecimal.ONE.compareTo(resultado))
        assertEquals(ConversorUnidadesPrecio.ESCALA, resultado.scale())
    }

    @Test
    fun `de gramo a kilo se multiplica por mil, exacto`() {
        val resultado = ConversorUnidadesPrecio.convertir(BigDecimal("1"), UnidadPrecio.GRAMO, UnidadPrecio.KILO)
        assertEquals(0, BigDecimal("1000").compareTo(resultado))
    }

    @Test
    fun `de kilo a onza troy`() {
        val resultado = ConversorUnidadesPrecio.convertir(BigDecimal("1"), UnidadPrecio.KILO, UnidadPrecio.ONZA_TROY)
        assertEquals(0, BigDecimal("0.0311034768").compareTo(resultado))
    }

    @Test
    fun `el oro de la muestra real por gramo y por kilo`() {
        val porOnza = BigDecimal("4606.4")
        val porGramo = ConversorUnidadesPrecio.convertir(porOnza, UnidadPrecio.ONZA_TROY, UnidadPrecio.GRAMO)
        val porKilo = ConversorUnidadesPrecio.convertir(porOnza, UnidadPrecio.ONZA_TROY, UnidadPrecio.KILO)
        assertCerca("148.0991989937", porGramo)
        assertCerca("148099.1989937279", porKilo)
        // Lo que verá el joyero (SC-002).
        assertEquals(BigDecimal("148.10"), porGramo.setScale(2, RoundingMode.HALF_UP))
        assertEquals(BigDecimal("148099.20"), porKilo.setScale(2, RoundingMode.HALF_UP))
    }

    @Test
    fun `el cobre pequeno conserva decimales`() {
        val resultado = ConversorUnidadesPrecio.convertir(BigDecimal("2.49"), UnidadPrecio.ONZA_TROY, UnidadPrecio.GRAMO)
        assertCerca("0.0800553590", resultado)
        assertEquals(BigDecimal("0.0801"), resultado.setScale(4, RoundingMode.HALF_UP))
    }

    @Test
    fun `la misma unidad devuelve el importe intacto`() {
        val importe = BigDecimal("12.345")
        UnidadPrecio.entries.forEach { unidad ->
            assertEquals(importe, ConversorUnidadesPrecio.convertir(importe, unidad, unidad))
        }
    }

    @Test
    fun `ida y vuelta recupera el importe dentro de la escala`() {
        val importe = BigDecimal("4606.4")
        UnidadPrecio.entries.forEach { desde ->
            UnidadPrecio.entries.forEach { hacia ->
                val vuelta = ConversorUnidadesPrecio.convertir(
                    ConversorUnidadesPrecio.convertir(importe, desde, hacia), hacia, desde,
                )
                assertCerca(importe.toPlainString(), vuelta)
            }
        }
    }

    @Test
    fun `los importes negativos se convierten con el mismo factor`() {
        val variacion = BigDecimal("-45.30000000000018")
        val porGramo = ConversorUnidadesPrecio.convertir(variacion, UnidadPrecio.ONZA_TROY, UnidadPrecio.GRAMO)
        assertTrue(porGramo.signum() < 0)
        assertCerca("-1.4564288196", porGramo)
    }

    private fun assertCerca(esperado: String, real: BigDecimal) {
        val diferencia = (BigDecimal(esperado) - real).abs()
        assertTrue("esperado $esperado y salió ${real.toPlainString()}", diferencia < BigDecimal("1E-6"))
    }
}
