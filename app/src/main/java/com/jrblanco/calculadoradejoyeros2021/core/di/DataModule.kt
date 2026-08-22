package com.jrblanco.calculadoradejoyeros2021.core.di

import com.jrblanco.calculadoradejoyeros2021.data.repository.AnalyticsRepositoryImpl
import com.jrblanco.calculadoradejoyeros2021.data.source.remote.FirebaseAnalyticsDataSource
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import org.koin.dsl.module

/**
 * Data sources y repositorios.
 *
 * Regla: se registra siempre `single<InterfazDeDominio> { Implementacion(get()) }`,
 * nunca la implementación como tipo público.
 */
val dataModule = module {
    single { FirebaseAnalyticsDataSource(get(), get()) }
    single<AnalyticsRepository> { AnalyticsRepositoryImpl(get()) }
}
