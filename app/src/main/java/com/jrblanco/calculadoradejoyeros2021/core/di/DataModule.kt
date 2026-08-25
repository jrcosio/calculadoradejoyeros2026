package com.jrblanco.calculadoradejoyeros2021.core.di

import com.jrblanco.calculadoradejoyeros2021.data.repository.AnalyticsRepositoryImpl
import com.jrblanco.calculadoradejoyeros2021.data.repository.CotizacionesRepositoryImpl
import com.jrblanco.calculadoradejoyeros2021.data.repository.PreferenciasRepositoryImpl
import com.jrblanco.calculadoradejoyeros2021.data.source.local.AjustesLocalDataSource
import com.jrblanco.calculadoradejoyeros2021.data.source.local.CotizacionesLocalDataSource
import com.jrblanco.calculadoradejoyeros2021.data.source.local.DataStoreAjustesLocalDataSource
import com.jrblanco.calculadoradejoyeros2021.data.source.local.SharedPreferencesCotizacionesLocalDataSource
import com.jrblanco.calculadoradejoyeros2021.data.source.remote.ClienteHttp
import com.jrblanco.calculadoradejoyeros2021.data.source.remote.ClienteHttpUrlConnection
import com.jrblanco.calculadoradejoyeros2021.data.source.remote.CotizacionesRemoteDataSource
import com.jrblanco.calculadoradejoyeros2021.data.source.remote.FirebaseAnalyticsDataSource
import com.jrblanco.calculadoradejoyeros2021.data.source.remote.MetalSentinelDataSource
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.repository.CotizacionesRepository
import com.jrblanco.calculadoradejoyeros2021.domain.repository.PreferenciasRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Regla: se registra siempre `single<InterfazDeDominio> { Implementacion(get()) }`,
 * nunca la implementación como tipo público.
 *
 * Los data sources van concretos y con `bind` a su interfaz a propósito: `verify()` de Koin
 * solo inspecciona los constructores del tipo primario de cada definición, así que solo así
 * comprueba que a `MetalSentinelDataSource` y a la caché no les falta ninguna dependencia.
 */
val dataModule = module {
    single { FirebaseAnalyticsDataSource(get(), get()) }
    single<AnalyticsRepository> { AnalyticsRepositoryImpl(get()) }

    // Cotizaciones (007): sin dependencias externas, HttpURLConnection y SharedPreferences.
    single<ClienteHttp> { ClienteHttpUrlConnection() }
    single { MetalSentinelDataSource(get(), get(), get()) } bind CotizacionesRemoteDataSource::class
    single { SharedPreferencesCotizacionesLocalDataSource(androidContext(), get()) } bind CotizacionesLocalDataSource::class
    single<CotizacionesRepository> { CotizacionesRepositoryImpl(get(), get(), get()) }

    // Ajustes (008): DataStore, con el almacén dentro del data source (ver su KDoc).
    single { DataStoreAjustesLocalDataSource(androidContext(), get()) } bind AjustesLocalDataSource::class
    single<PreferenciasRepository> { PreferenciasRepositoryImpl(get()) }
}
