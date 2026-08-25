package com.jrblanco.calculadoradejoyeros2021.data.source.local

import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.cotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.error
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.exito
import com.jrblanco.calculadoradejoyeros2021.domain.model.InstantaneaCotizaciones
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MotivoErrorCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.OrigenDatos
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoCotizacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CodificadorInstantaneaTest {

    private val codificador = CodificadorInstantanea()

    private val muestra = InstantaneaCotizaciones(
        resultados = mapOf(
            MetalCotizado.ORO to exito(MetalCotizado.ORO, obtenidoEn = 1_000),
            MetalCotizado.PLATA to exito(MetalCotizado.PLATA, obtenidoEn = 1_000),
            MetalCotizado.COBRE to ResultadoCotizacion.Exito(
                cotizacion(metal = MetalCotizado.COBRE, unidadOrigen = null, etiquetaUnidadOrigen = "LB", obtenidoEn = 1_000),
            ),
            MetalCotizado.PALADIO to exito(MetalCotizado.PALADIO, obtenidoEn = 1_000),
            MetalCotizado.RODIO to error(
                MetalCotizado.RODIO,
                MotivoErrorCotizacion.SIN_CONEXION,
                ultimaConocida = cotizacion(metal = MetalCotizado.RODIO, obtenidoEn = 500),
            ),
        ),
        instanteIntentoEpochMillis = 2_000,
        origen = OrigenDatos.RED,
    )

    @Test
    fun `ida y vuelta conserva importes exactos, instantes y unidades`() {
        val texto = codificador.codificar(muestra)
        val leida = codificador.decodificar(texto)!!

        assertEquals(2_000L, leida.instanteIntentoEpochMillis)
        assertEquals(muestra.resultados.keys, leida.resultados.keys)
        val oro = leida.resultados[MetalCotizado.ORO] as ResultadoCotizacion.Exito
        assertEquals(0, cotizacion().variacion.compareTo(oro.cotizacion.variacion))
        assertEquals(1_000L, oro.cotizacion.obtenidoEnEpochMillis)
        val cobre = leida.resultados[MetalCotizado.COBRE] as ResultadoCotizacion.Exito
        assertNull(cobre.cotizacion.unidadOrigen)
        assertEquals("LB", cobre.cotizacion.etiquetaUnidadOrigen)
        val rodio = leida.resultados[MetalCotizado.RODIO] as ResultadoCotizacion.Error
        assertEquals(MotivoErrorCotizacion.SIN_CONEXION, rodio.motivo)
        assertEquals(500L, rodio.ultimaConocida?.obtenidoEnEpochMillis)
        assertNull(rodio.causa)
    }

    @Test
    fun `el origen no se persiste`() {
        assertEquals(OrigenDatos.CACHE, codificador.decodificar(codificador.codificar(muestra))!!.origen)
    }

    @Test
    fun `el json lleva version y los importes como texto`() {
        val texto = codificador.codificar(muestra)
        assertTrue(texto.contains("\"version\":1"))
        assertTrue(texto.contains("\"variacion\":\"-45.30000000000018\""))
    }

    @Test
    fun `un metal o un motivo desconocidos se descartan sin tumbar la lectura`() {
        val texto = codificador.codificar(muestra)
            .replace("\"metal\":\"PLATA\"", "\"metal\":\"PLATINO\"")
            .replace("\"motivoError\":\"SIN_CONEXION\"", "\"motivoError\":\"MARCIANOS\"")
        val leida = codificador.decodificar(texto)!!
        assertEquals(setOf(MetalCotizado.ORO, MetalCotizado.COBRE, MetalCotizado.PALADIO), leida.resultados.keys)
    }

    @Test
    fun `un json corrupto devuelve null`() {
        assertNull(codificador.decodificar("{\"version\":1,\"resultados\":["))
        assertNull(codificador.decodificar("basura"))
    }

    @Test
    fun `la instantanea vacia tambien viaja`() {
        val leida = codificador.decodificar(codificador.codificar(InstantaneaCotizaciones.VACIA))!!
        assertEquals(InstantaneaCotizaciones.VACIA, leida)
    }
}
