package com.jrblanco.calculadoradejoyeros2021.ui.herramientas

import com.jrblanco.calculadoradejoyeros2021.R

/**
 * Mapeos de presentación del armazón, internos al paquete. `domain/` y el ViewModel no
 * conocen recursos: es la pantalla la que traduce cada sub-herramienta a etiqueta e icono.
 */
internal val Subherramienta.etiquetaRes: Int
    get() = when (this) {
        Subherramienta.PRECIOS -> R.string.herramientas_subherramienta_precios
        Subherramienta.CHAPAS -> R.string.herramientas_subherramienta_chapas
    }

internal val Subherramienta.iconRes: Int
    get() = when (this) {
        Subherramienta.PRECIOS -> R.drawable.ic_grafica
        Subherramienta.CHAPAS -> R.drawable.ic_capas
    }
