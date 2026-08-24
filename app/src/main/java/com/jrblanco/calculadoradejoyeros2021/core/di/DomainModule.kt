package com.jrblanco.calculadoradejoyeros2021.core.di

import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularAleacionInversaOroUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularAleacionInversaPlataUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularAleacionOroUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularAleacionPlataUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraBaseInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraBaseUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraClasicaInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraClasicaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraLeyDesdeOroUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraLeyInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraLeyUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraPlataInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraPlataUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Casos de uso. Se llena feature a feature vía SDD. */
val domainModule = module {
    factoryOf(::CalcularAleacionOroUseCase)
    factoryOf(::CalcularAleacionInversaOroUseCase)
    factoryOf(::CalcularAleacionPlataUseCase)
    factoryOf(::CalcularAleacionInversaPlataUseCase)
    factoryOf(::CalcularSoldaduraClasicaUseCase)
    factoryOf(::CalcularSoldaduraClasicaInversaUseCase)
    factoryOf(::CalcularSoldaduraPlataUseCase)
    factoryOf(::CalcularSoldaduraPlataInversaUseCase)
    factoryOf(::CalcularSoldaduraBaseUseCase)
    factoryOf(::CalcularSoldaduraBaseInversaUseCase)
    factoryOf(::CalcularSoldaduraLeyDesdeOroUseCase)
    // Sin UI en esta versión (§5.4, TEST 7): existe y se prueba, precedente de la 005.
    factoryOf(::CalcularSoldaduraLeyUseCase)
    factoryOf(::CalcularSoldaduraLeyInversaUseCase)
}
