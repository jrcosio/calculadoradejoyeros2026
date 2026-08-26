package com.jrblanco.calculadoradejoyeros2021.ui.plata

import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata

// --- Cómo se pinta la ley de plata. Nació privado en `PlataScreen.kt` y subió aquí con la ---
// --- feature 009, cuando Favoritos pidió la misma etiqueta para el título de su tarjeta.  ---

internal val LeyPlata.etiquetaRes: Int
    get() = when (this) {
        LeyPlata.LEY_950 -> R.string.plata_ley_950
        LeyPlata.LEY_925 -> R.string.plata_ley_925
        LeyPlata.LEY_900 -> R.string.plata_ley_900
        LeyPlata.LEY_800 -> R.string.plata_ley_800
    }
