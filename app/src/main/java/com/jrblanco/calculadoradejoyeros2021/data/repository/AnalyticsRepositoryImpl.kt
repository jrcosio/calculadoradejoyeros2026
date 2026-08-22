package com.jrblanco.calculadoradejoyeros2021.data.repository

import com.jrblanco.calculadoradejoyeros2021.data.source.remote.FirebaseAnalyticsDataSource
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository

class AnalyticsRepositoryImpl(
    private val remote: FirebaseAnalyticsDataSource,
) : AnalyticsRepository {

    override fun logScreenView(screenName: String) = remote.logScreenView(screenName)

    override fun logEvent(name: String, params: Map<String, String>) =
        remote.logEvent(name, params)

    override fun recordError(throwable: Throwable) = remote.recordError(throwable)
}
