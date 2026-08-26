package com.jrblanco.calculadoradejoyeros2021.ui.oro

import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalLiga
import com.jrblanco.calculadoradejoyeros2021.ui.favoritos.AvisoFavorito

/**
 * Estado de la calculadora de aleaciones de oro.
 *
 * El constructor sin argumentos ES el estado inicial de la pantalla: campo vacío,
 * 18 K y amarillo, sin resultados. «Limpiar» y cada entrada al módulo vuelven aquí.
 * El aviso de 12 K no tiene campo propio: se deriva de [LeyOro.esSoloTecnica].
 */
data class OroUiState(
    /** Lo que el joyero ha tecleado, tal cual, con coma o con punto. */
    val cantidadTexto: String = "",
    val ley: LeyOro = LeyOro.LEY_18K,
    val color: ColorOro = ColorOro.AMARILLO,
    /** Presente solo con entrada válida; ausente = no se pinta nada. */
    val resultado: ResultadoOro? = null,
    /**
     * Lo que hay que decirle al joyero tras pulsar «Guardar en favoritos», de un solo uso: la vista
     * lo muestra y llama a `onAvisoFavoritoMostrado()`. Nulo mientras no hay nada que decir.
     */
    val avisoFavorito: AvisoFavorito? = null,
)

/**
 * Resultado ya listo para pintar: cifras formateadas (3 decimales, coma) en el orden
 * de [MetalLiga]. El valor exacto vive solo en el motor y jamás se recalcula desde
 * estas cadenas (§21 del documento técnico).
 */
data class ResultadoOro(
    val metales: List<MetalCalculado>,
    val totalFormateado: String,
)

data class MetalCalculado(
    val metal: MetalLiga,
    val gramosFormateados: String,
)
