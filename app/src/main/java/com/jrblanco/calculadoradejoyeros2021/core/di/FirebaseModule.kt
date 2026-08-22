package com.jrblanco.calculadoradejoyeros2021.core.di

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import org.koin.dsl.module

/** SDK de Firebase. Nada fuera de `data/source/remote` debería depender de estos tipos. */
val firebaseModule = module {
    single<FirebaseAnalytics> { Firebase.analytics }
    single<FirebaseCrashlytics> { Firebase.crashlytics }
}
