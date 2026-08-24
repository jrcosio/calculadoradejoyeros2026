package com.jrblanco.calculadoradejoyeros2021.ui.soldaduras

import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraClasica
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraPlata

/**
 * Las tres familias de soldadura de la pantalla. Concepto de UI, como `HomeModule`:
 * ningún caso de uso lo recibe — cada familia elige sus propios casos de uso.
 */
enum class FamiliaSoldadura {
    ORO_LEY,
    CLASICA,
    PLATA,
    ;

    /** Identificador estable para telemetría, independiente del idioma. */
    val analyticsId: String get() = name.lowercase()
}

/** Los dos modos de entrada del conmutador (§2.3 del documento técnico). */
enum class ModoEntradaSoldadura {
    /** El de los mockups: se introduce el metal que se tiene (oro o plata). */
    DESDE_METAL,

    /** El mínimo de la spec: se introduce el peso final de soldadura deseado. */
    PESO_FINAL,
    ;

    /** Identificador estable para telemetría, independiente del idioma. */
    val analyticsId: String get() = name.lowercase()
}

/**
 * Lo que puede aparecer en una fila de resultado. Existe aparte de `MetalSoldadura`
 * porque la BASE es un preparado de la otra pantalla, no un metal del motor, y porque el
 * oro de 18 K necesita el color elegido para su etiqueta.
 */
enum class IngredienteSoldadura {
    BASE,
    ORO_24K,
    ORO_18K,
    PLATA_FINA,
    LATON,
    COBRE,
    ZINC,
    CADMIO,
}

/** Una fila de resultado ya lista para pintar, con la cifra formateada. */
data class FilaSoldadura(
    val ingrediente: IngredienteSoldadura,
    val gramosFormateados: String,
)

/**
 * Resultado ya listo para pintar: filas en el orden de la receta y total, formateados a
 * 3 decimales con coma decimal. El valor exacto vive solo en el motor y jamás se
 * recalcula desde estas cadenas (§8.1, §8.3 del documento técnico).
 */
data class ResultadoSoldaduras(
    val filas: List<FilaSoldadura>,
    val totalFormateado: String,
)

/**
 * Estado de la calculadora de soldaduras.
 *
 * El constructor sin argumentos ES la primera visita (FR-002): `familia = null` y solo
 * se ve el selector de familias — sin formulario, sin resultados y sin botones. Elegir
 * familia arranca su formulario limpio; volver a entrar al módulo vuelve aquí.
 *
 * Los avisos no tienen campo propio: el de seguridad de CLÁSICA se deriva de
 * [TipoSoldaduraClasica.llevaCadmio].
 */
data class SoldadurasUiState(
    val familia: FamiliaSoldadura? = null,
    val modo: ModoEntradaSoldadura = ModoEntradaSoldadura.DESDE_METAL,
    /** Lo que el joyero ha tecleado, tal cual, con coma o con punto. */
    val cantidadTexto: String = "",
    /** Solo ORO LEY. No cambia cantidades: identifica el oro (§5.1, TEST 9). */
    val colorOro: ColorOroSoldadura = ColorOroSoldadura.AMARILLO,
    /** Solo ORO LEY. */
    val dureza: DurezaSoldaduraLey = DurezaSoldaduraLey.MUY_FLOJA,
    /** Solo CLÁSICA. */
    val tipoClasica: TipoSoldaduraClasica = TipoSoldaduraClasica.FLOJA,
    /** Solo PLATA. */
    val tipoPlata: TipoSoldaduraPlata = TipoSoldaduraPlata.MUY_FLOJA,
    /** Presente solo con familia elegida y entrada válida; ausente = no se pinta nada. */
    val resultado: ResultadoSoldaduras? = null,
)
