package com.jrblanco.calculadoradejoyeros2021.core.util

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DecimalesTest {

    @Test
    fun `la coma y el punto decimal producen el mismo valor`() {
        assertEquals(0, BigDecimal("0.5").compareTo(parsearDecimalPositivo("0,5")!!))
        assertEquals(0, BigDecimal("0.5").compareTo(parsearDecimalPositivo("0.5")!!))
    }

    @Test
    fun `acepta enteros y espacios alrededor`() {
        assertEquals(0, BigDecimal("10").compareTo(parsearDecimalPositivo("10")!!))
        assertEquals(0, BigDecimal("7.25").compareTo(parsearDecimalPositivo(" 7,25 ")!!))
    }

    @Test
    fun `rechaza vacio, cero, negativos, texto y separadores repetidos`() {
        listOf("", "  ", "0", "0,0", "-1", "abc", "1,2,3", "1.2.3", "1,2.3").forEach { texto ->
            assertNull("«$texto» debería ser inválido", parsearDecimalPositivo(texto))
        }
    }
}
