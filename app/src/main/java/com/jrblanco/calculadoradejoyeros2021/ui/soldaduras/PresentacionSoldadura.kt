package com.jrblanco.calculadoradejoyeros2021.ui.soldaduras

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.ModoEntradaSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraClasica
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraPlata
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors

// --- Cómo se pinta cada valor de dominio en las dos pantallas de soldaduras. Vive ---
// --- aquí y no en los enums para que `domain/` siga libre de Android, igual que    ---
// --- `LeyPlata.etiquetaRes` en plata y `MetalLiga.presentacion()` en oro.          ---

/**
 * El ingrediente de presentación de cada metal del motor. Lo usan los dos ViewModels
 * para convertir componentes calculados en filas; ser extensión de paquete evita que
 * importen nada de Compose.
 */
internal val MetalSoldadura.ingrediente: IngredienteSoldadura
    get() = when (this) {
        MetalSoldadura.ORO_24K -> IngredienteSoldadura.ORO_24K
        MetalSoldadura.ORO_18K -> IngredienteSoldadura.ORO_18K
        MetalSoldadura.PLATA_FINA -> IngredienteSoldadura.PLATA_FINA
        MetalSoldadura.LATON -> IngredienteSoldadura.LATON
        MetalSoldadura.COBRE -> IngredienteSoldadura.COBRE
        MetalSoldadura.ZINC -> IngredienteSoldadura.ZINC
        MetalSoldadura.CADMIO -> IngredienteSoldadura.CADMIO
    }

/** La imagen de cada ingrediente de una fila de resultado. */
internal val IngredienteSoldadura.imagenRes: Int
    get() = when (this) {
        IngredienteSoldadura.BASE -> R.drawable.granalla
        IngredienteSoldadura.ORO_24K -> R.drawable.modulo_oro
        IngredienteSoldadura.ORO_18K -> R.drawable.modulo_oro
        IngredienteSoldadura.PLATA_FINA -> R.drawable.modulo_plata
        IngredienteSoldadura.LATON -> R.drawable.laton
        IngredienteSoldadura.COBRE -> R.drawable.cobre
        IngredienteSoldadura.ZINC -> R.drawable.zinc
        IngredienteSoldadura.CADMIO -> R.drawable.cadmio
    }

internal val IngredienteSoldadura.imagenDescripcionRes: Int
    get() = when (this) {
        IngredienteSoldadura.BASE -> R.string.soldadura_granalla_imagen
        IngredienteSoldadura.ORO_24K -> R.string.metal_oro_24k_imagen
        IngredienteSoldadura.ORO_18K -> R.string.oro_entrada_imagen
        IngredienteSoldadura.PLATA_FINA -> R.string.metal_plata_fina_imagen
        IngredienteSoldadura.LATON -> R.string.metal_laton_imagen
        IngredienteSoldadura.COBRE -> R.string.metal_cobre_imagen
        IngredienteSoldadura.ZINC -> R.string.metal_zinc_imagen
        IngredienteSoldadura.CADMIO -> R.string.metal_cadmio_imagen
    }

/**
 * Nombre visible de una fila de resultado. El oro de 18 K lleva el color elegido como
 * argumento («Oro 18K Blanco») y la base se llama «Soldadura BASE necesaria» cuando es
 * la respuesta del modo directo de ORO LEY.
 */
@Composable
internal fun nombreDeIngrediente(
    ingrediente: IngredienteSoldadura,
    colorOro: ColorOroSoldadura,
    baseNecesaria: Boolean,
): String = when (ingrediente) {
    IngredienteSoldadura.BASE -> stringResource(
        if (baseNecesaria) R.string.soldadura_fila_base_necesaria else R.string.soldadura_fila_base,
    )
    IngredienteSoldadura.ORO_24K -> stringResource(R.string.metal_oro_24k)
    IngredienteSoldadura.ORO_18K -> stringResource(
        R.string.soldadura_fila_oro18k,
        stringResource(colorOro.etiquetaRes),
    )
    IngredienteSoldadura.PLATA_FINA -> stringResource(R.string.metal_plata_fina)
    IngredienteSoldadura.LATON -> stringResource(R.string.metal_laton)
    IngredienteSoldadura.COBRE -> stringResource(R.string.metal_cobre)
    IngredienteSoldadura.ZINC -> stringResource(R.string.metal_zinc)
    IngredienteSoldadura.CADMIO -> stringResource(R.string.metal_cadmio)
}

/** Etiquetas de los tres colores admitidos (§5.1): las mismas que usa el módulo de oro. */
internal val ColorOroSoldadura.etiquetaRes: Int
    get() = when (this) {
        ColorOroSoldadura.AMARILLO -> R.string.oro_color_amarillo
        ColorOroSoldadura.BLANCO -> R.string.oro_color_blanco
        ColorOroSoldadura.ROSA -> R.string.oro_color_rosa
    }

/** El tono con el que se pinta cada color de oro, el mismo mapeo que en la calculadora de oro. */
internal val ColorOroSoldadura.acento: Color
    get() = when (this) {
        ColorOroSoldadura.AMARILLO -> JewelryColors.GoldPrimary
        ColorOroSoldadura.BLANCO -> JewelryColors.TealPrimary
        ColorOroSoldadura.ROSA -> JewelryColors.RoseGold
    }

// --- Los seis mapeos de abajo nacieron privados en `SoldadurasScreen.kt` y subieron aquí con  ---
// --- la feature 009, cuando la pantalla de Favoritos pidió los mismos para componer el título ---
// --- de sus tarjetas. La regla del proyecto: en cuanto hay un segundo consumidor, dejan de    ---
// --- ser privados.                                                                           ---

internal val FamiliaSoldadura.etiquetaRes: Int
    get() = when (this) {
        FamiliaSoldadura.ORO_LEY -> R.string.soldadura_familia_oro_ley
        FamiliaSoldadura.CLASICA -> R.string.soldadura_familia_clasica
        FamiliaSoldadura.PLATA -> R.string.soldadura_familia_plata
    }

/** La etiqueta del modo directo, que nombra el metal de entrada de cada familia. */
internal val FamiliaSoldadura.etiquetaModoDirectoRes: Int
    get() = when (this) {
        FamiliaSoldadura.ORO_LEY -> R.string.soldadura_modo_tengo_oro18k
        FamiliaSoldadura.CLASICA -> R.string.soldadura_modo_tengo_oro
        FamiliaSoldadura.PLATA -> R.string.soldadura_modo_tengo_plata
    }

/** Las dos familias de oro en dorado y la de plata en plateado, como el mockup. */
internal val FamiliaSoldadura.acento: Color
    get() = when (this) {
        FamiliaSoldadura.ORO_LEY -> JewelryColors.GoldPrimary
        FamiliaSoldadura.CLASICA -> JewelryColors.GoldPrimary
        FamiliaSoldadura.PLATA -> JewelryColors.SilverPrimary
    }

internal val TipoSoldaduraPlata.etiquetaRes: Int
    get() = when (this) {
        TipoSoldaduraPlata.MUY_FLOJA -> R.string.soldadura_plata_muy_floja
        TipoSoldaduraPlata.FLOJA -> R.string.soldadura_plata_floja
        TipoSoldaduraPlata.NORMAL -> R.string.soldadura_plata_normal
        TipoSoldaduraPlata.FUERTE -> R.string.soldadura_plata_fuerte
    }

internal val TipoSoldaduraClasica.etiquetaRes: Int
    get() = when (this) {
        TipoSoldaduraClasica.FLOJA -> R.string.soldadura_clasica_floja
        TipoSoldaduraClasica.FUERTE -> R.string.soldadura_clasica_fuerte
        TipoSoldaduraClasica.MUY_FLOJA_LEY -> R.string.soldadura_clasica_muy_floja_ley
    }

internal val DurezaSoldaduraLey.etiquetaRes: Int
    get() = when (this) {
        DurezaSoldaduraLey.MUY_FLOJA -> R.string.soldadura_dureza_muy_floja
        DurezaSoldaduraLey.FLOJA -> R.string.soldadura_dureza_floja
        DurezaSoldaduraLey.MEDIA -> R.string.soldadura_dureza_media
        DurezaSoldaduraLey.FUERTE -> R.string.soldadura_dureza_fuerte
        DurezaSoldaduraLey.MUY_FUERTE -> R.string.soldadura_dureza_muy_fuerte
    }

/**
 * La etiqueta del modo de una soldadura, que en el directo depende de la familia («Tengo oro 18K»,
 * «Tengo el oro», «Tengo la plata») y en el inverso es siempre la misma.
 *
 * No existía: las cuatro claves `soldadura_modo_*` se usaban en línea dentro de la pantalla, porque
 * hasta ahora nadie necesitaba nombrar un modo fuera del selector. El título de una tarjeta de
 * favoritos sí, y sin el modo «10 gr» sería ambiguo entre el metal de partida y el peso final.
 */
internal fun etiquetaModoRes(
    familia: FamiliaSoldadura,
    modo: ModoEntradaSoldadura,
): Int = when (modo) {
    ModoEntradaSoldadura.DESDE_METAL -> familia.etiquetaModoDirectoRes
    ModoEntradaSoldadura.PESO_FINAL -> R.string.soldadura_modo_peso_final
}

/** El equivalente para la soldadura BASE, que tiene sus propias dos etiquetas. */
internal fun etiquetaModoBaseRes(modo: ModoEntradaSoldadura): Int = when (modo) {
    ModoEntradaSoldadura.DESDE_METAL -> R.string.soldadura_base_modo_tengo_oro
    ModoEntradaSoldadura.PESO_FINAL -> R.string.soldadura_base_modo_peso
}
