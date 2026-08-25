package com.jrblanco.calculadoradejoyeros2021.domain.repository

import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import kotlinx.coroutines.flow.Flow

/**
 * Los ajustes que el joyero elige y la app recuerda. Hoy solo el idioma; el nombre va en plural
 * para que el día que Ajustes crezca (tema, unidad por defecto) no haya que renombrar nada.
 *
 * `Flow` y no `suspend`, al contrario que `CotizacionesRepository`: aquí hay dos consumidores que
 * necesitan enterarse de los cambios —la raíz de la app y la pantalla de Ajustes—, y de esa
 * emisión depende que un toque en una bandera repinte la app entera.
 */
interface PreferenciasRepository {
    /** Emite la elección actual y cada cambio. `null` = «Automático»: manda el dispositivo. */
    val idioma: Flow<IdiomaApp?>

    /** Guarda la elección. `null` devuelve el control al dispositivo. */
    suspend fun guardarIdioma(idioma: IdiomaApp?)
}
