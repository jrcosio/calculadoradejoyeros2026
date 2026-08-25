package com.jrblanco.calculadoradejoyeros2021.domain.model

/** Qué hacer con la instantánea guardada antes de pintar la pantalla de precios. */
sealed interface DecisionCache {
    /** Todo vigente: se sirve lo guardado, cero red. */
    data object Servir : DecisionCache

    /** Hay metales sin precio vigente pero el último intento es demasiado reciente: cero red. */
    data object Esperar : DecisionCache

    /** Consultar al proveedor **solo** por estos metales. */
    data class Actualizar(val pendientes: Set<MetalCotizado>) : DecisionCache
}

/**
 * La regla de la caché de una hora, como función pura: sin corrutinas, sin reloj real, sin
 * red. Es la pieza que decide cuándo se gasta cuota del proveedor, y por eso se prueba sola.
 *
 * Un metal está vigente si su cotización se obtuvo hace menos de [vigenciaMillis]. Si los
 * cinco lo están, [DecisionCache.Servir]. Si no, y el último intento de red fue hace menos
 * de la espera aplicable —[esperaReintentoMillis], o [esperaTrasLimiteMillis] cuando el
 * proveedor rechazó por cuota—, [DecisionCache.Esperar]. En otro caso,
 * [DecisionCache.Actualizar] con los metales sin precio vigente: un fallo de rodio no obliga
 * a repetir los otros cuatro.
 */
class PoliticaCacheCotizaciones(
    val vigenciaMillis: Long = 3_600_000L,
    val esperaReintentoMillis: Long = 60_000L,
    val esperaTrasLimiteMillis: Long = 300_000L,
) {
    fun decidir(guardada: InstantaneaCotizaciones, ahoraMillis: Long): DecisionCache {
        val pendientes = MetalCotizado.entries
            .filterNot { guardada.esVigente(it, ahoraMillis, vigenciaMillis) }
            .toSet()
        if (pendientes.isEmpty()) return DecisionCache.Servir

        val intento = guardada.instanteIntentoEpochMillis
        if (intento != null) {
            val transcurrido = ahoraMillis - intento
            val espera = if (guardada.hayErrorPorLimite) esperaTrasLimiteMillis else esperaReintentoMillis
            // Un intento «del futuro» (reloj atrasado) no bloquea: se vuelve a consultar.
            if (transcurrido >= 0 && transcurrido < espera) return DecisionCache.Esperar
        }
        return DecisionCache.Actualizar(pendientes)
    }
}
