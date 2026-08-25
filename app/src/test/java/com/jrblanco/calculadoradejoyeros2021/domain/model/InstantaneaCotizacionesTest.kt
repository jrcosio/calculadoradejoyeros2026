package com.jrblanco.calculadoradejoyeros2021.domain.model

import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.cotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.error
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.exito
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.instantaneaCompleta
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.instantaneaParcial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InstantaneaCotizacionesTest {

    private val hora = 3_600_000L

    @Test
    fun `la vacia no esta completa ni tiene errores ni instante`() {
        val vacia = InstantaneaCotizaciones.VACIA
        assertFalse(vacia.estaCompleta)
        assertFalse(vacia.hayErrores)
        assertNull(vacia.instanteMasRecienteEpochMillis)
        assertNull(vacia.instanteIntentoEpochMillis)
    }

    @Test
    fun `completa con cinco exitos, parcial con un error`() {
        assertTrue(instantaneaCompleta(obtenidoEn = 1_000).estaCompleta)
        val parcial = instantaneaParcial(obtenidoEn = 1_000)
        assertFalse(parcial.estaCompleta)
        assertTrue(parcial.hayErrores)
        assertFalse(parcial.hayErrorPorLimite)
        assertTrue(instantaneaParcial(1_000, motivo = MotivoErrorCotizacion.LIMITE_ALCANZADO).hayErrorPorLimite)
    }

    @Test
    fun `vigente dentro de la hora, caducada al cumplirla`() {
        val guardada = instantaneaCompleta(obtenidoEn = 10_000)
        assertTrue(guardada.esVigente(MetalCotizado.ORO, ahoraMillis = 10_000, vigenciaMillis = hora))
        assertTrue(guardada.esVigente(MetalCotizado.ORO, ahoraMillis = 10_000 + hora - 1, vigenciaMillis = hora))
        assertFalse(guardada.esVigente(MetalCotizado.ORO, ahoraMillis = 10_000 + hora, vigenciaMillis = hora))
    }

    @Test
    fun `un dato del futuro no es vigente - reloj atrasado`() {
        val guardada = instantaneaCompleta(obtenidoEn = 10_000)
        assertFalse(guardada.esVigente(MetalCotizado.ORO, ahoraMillis = 9_999, vigenciaMillis = hora))
    }

    @Test
    fun `un error o un metal ausente nunca son vigentes`() {
        val parcial = instantaneaParcial(obtenidoEn = 10_000)
        assertFalse(parcial.esVigente(MetalCotizado.RODIO, 10_000, hora))
        assertFalse(InstantaneaCotizaciones.VACIA.esVigente(MetalCotizado.ORO, 10_000, hora))
    }

    @Test
    fun `fusionar sustituye exitos y hereda la ultima conocida en los errores`() {
        val anterior = instantaneaCompleta(obtenidoEn = 1_000)
        val fusionada = anterior.fusionarCon(
            nuevos = mapOf(
                MetalCotizado.ORO to exito(MetalCotizado.ORO, obtenidoEn = 5_000),
                MetalCotizado.RODIO to error(MetalCotizado.RODIO),
            ),
            instanteIntentoEpochMillis = 5_000,
        )
        val oro = fusionada.resultados[MetalCotizado.ORO] as ResultadoCotizacion.Exito
        assertEquals(5_000L, oro.cotizacion.obtenidoEnEpochMillis)
        val rodio = fusionada.resultados[MetalCotizado.RODIO] as ResultadoCotizacion.Error
        assertEquals(1_000L, rodio.ultimaConocida?.obtenidoEnEpochMillis)
        assertEquals(5_000L, fusionada.instanteIntentoEpochMillis)
        // Los que no se consultaron siguen como estaban.
        assertEquals(1_000L, (fusionada.resultados[MetalCotizado.PLATA] as ResultadoCotizacion.Exito).cotizacion.obtenidoEnEpochMillis)
    }

    @Test
    fun `un error con ultima conocida propia no la pierde al fusionar`() {
        val anterior = instantaneaCompleta(obtenidoEn = 1_000)
        val propia = cotizacion(metal = MetalCotizado.RODIO, obtenidoEn = 3_000)
        val fusionada = anterior.fusionarCon(
            mapOf(MetalCotizado.RODIO to error(MetalCotizado.RODIO, ultimaConocida = propia)),
            instanteIntentoEpochMillis = 5_000,
        )
        assertEquals(propia, (fusionada.resultados[MetalCotizado.RODIO] as ResultadoCotizacion.Error).ultimaConocida)
    }

    @Test
    fun `la ultima cotizacion conocida sale del exito o del error`() {
        val parcial = instantaneaParcial(obtenidoEn = 1_000)
        assertEquals(1_000L, parcial.ultimaCotizacionConocida(MetalCotizado.ORO)?.obtenidoEnEpochMillis)
        assertNull(parcial.ultimaCotizacionConocida(MetalCotizado.RODIO))
        assertNull(InstantaneaCotizaciones.VACIA.ultimaCotizacionConocida(MetalCotizado.ORO))
    }

    @Test
    fun `el instante mas reciente es el mayor obtenidoEn de los exitos`() {
        val mezcla = InstantaneaCotizaciones(
            resultados = mapOf(
                MetalCotizado.ORO to exito(MetalCotizado.ORO, obtenidoEn = 1_000),
                MetalCotizado.PLATA to exito(MetalCotizado.PLATA, obtenidoEn = 9_000),
                MetalCotizado.RODIO to error(MetalCotizado.RODIO),
            ),
        )
        assertEquals(9_000L, mezcla.instanteMasRecienteEpochMillis)
    }
}
