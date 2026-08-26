package com.jrblanco.calculadoradejoyeros2021.core.di

import com.jrblanco.calculadoradejoyeros2021.ui.ajustes.AjustesViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.favoritos.FavoritosViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.herramientas.HerramientasViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas.PesoChapasViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.herramientas.precios.PreciosMetalesViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.home.HomeViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.idioma.IdiomaAppViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.info.InfoViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.oro.OroViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.plata.PlataViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.soldaduras.SoldaduraBaseViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.soldaduras.SoldadurasViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Todo ViewModel se registra aquí con `viewModelOf(::NombreViewModel)`. */
val viewModelModule = module {
    viewModelOf(::WelcomeViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::InfoViewModel)
    viewModelOf(::OroViewModel)
    viewModelOf(::PlataViewModel)
    viewModelOf(::SoldadurasViewModel)
    viewModelOf(::SoldaduraBaseViewModel)
    // Herramientas (007): el armazón y cada sub-herramienta tienen su propio ViewModel.
    viewModelOf(::HerramientasViewModel)
    viewModelOf(::PreciosMetalesViewModel)
    viewModelOf(::PesoChapasViewModel)
    // Favoritos (009): la pestaña que sustituye al último placeholder.
    viewModelOf(::FavoritosViewModel)

    // Ajustes (008): la pantalla, y el idioma de la app entera, que lo posee la Activity.
    viewModelOf(::AjustesViewModel)
    viewModelOf(::IdiomaAppViewModel)
}
