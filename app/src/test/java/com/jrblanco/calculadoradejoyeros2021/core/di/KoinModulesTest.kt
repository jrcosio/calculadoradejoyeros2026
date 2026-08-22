package com.jrblanco.calculadoradejoyeros2021.core.di

import android.app.Application
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.KoinInternalApi
import org.koin.dsl.module
import org.koin.test.verify.verify

/**
 * Red de seguridad del grafo de DI.
 *
 * `verify()` recorre por reflexión los constructores de todo lo registrado y falla
 * si a alguno le falta una dependencia. Detecta un módulo de Koin roto en el test
 * unitario, no al arrancar la app en el móvil.
 *
 * [firebaseModule] queda fuera del `verify()` a propósito: sus definiciones no se
 * construyen con un constructor, sino con las fábricas estáticas `Firebase.analytics`
 * y `Firebase.crashlytics`, así que la reflexión acabaría inspeccionando tipos
 * internos de Google Play Services. Sus tipos entran como `extraTypes` para que el
 * resto del grafo sí se valide contra ellos, y el segundo test cubre que ese módulo
 * los declara de verdad.
 */
class KoinModulesTest {

    private val firebaseProvidedTypes = listOf(
        Application::class,
        Context::class,
        FirebaseAnalytics::class,
        FirebaseCrashlytics::class,
    )

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `el grafo de dependencias esta completo`() {
        // `includes` fusiona los módulos en un único índice: sin esto cada módulo se
        // verificaría aislado y no vería las definiciones de los demás.
        module { includes(featureModules) }.verify(extraTypes = firebaseProvidedTypes)
    }

    // `Module.mappings` es API interna, pero es la única forma de comprobar el
    // contenido de un módulo sin instanciarlo (instanciarlo arrancaría los SDK de
    // Firebase, imposible en un test unitario de JVM).
    @OptIn(KoinInternalApi::class)
    @Test
    fun `firebaseModule declara los dos SDK que consume la capa de datos`() {
        val declared = firebaseModule.mappings.values.map { it.beanDefinition.primaryType }

        assertTrue(
            "Falta la definición de FirebaseAnalytics",
            declared.contains(FirebaseAnalytics::class),
        )
        assertTrue(
            "Falta la definición de FirebaseCrashlytics",
            declared.contains(FirebaseCrashlytics::class),
        )
    }
}
