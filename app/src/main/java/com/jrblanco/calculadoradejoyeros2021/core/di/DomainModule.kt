package com.jrblanco.calculadoradejoyeros2021.core.di

import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularAleacionInversaOroUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularAleacionOroUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Casos de uso. Se llena feature a feature vía SDD. */
val domainModule = module {
    factoryOf(::CalcularAleacionOroUseCase)
    factoryOf(::CalcularAleacionInversaOroUseCase)
}
