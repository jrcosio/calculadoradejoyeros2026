package com.jrblanco.calculadoradejoyeros2021.ui.herramientas

/**
 * Las dos utilidades excluyentes del módulo. Concepto de UI, como `HomeModule`: ningún caso
 * de uso lo recibe. El orden del enum es el del selector.
 */
enum class Subherramienta {
    PRECIOS,
    CHAPAS,
    ;

    /** Identificador estable para telemetría: "precios", "chapas". */
    val analyticsId: String get() = name.lowercase()
}

/**
 * Estado del armazón de Herramientas.
 *
 * El constructor sin argumentos **es** la primera visita: ninguna sub-herramienta elegida,
 * solo el selector y una invitación a elegir (FR-002). El estado de cada sub-herramienta vive
 * en su propio ViewModel, que se crea al abrirla por primera vez y sobrevive al cambio de
 * pestaña mientras el joyero siga en la pantalla.
 */
data class HerramientasUiState(
    val subherramienta: Subherramienta? = null,
)
