package com.jrblanco.calculadoradejoyeros2021.data.source.local

import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.FavoritosDePrueba
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata
import com.jrblanco.calculadoradejoyeros2021.domain.model.MaterialChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.ModoEntradaSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraClasica
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraPlata
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Kotlin puro, así que se prueba en JVM. Lo que este test protege es la **identidad** de un
 * favorito: si la firma cambia de forma sin querer, el deduplicado deja de funcionar en silencio y
 * el joyero empieza a ver entradas repetidas. Por eso las siete firmas van literales.
 */
class CodificadorFavoritoTest {

    private val codificador = CodificadorFavorito()

    // --- Las siete firmas doradas (contracts/favoritos-persistidos.md) ---

    @Test
    fun `las siete firmas tienen exactamente la forma del contrato`() {
        val esperadas = mapOf(
            EntradasFavorito.Oro(BigDecimal("30"), ColorOro.AMARILLO, LeyOro.LEY_18K) to
                "oro|v1|masa=30|color=AMARILLO|ley=LEY_18K",
            EntradasFavorito.Plata(BigDecimal("100"), LeyPlata.LEY_925) to
                "plata|v1|masa=100|ley=LEY_925",
            EntradasFavorito.SoldaduraLey(
                BigDecimal("2"), DurezaSoldaduraLey.MUY_FLOJA, ColorOroSoldadura.AMARILLO,
                ModoEntradaSoldadura.DESDE_METAL,
            ) to "soldadura_ley|v1|cant=2|dureza=MUY_FLOJA|color=AMARILLO|modo=DESDE_METAL",
            EntradasFavorito.SoldaduraClasica(
                BigDecimal("5"), TipoSoldaduraClasica.FLOJA, ModoEntradaSoldadura.PESO_FINAL,
            ) to "soldadura_clasica|v1|cant=5|tipo=FLOJA|modo=PESO_FINAL",
            EntradasFavorito.SoldaduraPlata(
                BigDecimal("10"), TipoSoldaduraPlata.NORMAL, ModoEntradaSoldadura.DESDE_METAL,
            ) to "soldadura_plata|v1|cant=10|tipo=NORMAL|modo=DESDE_METAL",
            EntradasFavorito.SoldaduraBase(BigDecimal("10"), ModoEntradaSoldadura.DESDE_METAL) to
                "soldadura_base|v1|cant=10|modo=DESDE_METAL",
            EntradasFavorito.Chapa(
                BigDecimal("10"), BigDecimal("30"), BigDecimal("2"), MaterialChapa.ORO_18K,
            ) to "chapa|v1|ancho=10|largo=30|espesor=2|material=ORO_18K",
        )

        esperadas.forEach { (entradas, firma) ->
            assertEquals(firma, codificador.codificar(entradas).firma)
        }
    }

    @Test
    fun `el tipo de cada firma es el discriminador de la columna`() {
        FavoritosDePrueba.todas().forEach { entradas ->
            val persistido = codificador.codificar(entradas)
            assertEquals(entradas.analyticsId, persistido.tipo)
            assertTrue(persistido.firma.startsWith("${entradas.analyticsId}|v1|"))
        }
    }

    // --- Regla 2: la escala no cambia la identidad, aunque sí el `equals` ---

    @Test
    fun `treinta y treinta coma cero son el mismo favorito`() {
        val entero = FavoritosDePrueba.oro(masaOrigen = "30")
        val conDecimales = FavoritosDePrueba.oro(masaOrigen = "30.000")

        assertEquals(codificador.codificar(entero).firma, codificador.codificar(conDecimales).firma)
        // Y este es el motivo de que la identidad no pueda ser `equals`:
        assertNotEquals(entero, conDecimales)
    }

    @Test
    fun `las formas equivalentes de un decimal dan una sola firma`() {
        val firmas = listOf("30", "30.0", "030", "+30", "3e1")
            .map { codificador.codificar(FavoritosDePrueba.oro(masaOrigen = it)).firma }
            .toSet()

        assertEquals(setOf("oro|v1|masa=30|color=BLANCO|ley=LEY_18K"), firmas)
    }

    @Test
    fun `los ceros a la derecha de un decimal no entero se recortan sin perder valor`() {
        assertEquals(
            "chapa|v1|ancho=10|largo=20|espesor=0.5|material=ORO_18K",
            codificador.codificar(FavoritosDePrueba.chapa(espesor = "0.500")).firma,
        )
    }

    // --- Cambiar cualquier entrada cambia la firma ---

    @Test
    fun `cambiar el color o la ley cambia la firma`() {
        val base = codificador.codificar(FavoritosDePrueba.oro()).firma

        assertNotEquals(base, codificador.codificar(FavoritosDePrueba.oro(color = ColorOro.ROSA)).firma)
        assertNotEquals(base, codificador.codificar(FavoritosDePrueba.oro(ley = LeyOro.LEY_9K)).firma)
        assertNotEquals(base, codificador.codificar(FavoritosDePrueba.oro(masaOrigen = "31")).firma)
    }

    @Test
    fun `el modo entra en la identidad - diez gramos de metal no es diez de peso final`() {
        val directo = codificador.codificar(
            FavoritosDePrueba.soldaduraClasica(modo = ModoEntradaSoldadura.DESDE_METAL),
        ).firma
        val inverso = codificador.codificar(
            FavoritosDePrueba.soldaduraClasica(modo = ModoEntradaSoldadura.PESO_FINAL),
        ).firma

        assertNotEquals(directo, inverso)
    }

    @Test
    fun `una chapa girada no es la misma chapa aunque pese lo mismo`() {
        val tumbada = codificador.codificar(
            FavoritosDePrueba.chapa(ancho = "10", largo = "30", espesor = "2"),
        ).firma
        val girada = codificador.codificar(
            FavoritosDePrueba.chapa(ancho = "30", largo = "10", espesor = "2"),
        ).firma

        assertNotEquals("Cada medida es un campo con nombre propio", tumbada, girada)
    }

    // --- Ida y vuelta ---

    @Test
    fun `las siete variantes sobreviven a la ida y vuelta`() {
        FavoritosDePrueba.todas().forEach { entradas ->
            val persistido = codificador.codificar(entradas)
            val recuperadas = codificador.decodificar(persistido.tipo, persistido.datosJson)

            assertEquals("Falló ${entradas.analyticsId}", entradas, recuperadas)
        }
    }

    @Test
    fun `los dos modos de las cuatro soldaduras sobreviven a la ida y vuelta`() {
        ModoEntradaSoldadura.entries.forEach { modo ->
            listOf(
                FavoritosDePrueba.soldaduraLey(modo = modo),
                FavoritosDePrueba.soldaduraClasica(modo = modo),
                FavoritosDePrueba.soldaduraPlata(modo = modo),
                FavoritosDePrueba.soldaduraBase(modo = modo),
            ).forEach { entradas ->
                val p = codificador.codificar(entradas)
                assertEquals(entradas, codificador.decodificar(p.tipo, p.datosJson))
            }
        }
    }

    @Test
    fun `el json lleva siempre su version`() {
        assertTrue(codificador.codificar(FavoritosDePrueba.oro()).datosJson.contains("\"version\":1"))
    }

    // --- Tolerancia: lo que no se entiende devuelve null, y el llamante no lo borra ---

    @Test
    fun `un tipo desconocido no se entiende`() {
        val json = codificador.codificar(FavoritosDePrueba.oro()).datosJson

        assertNull(codificador.decodificar("platino", json))
    }

    @Test
    fun `un valor de enum desconocido no se entiende`() {
        assertNull(
            codificador.decodificar(
                CodificadorFavorito.TIPO_ORO,
                """{"version":1,"cantidad":"30","color":"BLANCO","ley":"LEY_22K"}""",
            ),
        )
    }

    @Test
    fun `una cantidad que no sirve no se entiende`() {
        listOf("0", "-5", "abc", "").forEach { cantidad ->
            assertNull(
                "Debería descartar la cantidad «$cantidad»",
                codificador.decodificar(
                    CodificadorFavorito.TIPO_ORO,
                    """{"version":1,"cantidad":"$cantidad","color":"BLANCO","ley":"LEY_18K"}""",
                ),
            )
        }
    }

    @Test
    fun `un campo que falta no se entiende`() {
        assertNull(
            codificador.decodificar(
                CodificadorFavorito.TIPO_ORO,
                """{"version":1,"cantidad":"30","ley":"LEY_18K"}""",
            ),
        )
    }

    @Test
    fun `un json roto no se entiende`() {
        listOf("", "{", "no soy json", """{"version":1,"cantidad":}""").forEach { texto ->
            assertNull("Debería descartar «$texto»", codificador.decodificar(CodificadorFavorito.TIPO_ORO, texto))
        }
    }

    @Test
    fun `las claves de mas se ignoran y la fila se lee`() {
        val entradas = codificador.decodificar(
            CodificadorFavorito.TIPO_ORO,
            """{"version":2,"cantidad":"30","color":"BLANCO","ley":"LEY_18K","acabado":"MATE"}""",
        )

        assertEquals(FavoritosDePrueba.oro(masaOrigen = "30"), entradas)
    }
}
