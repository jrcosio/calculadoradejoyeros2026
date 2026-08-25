package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.core.util.IdiomaSistema
import com.jrblanco.calculadoradejoyeros2021.domain.model.SeleccionIdioma
import com.jrblanco.calculadoradejoyeros2021.domain.repository.PreferenciasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Qué idioma toca, y por qué: junta la elección guardada con el idioma del dispositivo.
 *
 * Un solo flujo sirve a los dos consumidores: la raíz de la app solo mira
 * [SeleccionIdioma.efectivo], y la pantalla de Ajustes necesita además saber si manda el
 * dispositivo y cuál ha detectado.
 *
 * El idioma del sistema se lee **en cada emisión** y no una sola vez: si el joyero cambia el idioma
 * del móvil y vuelve a la app sin que el proceso muera, «Automático» sigue siendo verdad.
 */
class ObservarIdiomaUseCase(
    private val preferencias: PreferenciasRepository,
    private val idiomaSistema: IdiomaSistema,
) {
    operator fun invoke(): Flow<SeleccionIdioma> = preferencias.idioma.map { elegido ->
        SeleccionIdioma(elegido = elegido, sistema = idiomaSistema.idioma())
    }
}
