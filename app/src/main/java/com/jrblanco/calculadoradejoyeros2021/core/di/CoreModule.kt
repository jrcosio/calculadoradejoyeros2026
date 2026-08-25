package com.jrblanco.calculadoradejoyeros2021.core.di

import com.jrblanco.calculadoradejoyeros2021.core.util.DefaultDispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.core.util.Reloj
import com.jrblanco.calculadoradejoyeros2021.core.util.RelojSistema
import org.koin.dsl.module

val coreModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    // Hora del sistema tras interfaz: la caché de cotizaciones se prueba con un reloj falso.
    single<Reloj> { RelojSistema() }
}
