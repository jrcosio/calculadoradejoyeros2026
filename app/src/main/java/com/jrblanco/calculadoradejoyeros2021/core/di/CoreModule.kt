package com.jrblanco.calculadoradejoyeros2021.core.di

import com.jrblanco.calculadoradejoyeros2021.core.util.DefaultDispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import org.koin.dsl.module

val coreModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
}
