package com.jrblanco.calculadoradejoyeros2021.core.di

/**
 * Módulos escritos a mano en el proyecto.
 *
 * `KoinModulesTest` los verifica en bloque: si a un `single` o un `viewModel` le
 * falta una dependencia, el test unitario falla sin necesidad de arrancar la app.
 * Todo módulo nuevo va aquí, y así queda cubierto por el test automáticamente.
 */
val featureModules = listOf(
    coreModule,
    dataModule,
    domainModule,
    viewModelModule,
)

/**
 * Grafo completo que carga la app: los módulos propios más los SDK externos.
 */
val appModules = listOf(firebaseModule) + featureModules
