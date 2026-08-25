package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CotizacionMetalTest {

    @Test
    fun `el precio principal es el medio cuando existe`() {
        assertEquals(BigDecimal("4606.4"), CotizacionesDePrueba.cotizacion().precioPrincipal)
    }

    @Test
    fun `sin medio se usa la venta y despues la compra`() {
        assertEquals(BigDecimal("4607.4"), CotizacionesDePrueba.cotizacion(mid = "0").precioPrincipal)
        assertEquals(BigDecimal("4605.4"), CotizacionesDePrueba.cotizacion(mid = "0", ask = "0").precioPrincipal)
    }

    @Test
    fun `sin ninguno de los tres no hay precio principal`() {
        assertNull(CotizacionesDePrueba.cotizacion(mid = "0", ask = "0", bid = "0").precioPrincipal)
    }

    @Test
    fun `la tendencia sigue el signo de la variacion`() {
        assertEquals(Tendencia.BAJA, CotizacionesDePrueba.cotizacion().tendencia)
        assertEquals(Tendencia.SUBE, CotizacionesDePrueba.cotizacion(variacion = "0.01").tendencia)
        assertEquals(Tendencia.PLANA, CotizacionesDePrueba.cotizacion(variacion = "0").tendencia)
        assertEquals(Tendencia.PLANA, Tendencia.de(BigDecimal("0.000")))
    }
}
