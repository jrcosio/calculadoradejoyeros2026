package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/** Las dos familias del selector de material de la calculadora de chapas (§2). */
enum class FamiliaChapa {
    ORO,
    PLATA,
    ;

    val analyticsId: String get() = name.lowercase()
}

/**
 * Los ocho materiales de la calculadora de peso de chapas (§2, §5.1), en el orden en que se
 * pintan.
 *
 * Enum propio y no [LeyOro]/[LeyPlata] a propósito: la densidad es un dato de **este**
 * documento técnico, y su §19 prevé densidades distintas para una misma ley según el color
 * (18K amarillo 15,58; blanco 14,64; rojo 15,18), algo que una ley no puede expresar. La
 * coherencia de milésimas y de bandera técnica con las leyes de oro y plata la vigila un
 * test de paridad. Es el cuarto motor del proyecto y no depende de los otros tres.
 *
 * @property densidad g/cm³ **orientativos** de §5.1 (tablas técnicas y estimación
 *   plata-cobre); nunca se presentan como medidos.
 * @property esSoloTecnica 12K, 950 y 900 no figuran en el art. 9 de la Ley 17/1985 (§3): se
 *   calculan, pero no se llaman ley oficial española.
 */
enum class MaterialChapa(
    val familia: FamiliaChapa,
    val milesimas: Int,
    val densidad: BigDecimal,
    val esSoloTecnica: Boolean = false,
) {
    ORO_18K(FamiliaChapa.ORO, 750, BigDecimal("15.58")),
    ORO_14K(FamiliaChapa.ORO, 585, BigDecimal("13.07")),
    ORO_12K(FamiliaChapa.ORO, 500, BigDecimal("12.75"), esSoloTecnica = true),
    ORO_9K(FamiliaChapa.ORO, 375, BigDecimal("11.20")),
    PLATA_950(FamiliaChapa.PLATA, 950, BigDecimal("10.40"), esSoloTecnica = true),
    PLATA_925(FamiliaChapa.PLATA, 925, BigDecimal("10.36")),
    PLATA_900(FamiliaChapa.PLATA, 900, BigDecimal("10.31"), esSoloTecnica = true),
    PLATA_800(FamiliaChapa.PLATA, 800, BigDecimal("10.14")),
    ;

    /** Fracción de metal fino exacta (585 → 0.585). §3.1: 14K **es** 585, nunca 14/24. */
    val finura: BigDecimal get() = BigDecimal(milesimas).movePointLeft(3)

    /** Identificador estable para telemetría: "18k", "14k", "12k", "9k", "950", "925", "900", "800". */
    val analyticsId: String get() = name.substringAfter('_').lowercase()

    companion object {
        fun deFamilia(familia: FamiliaChapa): List<MaterialChapa> = entries.filter { it.familia == familia }

        /** 18K en oro (el material del mockup) y 925 en plata (la de trabajo, como en su calculadora). */
        fun porDefecto(familia: FamiliaChapa): MaterialChapa = when (familia) {
            FamiliaChapa.ORO -> ORO_18K
            FamiliaChapa.PLATA -> PLATA_925
        }
    }
}
