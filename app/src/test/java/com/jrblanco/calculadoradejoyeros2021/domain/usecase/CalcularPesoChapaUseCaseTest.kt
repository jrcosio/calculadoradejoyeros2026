package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.FamiliaChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata
import com.jrblanco.calculadoradejoyeros2021.domain.model.MaterialChapa
import java.math.BigDecimal
import java.math.RoundingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El motor es puro y determinista: se prueba sin mocks contra los valores del documento
 * técnico `Especificacion_Calculadora_Peso_Chapas_Oro_Plata.md` (§ citados). Todo es exacto
 * (no hay división), así que las comparaciones son `compareTo == 0`.
 */
class CalcularPesoChapaUseCaseTest {

    private val calcular = CalcularPesoChapaUseCase()

    /** La chapa de referencia de §6 y §7: 10 × 20 × 0,5 mm. */
    private fun referencia(material: MaterialChapa): CalculoChapa =
        calcular(BigDecimal("10"), BigDecimal("20"), BigDecimal("0.5"), material)

    @Test
    fun `el caso de la captura - 18K pesa 1,558 g con 1,1685 g de oro fino`() {
        val calculo = referencia(MaterialChapa.ORO_18K)
        assertExacto("200", calculo.areaMm2)
        assertExacto("100", calculo.volumenMm3)
        assertExacto("0.1", calculo.volumenCm3)
        assertExacto("15.58", calculo.densidad)
        assertExacto("1.558", calculo.peso)
        assertExacto("1.1685", calculo.metalFino)
        assertExacto("0.3895", calculo.liga)
        assertEquals(BigDecimal("1.56"), calculo.peso.setScale(2, RoundingMode.HALF_UP))
    }

    @Test
    fun `la tabla de §7 se reproduce para los ocho materiales`() {
        val esperados = mapOf(
            MaterialChapa.ORO_18K to Triple("1.558", "1.1685", "0.3895"),
            MaterialChapa.ORO_14K to Triple("1.307", "0.764595", "0.542405"),
            MaterialChapa.ORO_12K to Triple("1.275", "0.6375", "0.6375"),
            MaterialChapa.ORO_9K to Triple("1.120", "0.4200", "0.7000"),
            MaterialChapa.PLATA_950 to Triple("1.040", "0.9880", "0.0520"),
            MaterialChapa.PLATA_925 to Triple("1.036", "0.9583", "0.0777"),
            MaterialChapa.PLATA_900 to Triple("1.031", "0.9279", "0.1031"),
            MaterialChapa.PLATA_800 to Triple("1.014", "0.8112", "0.2028"),
        )
        esperados.forEach { (material, valores) ->
            val calculo = referencia(material)
            assertExacto(valores.first, calculo.peso, material.name)
            assertExacto(valores.second, calculo.metalFino, material.name)
            assertExacto(valores.third, calculo.liga, material.name)
        }
    }

    @Test
    fun `la columna Mostrar de §7 son los pesos a dos decimales`() {
        val mostrar = mapOf(
            MaterialChapa.ORO_18K to "1.56", MaterialChapa.ORO_14K to "1.31",
            MaterialChapa.ORO_12K to "1.28", MaterialChapa.ORO_9K to "1.12",
            MaterialChapa.PLATA_950 to "1.04", MaterialChapa.PLATA_925 to "1.04",
            MaterialChapa.PLATA_900 to "1.03", MaterialChapa.PLATA_800 to "1.01",
        )
        mostrar.forEach { (material, texto) ->
            assertEquals(material.name, BigDecimal(texto), referencia(material).peso.setScale(2, RoundingMode.HALF_UP))
        }
    }

    @Test
    fun `14K usa 585 milesimas y no 14 entre 24 - §3,1`() {
        val calculo = referencia(MaterialChapa.ORO_14K)
        assertExacto("0.764595", calculo.metalFino)
        val conCatorceVeinticuatroavos = calculo.peso.multiply(BigDecimal("14")).divide(BigDecimal("24"), 10, RoundingMode.HALF_UP)
        assertTrue(calculo.metalFino.compareTo(conCatorceVeinticuatroavos) != 0)
    }

    @Test
    fun `los invariantes se cumplen para todos los tamanos y materiales`() {
        val tamanos = listOf("0.1", "0.5", "10", "123.45", "10000").map(::BigDecimal)
        MaterialChapa.entries.forEach { material ->
            tamanos.forEach { a ->
                tamanos.forEach { l ->
                    val e = BigDecimal("0.5")
                    val calculo = calcular(a, l, e, material)
                    assertExacto(calculo.volumenCm3.multiply(material.densidad).toPlainString(), calculo.peso)
                    assertExacto(calculo.peso.toPlainString(), calculo.metalFino.add(calculo.liga))
                    assertTrue(calculo.metalFino < calculo.peso)
                    assertTrue(calculo.liga.signum() >= 0)
                    // Simetría ancho ↔ largo.
                    assertExacto(calculo.peso.toPlainString(), calcular(l, a, e, material).peso)
                    // La fórmula literal de §4.2, calculada aparte.
                    val literal = a.multiply(l).multiply(e).multiply(material.densidad).divide(BigDecimal("1000"))
                    assertExacto(literal.toPlainString(), calculo.peso)
                }
            }
        }
    }

    @Test
    fun `las medidas a cero o negativas se rechazan`() {
        val cero = BigDecimal.ZERO
        val negativa = BigDecimal("-1")
        val diez = BigDecimal("10")
        listOf(cero, negativa).forEach { mala ->
            assertThrows(IllegalArgumentException::class.java) { calcular(mala, diez, diez, MaterialChapa.ORO_18K) }
            assertThrows(IllegalArgumentException::class.java) { calcular(diez, mala, diez, MaterialChapa.ORO_18K) }
            assertThrows(IllegalArgumentException::class.java) { calcular(diez, diez, mala, MaterialChapa.ORO_18K) }
        }
    }

    @Test
    fun `las densidades son las literales de §5,1`() {
        val densidades = mapOf(
            MaterialChapa.ORO_18K to "15.58", MaterialChapa.ORO_14K to "13.07",
            MaterialChapa.ORO_12K to "12.75", MaterialChapa.ORO_9K to "11.20",
            MaterialChapa.PLATA_950 to "10.40", MaterialChapa.PLATA_925 to "10.36",
            MaterialChapa.PLATA_900 to "10.31", MaterialChapa.PLATA_800 to "10.14",
        )
        densidades.forEach { (material, texto) -> assertEquals(BigDecimal(texto), material.densidad) }
    }

    @Test
    fun `solo 12K, 950 y 900 son leyes tecnicas`() {
        assertEquals(
            setOf(MaterialChapa.ORO_12K, MaterialChapa.PLATA_950, MaterialChapa.PLATA_900),
            MaterialChapa.entries.filter { it.esSoloTecnica }.toSet(),
        )
    }

    @Test
    fun `familias, valores por defecto y finuras`() {
        assertEquals(listOf(MaterialChapa.ORO_18K, MaterialChapa.ORO_14K, MaterialChapa.ORO_12K, MaterialChapa.ORO_9K), MaterialChapa.deFamilia(FamiliaChapa.ORO))
        assertEquals(listOf(MaterialChapa.PLATA_950, MaterialChapa.PLATA_925, MaterialChapa.PLATA_900, MaterialChapa.PLATA_800), MaterialChapa.deFamilia(FamiliaChapa.PLATA))
        assertEquals(MaterialChapa.ORO_18K, MaterialChapa.porDefecto(FamiliaChapa.ORO))
        assertEquals(MaterialChapa.PLATA_925, MaterialChapa.porDefecto(FamiliaChapa.PLATA))
        assertExacto("0.585", MaterialChapa.ORO_14K.finura)
        assertEquals("18k", MaterialChapa.ORO_18K.analyticsId)
        assertEquals("925", MaterialChapa.PLATA_925.analyticsId)
    }

    @Test
    fun `paridad con las leyes de oro y plata del resto de la app`() {
        MaterialChapa.deFamilia(FamiliaChapa.ORO).forEach { material ->
            val ley = LeyOro.entries.single { it.milesimas == material.milesimas }
            assertEquals(material.name, ley.esSoloTecnica, material.esSoloTecnica)
        }
        MaterialChapa.deFamilia(FamiliaChapa.PLATA).forEach { material ->
            val ley = LeyPlata.entries.single { it.milesimas == material.milesimas }
            assertEquals(material.name, ley.esSoloTecnica, material.esSoloTecnica)
        }
    }

    private fun assertExacto(esperado: String, real: BigDecimal, contexto: String = "") {
        assertEquals("$contexto esperado $esperado y salió ${real.toPlainString()}", 0, BigDecimal(esperado).compareTo(real))
    }
}
