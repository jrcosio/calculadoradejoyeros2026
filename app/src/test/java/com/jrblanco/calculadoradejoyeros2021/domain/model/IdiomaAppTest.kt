package com.jrblanco.calculadoradejoyeros2021.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IdiomaAppTest {

    @Test
    fun `la etiqueta simple resuelve a su idioma`() {
        assertEquals(IdiomaApp.ESPANOL, IdiomaApp.desdeEtiqueta("es"))
        assertEquals(IdiomaApp.INGLES, IdiomaApp.desdeEtiqueta("en"))
        assertEquals(IdiomaApp.FRANCES, IdiomaApp.desdeEtiqueta("fr"))
        assertEquals(IdiomaApp.ALEMAN, IdiomaApp.desdeEtiqueta("de"))
        assertEquals(IdiomaApp.ITALIANO, IdiomaApp.desdeEtiqueta("it"))
    }

    @Test
    fun `la region se ignora, con guion o con subrayado`() {
        assertEquals(IdiomaApp.ESPANOL, IdiomaApp.desdeEtiqueta("es-ES"))
        assertEquals(IdiomaApp.ESPANOL, IdiomaApp.desdeEtiqueta("es_MX"))
        assertEquals(IdiomaApp.ESPANOL, IdiomaApp.desdeEtiqueta("es-419"))
        assertEquals(IdiomaApp.INGLES, IdiomaApp.desdeEtiqueta("en-GB"))
        assertEquals(IdiomaApp.INGLES, IdiomaApp.desdeEtiqueta("en_US"))
        assertEquals(IdiomaApp.ALEMAN, IdiomaApp.desdeEtiqueta("de-AT"))
    }

    @Test
    fun `las mayusculas y los espacios no importan`() {
        assertEquals(IdiomaApp.ESPANOL, IdiomaApp.desdeEtiqueta("ES"))
        assertEquals(IdiomaApp.FRANCES, IdiomaApp.desdeEtiqueta("Fr"))
        assertEquals(IdiomaApp.ITALIANO, IdiomaApp.desdeEtiqueta("  it  "))
    }

    @Test
    fun `un idioma no soportado no es un idioma de la app`() {
        assertNull(IdiomaApp.desdeEtiqueta("pt"))
        assertNull(IdiomaApp.desdeEtiqueta("pt-BR"))
        assertNull(IdiomaApp.desdeEtiqueta("xx"))
        // No se confunde con un idioma que empiece igual: solo vale el subtag completo.
        assertNull(IdiomaApp.desdeEtiqueta("esp"))
    }

    @Test
    fun `la ausencia de etiqueta no es un idioma`() {
        assertNull(IdiomaApp.desdeEtiqueta(null))
        assertNull(IdiomaApp.desdeEtiqueta(""))
        assertNull(IdiomaApp.desdeEtiqueta("   "))
        assertNull(IdiomaApp.desdeEtiqueta("-ES"))
    }

    @Test
    fun `la etiqueta es tambien el sufijo de la carpeta de recursos y el id de telemetria`() {
        assertEquals(listOf("es", "en", "fr", "de", "it"), IdiomaApp.entries.map { it.etiquetaBcp47 })
        assertEquals(IdiomaApp.entries.map { it.etiquetaBcp47 }, IdiomaApp.entries.map { it.analyticsId })
    }

    @Test
    fun `el orden del enum es el de la lista de Ajustes y el espanol es el predeterminado`() {
        assertEquals(IdiomaApp.ESPANOL, IdiomaApp.entries.first())
        assertEquals(IdiomaApp.ESPANOL, IdiomaApp.PREDETERMINADO)
        assertEquals(5, IdiomaApp.entries.size)
    }

    @Test
    fun `toda etiqueta del enum se resuelve a si misma`() {
        IdiomaApp.entries.forEach { idioma ->
            assertEquals(idioma, IdiomaApp.desdeEtiqueta(idioma.etiquetaBcp47))
        }
    }
}
