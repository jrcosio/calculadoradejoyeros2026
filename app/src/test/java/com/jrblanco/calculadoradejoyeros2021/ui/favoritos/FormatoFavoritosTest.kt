package com.jrblanco.calculadoradejoyeros2021.ui.favoritos

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Las cuatro políticas de redondeo de la pantalla de Favoritos, en sus valores frontera. Lo que se
 * fija aquí es que **no** son la misma política: la diferencia entre oro y plata es una milésima y
 * una ley por debajo de la objetivo.
 */
class FormatoFavoritosTest {

    @Test
    fun `la media redondea hacia arriba en el cinco`() {
        assertEquals("5,158", FormatoFavoritos.gramosMedia(BigDecimal("5.1575")))
        assertEquals("1,558", FormatoFavoritos.gramosMedia(BigDecimal("1.5578")))
    }

    @Test
    fun `plata trunca y por eso da una milesima menos`() {
        assertEquals("5,157", FormatoFavoritos.gramosPlata(BigDecimal("5.1575")))
        assertEquals(
            "El caso que la Ley 17/1985 obliga a truncar",
            "5,157",
            FormatoFavoritos.gramosPlata(BigDecimal("5.157894736842105")),
        )
    }

    @Test
    fun `la media y el truncado no coinciden - son politicas distintas a proposito`() {
        val valor = BigDecimal("5.157894736842105")

        assertEquals("5,158", FormatoFavoritos.gramosMedia(valor))
        assertEquals("5,157", FormatoFavoritos.gramosPlata(valor))
    }

    @Test
    fun `el peso de una chapa va a dos decimales`() {
        assertEquals("1,56", FormatoFavoritos.pesoChapa(BigDecimal("1.558")))
        assertEquals("0,10", FormatoFavoritos.pesoChapa(BigDecimal("0.0999")))
    }

    @Test
    fun `volumen y metal fino van a tres`() {
        assertEquals("0,100", FormatoFavoritos.tresDecimales(BigDecimal("0.1")))
        assertEquals("1,169", FormatoFavoritos.tresDecimales(BigDecimal("1.16850")))
    }

    @Test
    fun `la cantidad de entrada es un eco de lo teclado y no impone escala`() {
        assertEquals("30", FormatoFavoritos.cantidadEntrada(BigDecimal("30")))
        assertEquals("30", FormatoFavoritos.cantidadEntrada(BigDecimal("30.000")))
        assertEquals("30,5", FormatoFavoritos.cantidadEntrada(BigDecimal("30.50")))
        assertEquals("0,5", FormatoFavoritos.cantidadEntrada(BigDecimal("0.500")))
    }

    @Test
    fun `todas las cifras salen con coma decimal`() {
        listOf(
            FormatoFavoritos.gramosMedia(BigDecimal("1.5")),
            FormatoFavoritos.gramosPlata(BigDecimal("1.5")),
            FormatoFavoritos.pesoChapa(BigDecimal("1.5")),
            FormatoFavoritos.tresDecimales(BigDecimal("1.5")),
            FormatoFavoritos.cantidadEntrada(BigDecimal("1.5")),
        ).forEach { texto ->
            assertEquals("Sin puntos decimales: $texto", -1, texto.indexOf('.'))
        }
    }
}
