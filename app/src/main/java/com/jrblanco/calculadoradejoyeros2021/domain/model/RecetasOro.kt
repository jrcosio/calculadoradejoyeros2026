package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/**
 * Las 16 recetas de liga de la aplicación: única fuente de verdad (§15 del documento
 * técnico), versión de recetas 1.0.
 *
 * Transcripción literal de §7.1–§7.4 de
 * `UI_Plantillas/Feature_Oro/ESPECIFICACION_CALCULADORA_ALEACIONES_ORO.md`. Las
 * proporciones se construyen desde literales `String` para que `BigDecimal` no herede
 * el error binario de un `Double`. **Prohibido normalizarlas o sustituirlas** (§24,
 * §26): las de 18 K amarillo y 18 K blanco son recetas de taller requisito del
 * proyecto, y las de blanco son sin níquel.
 */
object RecetasOro {

    const val VERSION_RECETAS = "1.0"

    private fun receta(
        color: ColorOro,
        ley: LeyOro,
        vararg proporciones: Pair<MetalLiga, String>,
    ) = RecetaLiga(
        color = color,
        ley = ley,
        proporciones = proporciones.associate { (metal, literal) -> metal to BigDecimal(literal) },
    )

    private val recetas: Map<Pair<ColorOro, LeyOro>, RecetaLiga> = listOf(
        // §7.1 — Oro amarillo. La de 18 K es la receta de taller (22.00 Ag / 11.33 Cu).
        receta(ColorOro.AMARILLO, LeyOro.LEY_18K,
            MetalLiga.PLATA_FINA to "0.6600660066006601",
            MetalLiga.COBRE to "0.3399339933993399"),
        receta(ColorOro.AMARILLO, LeyOro.LEY_14K,
            MetalLiga.PLATA_FINA to "0.7194244604316547",
            MetalLiga.COBRE to "0.2805755395683453"),
        receta(ColorOro.AMARILLO, LeyOro.LEY_12K,
            MetalLiga.PLATA_FINA to "0.6997596153846154",
            MetalLiga.COBRE to "0.3002403846153846"),
        receta(ColorOro.AMARILLO, LeyOro.LEY_9K,
            MetalLiga.PLATA_FINA to "0.68",
            MetalLiga.COBRE to "0.32"),

        // §7.2 — Oro blanco, siempre sin níquel. La de 18 K es la receta de taller
        // (14.75 Pd / 13.18 Ag / 5.40 Cu).
        receta(ColorOro.BLANCO, LeyOro.LEY_18K,
            MetalLiga.PLATA_FINA to "0.3954395439543954",
            MetalLiga.COBRE to "0.1620162016201620",
            MetalLiga.PALADIO to "0.4425442544254425"),
        receta(ColorOro.BLANCO, LeyOro.LEY_14K,
            MetalLiga.PLATA_FINA to "0.7721822541966426",
            MetalLiga.PALADIO to "0.2278177458033573"),
        receta(ColorOro.BLANCO, LeyOro.LEY_12K,
            MetalLiga.PLATA_FINA to "0.8858173076923077",
            MetalLiga.PALADIO to "0.1141826923076923"),
        receta(ColorOro.BLANCO, LeyOro.LEY_9K,
            MetalLiga.PLATA_FINA to "1.0"),

        // §7.3 — Oro rojo: sistema Au + Cu, toda la liga es cobre.
        receta(ColorOro.ROJO, LeyOro.LEY_18K, MetalLiga.COBRE to "1.0"),
        receta(ColorOro.ROJO, LeyOro.LEY_14K, MetalLiga.COBRE to "1.0"),
        receta(ColorOro.ROJO, LeyOro.LEY_12K, MetalLiga.COBRE to "1.0"),
        receta(ColorOro.ROJO, LeyOro.LEY_9K, MetalLiga.COBRE to "1.0"),

        // §7.4 — Oro rosa.
        receta(ColorOro.ROSA, LeyOro.LEY_18K,
            MetalLiga.PLATA_FINA to "0.112",
            MetalLiga.COBRE to "0.888"),
        receta(ColorOro.ROSA, LeyOro.LEY_14K,
            MetalLiga.PLATA_FINA to "0.2206235011990408",
            MetalLiga.COBRE to "0.7793764988009592"),
        receta(ColorOro.ROSA, LeyOro.LEY_12K,
            MetalLiga.PLATA_FINA to "0.2701923076923077",
            MetalLiga.COBRE to "0.7298076923076923"),
        receta(ColorOro.ROSA, LeyOro.LEY_9K,
            MetalLiga.PLATA_FINA to "0.32",
            MetalLiga.COBRE to "0.68"),
    ).associateBy { it.color to it.ley }

    /** Receta para una combinación. Existen las 16: color y ley cualesquiera valen. */
    fun receta(color: ColorOro, ley: LeyOro): RecetaLiga = recetas.getValue(color to ley)
}
