package com.jrblanco.calculadoradejoyeros2021.core.di

import com.jrblanco.calculadoradejoyeros2021.ui.home.HomeViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Todo ViewModel se registra aquí con `viewModelOf(::NombreViewModel)`. */
val viewModelModule = module {
    viewModelOf(::WelcomeViewModel)
    viewModelOf(::HomeViewModel)
}
