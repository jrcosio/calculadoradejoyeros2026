package com.jrblanco.calculadoradejoyeros2021.ui.plata

import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata

/**
 * Estado de la calculadora de aleaciones de plata.
 *
 * El constructor sin argumentos ES el estado inicial de la pantalla: campo vacío, 925‰ y
 * sin resultados. 925 es la plata Sterling, la ley de trabajo habitual y la que el mockup
 * muestra activa. «Limpiar» y cada entrada al módulo vuelven aquí.
 *
 * El aviso de ley técnica no tiene campo propio: se deriva de [LeyPlata.esSoloTecnica].
 */
data class PlataUiState(
    /** Lo que el joyero ha tecleado, tal cual, con coma o con punto. */
    val cantidadTexto: String = "",
    val ley: LeyPlata = LeyPlata.LEY_925,
    /** Presente solo con entrada válida; ausente = no se pinta nada. */
    val resultado: ResultadoPlata? = null,
)

/**
 * Resultado ya listo para pintar: cifras formateadas a 3 decimales **truncados** y con
 * coma decimal. El valor exacto vive solo en el motor y jamás se recalcula desde estas
 * cadenas (§14, §21 del documento técnico).
 */
data class ResultadoPlata(
    val cobreFormateado: String,
    val totalFormateado: String,
)
