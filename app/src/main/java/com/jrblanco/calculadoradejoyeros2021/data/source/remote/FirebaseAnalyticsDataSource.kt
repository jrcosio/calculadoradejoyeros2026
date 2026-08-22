package com.jrblanco.calculadoradejoyeros2021.data.source.remote

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Único punto del proyecto que habla con los SDK de Firebase.
 */
class FirebaseAnalyticsDataSource(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
) {
    fun logScreenView(screenName: String) {
        analytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply { putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName) },
        )
    }

    fun logEvent(name: String, params: Map<String, String>) {
        analytics.logEvent(
            name,
            Bundle().apply { params.forEach { (key, value) -> putString(key, value) } },
        )
    }

    fun recordError(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}
