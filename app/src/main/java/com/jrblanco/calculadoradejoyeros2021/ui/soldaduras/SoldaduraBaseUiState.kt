package com.jrblanco.calculadoradejoyeros2021.ui.soldaduras

import com.jrblanco.calculadoradejoyeros2021.domain.model.ModoEntradaSoldadura
import com.jrblanco.calculadoradejoyeros2021.ui.favoritos.AvisoFavorito

/**
 * Estado de la pantalla de la soldadura BASE.
 *
 * El constructor sin argumentos ES el estado inicial: modo desde el oro, campo vacío y
 * sin resultados. Los avisos no tienen campo propio: la receta de la base siempre lleva
 * cadmio y zinc, así que la advertencia de seguridad de §9 es fija en esta pantalla.
 */
data class SoldaduraBaseUiState(
    val modo: ModoEntradaSoldadura = ModoEntradaSoldadura.DESDE_METAL,
    /** Lo que el joyero ha tecleado, tal cual, con coma o con punto. */
    val cantidadTexto: String = "",
    /** Presente solo con entrada válida; ausente = no se pinta nada. */
    val resultado: ResultadoSoldaduraBase? = null,
    /**
     * Lo que hay que decirle al joyero tras pulsar «Guardar en favoritos», de un solo uso: la vista
     * lo muestra y llama a `onAvisoFavoritoMostrado()`. Nulo mientras no hay nada que decir.
     */
    val avisoFavorito: AvisoFavorito? = null,
)

/**
 * Resultado ya listo para pintar: las filas de liga en el orden de §5.2 (más el oro 24K
 * en modo inverso) y el peso teórico de base. Formateado a 3 decimales con coma decimal;
 * el valor exacto vive solo en el motor (§8.1, §8.3).
 */
data class ResultadoSoldaduraBase(
    val filas: List<FilaSoldadura>,
    val totalFormateado: String,
)
