package com.jrblanco.calculadoradejoyeros2021.core.util

import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp

/** [IdiomaSistema] de test: el dispositivo habla lo que diga el test. */
class IdiomaSistemaFalso(var idioma: IdiomaApp = IdiomaApp.ESPANOL) : IdiomaSistema {
    var consultas = 0
        private set

    override fun idioma(): IdiomaApp {
        consultas++
        return idioma
    }
}
