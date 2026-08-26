package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas

import com.jrblanco.calculadoradejoyeros2021.domain.model.MaterialChapa
import com.jrblanco.calculadoradejoyeros2021.ui.favoritos.AvisoFavorito
import java.math.BigDecimal

/**
 * Las tres medidas de la chapa, en el orden del mockup, que es el orden de pintado. El límite
 * operativo (§11.4 del documento técnico) es un control de interfaz: por encima se avisa y no
 * hay resultado, pero el motor calcularía igual.
 */
enum class MedidaChapa(val maximoMm: BigDecimal) {
    ANCHO(BigDecimal("10000")),
    ESPESOR(BigDecimal("1000")),
    LARGO(BigDecimal("10000")),
}

/** El resultado ya formateado: peso a dos decimales (§7), volumen y fino a tres, pureza a uno. */
data class ResultadoChapa(
    val pesoFormateado: String,
    val volumenFormateado: String,
    val densidadFormateada: String,
    val purezaFormateada: String,
    val metalFinoFormateado: String,
)

/**
 * Lo que necesita la ilustración: las proporciones visuales y la cota de cada medida válida
 * (sin unidad; la pone la vista). [completa] es falso mientras falte alguna medida y la chapa
 * se pinta atenuada.
 */
data class DibujoChapaUiState(
    val proporciones: ProporcionesChapa = ProporcionesChapa.REFERENCIA,
    val etiquetaAncho: String? = null,
    val etiquetaEspesor: String? = null,
    val etiquetaLargo: String? = null,
    val completa: Boolean = false,
)

/**
 * Estado de la calculadora de peso de chapas.
 *
 * El constructor sin argumentos ES el estado inicial: oro 18K, tres campos vacíos, la chapa
 * de referencia dibujada y sin resultado. La familia no se duplica: es `material.familia`.
 * Los textos de las medidas viajan tal cual se teclearon; el valor exacto vive solo en el motor.
 */
data class PesoChapasUiState(
    val material: MaterialChapa = MaterialChapa.ORO_18K,
    val medidas: Map<MedidaChapa, String> = MedidaChapa.entries.associateWith { "" },
    val fueraDeRango: Set<MedidaChapa> = emptySet(),
    val dibujo: DibujoChapaUiState = DibujoChapaUiState(),
    val resultado: ResultadoChapa? = null,
    /**
     * Lo que hay que decirle al joyero tras pulsar «Guardar en favoritos», de un solo uso: la vista
     * lo muestra y llama a `onAvisoFavoritoMostrado()`. Nulo mientras no hay nada que decir.
     */
    val avisoFavorito: AvisoFavorito? = null,
)
