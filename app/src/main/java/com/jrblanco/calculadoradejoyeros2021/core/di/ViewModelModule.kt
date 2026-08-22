package com.jrblanco.calculadoradejoyeros2021.core.di

import com.jrblanco.calculadoradejoyeros2021.ui.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Todo ViewModel se registra aquí con `viewModelOf(::NombreViewModel)`. */
val viewModelModule = module {
    viewModelOf(::HomeViewModel)
}
