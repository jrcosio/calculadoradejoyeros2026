package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProporcionesChapaTest {

    private fun desde(ancho: String?, largo: String?, espesor: String?) =
        ProporcionesChapa.desde(ancho?.let(::BigDecimal), largo?.let(::BigDecimal), espesor?.let(::BigDecimal))

    @Test
    fun `el mayor de ancho y largo vale uno`() {
        assertEquals(1f, desde("10", "20", "0.5").largo)
        assertEquals(1f, desde("30", "20", "0.5").ancho)
        val cuadrada = desde("10", "10", "1")
        assertEquals(1f, cuadrada.ancho)
        assertEquals(1f, cuadrada.largo)
    }

    @Test
    fun `una chapa 1 a 4 se ve 1 a 2`() {
        assertEquals(0.5f, desde("5", "20", "0.5").ancho, 1e-6f)
    }

    @Test
    fun `los topes mantienen la chapa legible en los extremos`() {
        val extrema = desde("0.01", "10000", "0.01")
        assertEquals(ProporcionesChapa.MIN_HORIZONTAL, extrema.ancho)
        assertEquals(1f, extrema.largo)
        assertEquals(ProporcionesChapa.MIN_ESPESOR, extrema.espesor)

        val gruesa = desde("10", "10", "10000")
        assertEquals(ProporcionesChapa.MAX_ESPESOR, gruesa.espesor)
    }

    @Test
    fun `cada proporcion es monotona en su medida`() {
        val fina = desde("10", "20", "0.1")
        val media = desde("10", "20", "0.5")
        val gruesa = desde("10", "20", "5")
        assertTrue(fina.espesor < media.espesor && media.espesor < gruesa.espesor)

        val estrecha = desde("5", "20", "0.5")
        val ancha = desde("15", "20", "0.5")
        assertTrue(estrecha.ancho < ancha.ancho)
    }

    @Test
    fun `sin medidas es la chapa de referencia`() {
        assertEquals(ProporcionesChapa.REFERENCIA, desde(null, null, null))
        assertEquals(ProporcionesChapa.REFERENCIA, desde("10", "20", "0.5"))
        assertEquals(ProporcionesChapa.REFERENCIA, desde(null, "20", null))
    }

    @Test
    fun `el espesor de referencia queda dentro de los topes`() {
        val referencia = ProporcionesChapa.REFERENCIA
        assertTrue(referencia.espesor > ProporcionesChapa.MIN_ESPESOR)
        assertTrue(referencia.espesor < ProporcionesChapa.MAX_ESPESOR)
    }
}
