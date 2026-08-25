package com.jrblanco.calculadoradejoyeros2021.core.util

import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import java.util.Locale

/**
 * El idioma del dispositivo detrás de una interfaz, hermano de [Reloj].
 *
 * Existe por lo mismo que el reloj: para que «¿en qué idioma está el móvil?» se pueda contestar en
 * un test sin depender de dónde se ejecute. Es un tipo del grafo de Koin, no un valor por defecto
 * de constructor, así que vale tanto para `verify()` como para la resolución real.
 *
 * **Ojo con la tentación de leerlo del `Configuration` de la app**: en cuanto `ProveedorIdioma`
 * está en marcha, la configuración de la app dice el idioma *elegido*, y «Automático» dejaría de
 * saber a qué seguir. `Locale.getDefault()` sigue siendo el del sistema.
 */
interface IdiomaSistema {
    /** El idioma del dispositivo, ya reducido a los soportados; español si no hay coincidencia. */
    fun idioma(): IdiomaApp
}

class IdiomaSistemaJvm : IdiomaSistema {
    override fun idioma(): IdiomaApp =
        IdiomaApp.desdeEtiqueta(Locale.getDefault().language) ?: IdiomaApp.PREDETERMINADO
}
