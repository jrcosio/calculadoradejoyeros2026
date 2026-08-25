package com.jrblanco.calculadoradejoyeros2021.ui.ajustes

import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp

// --- Cómo se pinta cada idioma. Vive aquí y no en el enum para que `domain/` siga libre de ---
// --- Android, igual que los demás Presentacion*.kt del proyecto.                           ---

internal val IdiomaApp.banderaRes: Int
    get() = when (this) {
        IdiomaApp.ESPANOL -> R.drawable.ic_bandera_es
        IdiomaApp.INGLES -> R.drawable.ic_bandera_en
        IdiomaApp.FRANCES -> R.drawable.ic_bandera_fr
        IdiomaApp.ALEMAN -> R.drawable.ic_bandera_de
        IdiomaApp.ITALIANO -> R.drawable.ic_bandera_it
    }

/** El nombre del idioma, escrito en su propia lengua: no se traduce (ver `strings.xml`). */
internal val IdiomaApp.nombreRes: Int
    get() = when (this) {
        IdiomaApp.ESPANOL -> R.string.idioma_es
        IdiomaApp.INGLES -> R.string.idioma_en
        IdiomaApp.FRANCES -> R.string.idioma_fr
        IdiomaApp.ALEMAN -> R.string.idioma_de
        IdiomaApp.ITALIANO -> R.string.idioma_it
    }
