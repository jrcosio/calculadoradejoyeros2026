package com.jrblanco.calculadoradejoyeros2021.recursos

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Paridad de los cinco `strings.xml`, en la tradición del test que vigila que las milésimas y la
 * bandera técnica de los materiales de chapa coincidan: ~980 cadenas no se mantienen a mano sin
 * una red que avise.
 *
 * Es la red rápida. La segunda es `./gradlew :app:lint` (`MissingTranslation`, `ExtraTranslation`),
 * que conoce el sistema de recursos de Android mejor que este test pero no mira los marcadores de
 * formato ni los saltos de línea, y tarda mucho más.
 *
 * Reglas comprobadas, las del contrato `specs/008-ajustes-idioma/contracts/traducciones.md`.
 */
class TraduccionesTest {

    private val idiomas = listOf("en", "fr", "de", "it")

    private val base = leer(ficheroDeRecursos("values"))
    private val traducciones = idiomas.associateWith { leer(ficheroDeRecursos("values-$it")) }

    private val traducibles = base.filterValues { !it.noTraducible }
    private val noTraducibles = base.filterValues { it.noTraducible }

    @Test
    fun `el fichero base tiene cadenas de las dos clases`() {
        // Si esto falla, el parser está roto y el resto del test no prueba nada.
        assertTrue("El fichero base no se ha leído", base.size > 200)
        assertTrue("No hay cadenas traducibles", traducibles.size > 150)
        assertTrue("Nadie ha marcado nada como no traducible", noTraducibles.size > 20)
    }

    @Test
    fun `existen los cuatro idiomas traducidos`() {
        idiomas.forEach { idioma ->
            assertTrue(
                "Falta app/src/main/res/values-$idioma/strings.xml",
                ficheroDeRecursos("values-$idioma").exists(),
            )
        }
    }

    @Test
    fun `no falta ninguna cadena traducible en ningun idioma`() {
        val faltan = traducciones.flatMap { (idioma, cadenas) ->
            traducibles.keys.filterNot { it in cadenas }.map { "$idioma: falta $it" }
        }
        assertEquals("Cadenas sin traducir", emptyList<String>(), faltan.sorted())
    }

    @Test
    fun `ninguna traduccion declara cadenas que no existan en el fichero base`() {
        val sobran = traducciones.flatMap { (idioma, cadenas) ->
            cadenas.keys.filterNot { it in base }.map { "$idioma: sobra $it" }
        }
        assertEquals("Cadenas huérfanas", emptyList<String>(), sobran.sorted())
    }

    @Test
    fun `lo marcado como no traducible no se traduce`() {
        val traducidas = traducciones.flatMap { (idioma, cadenas) ->
            noTraducibles.keys.filter { it in cadenas }.map { "$idioma: $it no debería traducirse" }
        }
        assertEquals("Cadenas no traducibles presentes en un values-xx", emptyList<String>(), traducidas.sorted())
    }

    @Test
    fun `los marcadores de formato se conservan, aunque se reordenen`() {
        val fallos = traducciones.flatMap { (idioma, cadenas) ->
            traducibles.mapNotNull { (clave, cadenaBase) ->
                val traducida = cadenas[clave] ?: return@mapNotNull null
                val esperados = marcadores(cadenaBase.texto)
                val encontrados = marcadores(traducida.texto)
                if (esperados == encontrados) {
                    null
                } else {
                    "$idioma: $clave espera $esperados y tiene $encontrados"
                }
            }
        }
        assertEquals("Marcadores de formato distintos", emptyList<String>(), fallos.sorted())
    }

    @Test
    fun `el porcentaje literal y los saltos de linea se conservan`() {
        val fallos = traducciones.flatMap { (idioma, cadenas) ->
            traducibles.mapNotNull { (clave, cadenaBase) ->
                val traducida = cadenas[clave] ?: return@mapNotNull null
                val porcentajes = cadenaBase.texto.split("%%").size
                val saltos = cadenaBase.texto.split("\\n").size
                when {
                    traducida.texto.split("%%").size != porcentajes -> "$idioma: $clave pierde un %%"
                    traducida.texto.split("\\n").size != saltos -> "$idioma: $clave pierde un salto de línea"
                    else -> null
                }
            }
        }
        assertEquals("Escapes perdidos", emptyList<String>(), fallos.sorted())
    }

    @Test
    fun `ninguna traduccion se ha quedado con el texto en espanol`() {
        // No es una regla del contrato, es una red contra el copiar y pegar: si una cadena larga
        // es idéntica al español en los cuatro idiomas, es que nadie la tradujo. Las cortas
        // (nombres de metal, «Normal», «Media») pueden coincidir legítimamente.
        val sospechosas = traducibles.filter { (clave, cadenaBase) ->
            cadenaBase.texto.length > 40 &&
                idiomas.all { traducciones.getValue(it)[clave]?.texto == cadenaBase.texto }
        }.keys
        assertEquals("Cadenas idénticas al español en los cuatro idiomas", emptySet<String>(), sospechosas)
    }

    // --- Lectura de los ficheros de recursos ---

    private data class Cadena(val texto: String, val noTraducible: Boolean)

    private fun ficheroDeRecursos(carpeta: String): File {
        // Los tests unitarios corren con el directorio del módulo (`app/`) como raíz, pero no
        // conviene depender solo de eso: se prueban las dos rutas posibles.
        val candidatos = listOf(
            File("src/main/res/$carpeta/strings.xml"),
            File("app/src/main/res/$carpeta/strings.xml"),
        )
        return candidatos.firstOrNull { it.exists() } ?: candidatos.first()
    }

    private fun leer(fichero: File): Map<String, Cadena> {
        if (!fichero.exists()) return emptyMap()
        val texto = fichero.readText()
        val patron = Regex("""<string name="([^"]+)"([^>]*)>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
        return patron.findAll(texto).associate { coincidencia ->
            val (clave, atributos, contenido) = coincidencia.destructured
            clave to Cadena(texto = contenido, noTraducible = atributos.contains("""translatable="false""""))
        }
    }

    /** Los `%1$s`, `%2$s`… de una cadena, como conjunto: el orden puede cambiar al traducir. */
    private fun marcadores(texto: String): Set<String> =
        Regex("""%\d+\$[a-zA-Z]""").findAll(texto).map { it.value }.toSet()
}
