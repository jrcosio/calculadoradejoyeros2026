package com.jrblanco.calculadoradejoyeros2021.domain.repository

/**
 * Contrato de telemetría visto desde el dominio.
 *
 * Firebase no aparece en esta firma a propósito: `domain` no conoce a `data`.
 */
interface AnalyticsRepository {
    fun logScreenView(screenName: String)
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
    fun recordError(throwable: Throwable)
}
