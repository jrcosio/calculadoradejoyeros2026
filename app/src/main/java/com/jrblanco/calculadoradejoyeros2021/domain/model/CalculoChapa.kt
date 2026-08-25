package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/**
 * Resultado del peso de una chapa rectangular maciza (§4, §8.1, §8.2).
 *
 * `peso = ancho × largo × espesor × densidad / 1000`. **Sin ningún redondeo** (§10.1): solo
 * hay multiplicaciones exactas y un desplazamiento de coma exacto para pasar de mm³ a cm³.
 * No hay `ESCALA` porque no hay división; llegará con los cálculos inversos (§8.3), fuera de
 * alcance en esta versión. [MM3_POR_CM3] es constante propia del motor, como `FINURA_ORIGEN`
 * en los de oro y plata: cada motor es fiel a su documento.
 *
 * Las medidas van en milímetros; el volumen se conserva en las dos unidades para la vista.
 */
data class CalculoChapa(
    val material: MaterialChapa,
    val ancho: BigDecimal,
    val largo: BigDecimal,
    val espesor: BigDecimal,
    val areaMm2: BigDecimal,
    val volumenMm3: BigDecimal,
    val volumenCm3: BigDecimal,
    val densidad: BigDecimal,
    val peso: BigDecimal,
    val metalFino: BigDecimal,
    val liga: BigDecimal,
) {
    companion object {
        /** 1 cm³ = 1 000 mm³ (§4). */
        val MM3_POR_CM3: BigDecimal = BigDecimal("1000")

        internal fun de(
            ancho: BigDecimal,
            largo: BigDecimal,
            espesor: BigDecimal,
            material: MaterialChapa,
        ): CalculoChapa {
            val areaMm2 = ancho.multiply(largo)
            val volumenMm3 = areaMm2.multiply(espesor)
            // ÷ MM3_POR_CM3 sin dividir: mover la coma tres posiciones es exacto.
            val volumenCm3 = volumenMm3.movePointLeft(3)
            val peso = volumenCm3.multiply(material.densidad)
            val metalFino = peso.multiply(material.finura)
            val liga = peso.subtract(metalFino)

            // Red de seguridad, no lógica de negocio: la finura es ≤ 1 por construcción.
            check(liga.signum() >= 0) { "La liga no puede ser negativa y salió $liga" }

            return CalculoChapa(
                material = material,
                ancho = ancho,
                largo = largo,
                espesor = espesor,
                areaMm2 = areaMm2,
                volumenMm3 = volumenMm3,
                volumenCm3 = volumenCm3,
                densidad = material.densidad,
                peso = peso,
                metalFino = metalFino,
                liga = liga,
            )
        }
    }
}
