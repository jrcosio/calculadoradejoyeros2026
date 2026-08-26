package com.jrblanco.calculadoradejoyeros2021.ui.oro

import androidx.compose.ui.graphics.Color
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors

// --- Cómo se pinta cada valor de dominio del módulo de oro. Vive aquí y no en los enums para ---
// --- que `domain/` siga libre de Android, igual que `PresentacionSoldadura` en soldaduras.   ---
//
// Nacieron privados en `OroScreen.kt` y subieron a este fichero con la feature 009, cuando la
// pantalla de Favoritos pidió los mismos para componer el título de sus tarjetas. Es la regla del
// proyecto: en cuanto un segundo consumidor lo pide, deja de ser privado. `internal` y no `public`
// porque el consumidor está en el mismo módulo.

internal val LeyOro.etiquetaRes: Int
    get() = when (this) {
        LeyOro.LEY_18K -> R.string.oro_ley_18k
        LeyOro.LEY_14K -> R.string.oro_ley_14k
        LeyOro.LEY_12K -> R.string.oro_ley_12k
        LeyOro.LEY_9K -> R.string.oro_ley_9k
    }

internal val ColorOro.etiquetaRes: Int
    get() = when (this) {
        ColorOro.AMARILLO -> R.string.oro_color_amarillo
        ColorOro.BLANCO -> R.string.oro_color_blanco
        ColorOro.ROSA -> R.string.oro_color_rosa
        ColorOro.ROJO -> R.string.oro_color_rojo
    }

/** El tono con el que se pinta cada color de oro al seleccionarlo. */
internal val ColorOro.acento: Color
    get() = when (this) {
        ColorOro.AMARILLO -> JewelryColors.GoldPrimary
        ColorOro.BLANCO -> JewelryColors.TealPrimary
        ColorOro.ROSA -> JewelryColors.RoseGold
        ColorOro.ROJO -> JewelryColors.RedGold
    }
