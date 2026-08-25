package com.jrblanco.calculadoradejoyeros2021.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeleccionIdiomaTest {

    @Test
    fun `sin eleccion manda el dispositivo`() {
        val seleccion = SeleccionIdioma(elegido = null, sistema = IdiomaApp.FRANCES)

        assertEquals(IdiomaApp.FRANCES, seleccion.efectivo)
        assertTrue(seleccion.esAutomatico)
    }

    @Test
    fun `sin eleccion y con el dispositivo en un idioma no soportado, el sistema ya llega en espanol`() {
        // Quien reduce el idioma del dispositivo a los soportados es IdiomaSistema; aquí solo se
        // comprueba que la regla no lo vuelve a tocar.
        val seleccion = SeleccionIdioma(elegido = null, sistema = IdiomaApp.PREDETERMINADO)

        assertEquals(IdiomaApp.ESPANOL, seleccion.efectivo)
        assertTrue(seleccion.esAutomatico)
    }

    @Test
    fun `la eleccion del joyero prevalece sobre el dispositivo`() {
        val seleccion = SeleccionIdioma(elegido = IdiomaApp.ALEMAN, sistema = IdiomaApp.INGLES)

        assertEquals(IdiomaApp.ALEMAN, seleccion.efectivo)
        assertFalse(seleccion.esAutomatico)
    }

    @Test
    fun `elegir el mismo idioma que el dispositivo no es automatico`() {
        val seleccion = SeleccionIdioma(elegido = IdiomaApp.ALEMAN, sistema = IdiomaApp.ALEMAN)

        assertEquals(IdiomaApp.ALEMAN, seleccion.efectivo)
        assertFalse(seleccion.esAutomatico)
    }
}
