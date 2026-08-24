package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/**
 * Las recetas y factores de la calculadora de soldaduras: única fuente de verdad (§7 del
 * documento técnico), versión de recetas 1.0.
 *
 * Transcripción literal de §3, §4.1, §5.2 y §5.4 de
 * `UI_Plantillas/Feature_Soldadura/ESPECIFICACION_CALCULADORA_SOLDADURAS_JOYERIA.md`.
 * Los valores se construyen desde literales `String` para que `BigDecimal` no herede el
 * error binario de un `Double` (§2.1). **Prohibido duplicar estos números** en la
 * interfaz o en los casos de uso (§7).
 */
object RecetasSoldadura {

    const val VERSION_RECETAS = "1.0"

    private fun receta(vararg componentes: Pair<MetalSoldadura, String>) = RecetaSoldadura(
        componentes = componentes.map { (metal, literal) ->
            ComponenteReceta(metal, BigDecimal(literal))
        },
    )

    // §3.2 — Clásica floja: 5 g de oro 18K + 2 g de plata fina + 1 g de latón (total 8).
    private val CLASICA_FLOJA = receta(
        MetalSoldadura.ORO_18K to "5",
        MetalSoldadura.PLATA_FINA to "2",
        MetalSoldadura.LATON to "1",
    )

    // §3.3 — Clásica fuerte: 5 g de oro 18K + 0,50 g de plata + cobre + latón (total 6,50).
    private val CLASICA_FUERTE = receta(
        MetalSoldadura.ORO_18K to "5",
        MetalSoldadura.PLATA_FINA to "0.50",
        MetalSoldadura.COBRE to "0.50",
        MetalSoldadura.LATON to "0.50",
    )

    // §3.4 — Clásica muy floja de ley: 1 g de oro 24K + 0,10 + 0,16 + 0,18 (total 1,44).
    // La equivalencia ×7 del documento suma 10,08 g, no 10.
    private val CLASICA_MUY_FLOJA_LEY = receta(
        MetalSoldadura.ORO_24K to "1",
        MetalSoldadura.PLATA_FINA to "0.10",
        MetalSoldadura.LATON to "0.16",
        MetalSoldadura.CADMIO to "0.18",
    )

    /**
     * §5.2 — La base de oro de 18 K: por 10 g de oro fino 24K, 0,54 g de cobre, 0,80 g de
     * plata fina, 0,92 g de zinc y 1,00 g de cadmio (total teórico 13,26 g).
     *
     * Estos son los valores del documento técnico. El mockup de la pantalla los muestra
     * **intercambiados** (plata 0,54 / cobre 0,80 / cadmio 0,92 / zinc 1,00) y §12 da la
     * prevalencia al documento: prohibido «corregirlos» mirando el PNG.
     */
    val BASE = receta(
        MetalSoldadura.ORO_24K to "10",
        MetalSoldadura.COBRE to "0.54",
        MetalSoldadura.PLATA_FINA to "0.80",
        MetalSoldadura.ZINC to "0.92",
        MetalSoldadura.CADMIO to "1.00",
    )

    /** La receta patrón de cada tipo clásico (§3). */
    fun clasica(tipo: TipoSoldaduraClasica): RecetaSoldadura = when (tipo) {
        TipoSoldaduraClasica.FLOJA -> CLASICA_FLOJA
        TipoSoldaduraClasica.FUERTE -> CLASICA_FUERTE
        TipoSoldaduraClasica.MUY_FLOJA_LEY -> CLASICA_MUY_FLOJA_LEY
    }

    /**
     * §4.1 — Factor `p` de latón **respecto a la plata fina** (no sobre el peso final;
     * interpretación obligatoria del documento).
     */
    fun factorLaton(tipo: TipoSoldaduraPlata): BigDecimal = when (tipo) {
        TipoSoldaduraPlata.MUY_FLOJA -> BigDecimal("0.75")
        TipoSoldaduraPlata.FLOJA -> BigDecimal("0.50")
        TipoSoldaduraPlata.NORMAL -> BigDecimal("0.40")
        TipoSoldaduraPlata.FUERTE -> BigDecimal("0.30")
    }

    /** §5.4 — Factor `r`: gramos de oro de 18 K por cada gramo de base. */
    fun factorOro(dureza: DurezaSoldaduraLey): BigDecimal = when (dureza) {
        DurezaSoldaduraLey.MUY_FLOJA -> BigDecimal("0.3")
        DurezaSoldaduraLey.FLOJA -> BigDecimal("0.5")
        DurezaSoldaduraLey.MEDIA -> BigDecimal("1")
        DurezaSoldaduraLey.FUERTE -> BigDecimal("2")
        DurezaSoldaduraLey.MUY_FUERTE -> BigDecimal("3")
    }
}
