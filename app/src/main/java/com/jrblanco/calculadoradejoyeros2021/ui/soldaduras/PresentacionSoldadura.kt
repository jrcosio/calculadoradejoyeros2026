package com.jrblanco.calculadoradejoyeros2021.ui.soldaduras

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalSoldadura
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
