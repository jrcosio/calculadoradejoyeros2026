package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.ConversorUnidadesPrecio
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.cotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.UnidadPrecio
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class ConvertirCotizacionUseCaseTest {

    private val convertir = ConvertirCotizacionUseCase()

    @Test
    fun `convierte todos los importes lineales y fija la unidad`() {
        val origen = cotizacion()
        val gramo = convertir(origen, UnidadPrecio.GRAMO)!!
        listOf(
            origen.ask to gramo.ask,
            origen.bid to gramo.bid,
            origen.mid to gramo.mid,
            origen.maximo to gramo.maximo,
            origen.minimo to gramo.minimo,
            origen.variacion to gramo.variacion,
        ).forEach { (antes, despues) ->
            assertEquals(
                0,
                ConversorUnidadesPrecio.convertir(antes, UnidadPrecio.ONZA_TROY, UnidadPrecio.GRAMO).compareTo(despues),
            )
        }
        assertEquals(UnidadPrecio.GRAMO, gramo.unidadOrigen)
        assertEquals(BigDecimal("148.10"), gramo.mid.setScale(2, java.math.RoundingMode.HALF_UP))
    }

    @Test
    fun `el porcentaje y el resto de campos no cambian`() {
        val origen = cotizacion()
        val kilo = convertir(origen, UnidadPrecio.KILO)!!
        assertEquals(origen.variacionPorcentaje, kilo.variacionPorcentaje)
        assertEquals(origen.etiquetaUnidadOrigen, kilo.etiquetaUnidadOrigen)
        assertEquals(origen.instanteMercadoEpochMillis, kilo.instanteMercadoEpochMillis)
        assertEquals(origen.obtenidoEnEpochMillis, kilo.obtenidoEnEpochMillis)
        assertEquals(origen.metal, kilo.metal)
    }

    @Test
    fun `sin unidad de origen confirmada no se convierte`() {
        assertNull(convertir(cotizacion(unidadOrigen = null, etiquetaUnidadOrigen = "LB"), UnidadPrecio.GRAMO))
    }

    @Test
    fun `a la misma unidad devuelve la misma cotizacion`() {
        val origen = cotizacion()
        assertSame(origen, convertir(origen, UnidadPrecio.ONZA_TROY))
    }
}
