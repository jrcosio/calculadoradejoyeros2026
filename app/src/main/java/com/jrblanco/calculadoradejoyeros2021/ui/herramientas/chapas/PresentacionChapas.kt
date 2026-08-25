package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas

import androidx.compose.ui.graphics.Color
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.FamiliaChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.MaterialChapa
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors

/**
 * Mapeos de presentación de la calculadora de chapas, internos al paquete. `domain/` no conoce
 * Android: es la pantalla la que traduce cada enum a texto, color o icono. Las etiquetas de
 * ley y los avisos técnicos se reutilizan de las calculadoras de oro y plata.
 */
internal val FamiliaChapa.etiquetaRes: Int
    get() = when (this) {
        FamiliaChapa.ORO -> R.string.chapas_familia_oro
        FamiliaChapa.PLATA -> R.string.chapas_familia_plata
    }

/** ORO en dorado y PLATA en turquesa: decisión del autor (el mockup pintaba ambas en turquesa). */
internal val FamiliaChapa.acento: Color
    get() = when (this) {
        FamiliaChapa.ORO -> JewelryColors.GoldPrimary
        FamiliaChapa.PLATA -> JewelryColors.TealPrimary
    }

internal val FamiliaChapa.metalFinoRes: Int
    get() = when (this) {
        FamiliaChapa.ORO -> R.string.metal_oro_fino
        FamiliaChapa.PLATA -> R.string.metal_plata_fina
    }

/** «Oro %1$s» / «Plata %1$s», con la etiqueta de la ley como argumento. */
internal val FamiliaChapa.nombreMaterialRes: Int
    get() = when (this) {
        FamiliaChapa.ORO -> R.string.chapas_material_oro
        FamiliaChapa.PLATA -> R.string.chapas_material_plata
    }

internal val MaterialChapa.etiquetaRes: Int
    get() = when (this) {
        MaterialChapa.ORO_18K -> R.string.oro_ley_18k
        MaterialChapa.ORO_14K -> R.string.oro_ley_14k
        MaterialChapa.ORO_12K -> R.string.oro_ley_12k
        MaterialChapa.ORO_9K -> R.string.oro_ley_9k
        MaterialChapa.PLATA_950 -> R.string.plata_ley_950
        MaterialChapa.PLATA_925 -> R.string.plata_ley_925
        MaterialChapa.PLATA_900 -> R.string.plata_ley_900
        MaterialChapa.PLATA_800 -> R.string.plata_ley_800
    }

/** Aviso de ley técnica (§3): solo 12K, 950 y 900, con los textos que ya usa la app. */
internal val MaterialChapa.avisoRes: Int?
    get() = when (this) {
        MaterialChapa.ORO_12K -> R.string.oro_aviso_12k
        MaterialChapa.PLATA_950 -> R.string.plata_aviso_950
        MaterialChapa.PLATA_900 -> R.string.plata_aviso_900
        MaterialChapa.ORO_18K, MaterialChapa.ORO_14K, MaterialChapa.ORO_9K,
        MaterialChapa.PLATA_925, MaterialChapa.PLATA_800,
        -> null
    }

internal val MedidaChapa.etiquetaRes: Int
    get() = when (this) {
        MedidaChapa.ANCHO -> R.string.chapas_medida_ancho
        MedidaChapa.ESPESOR -> R.string.chapas_medida_espesor
        MedidaChapa.LARGO -> R.string.chapas_medida_largo
    }

internal val MedidaChapa.iconRes: Int
    get() = when (this) {
        MedidaChapa.ANCHO -> R.drawable.ic_ancho
        MedidaChapa.ESPESOR -> R.drawable.ic_espesor
        MedidaChapa.LARGO -> R.drawable.ic_regla
    }
