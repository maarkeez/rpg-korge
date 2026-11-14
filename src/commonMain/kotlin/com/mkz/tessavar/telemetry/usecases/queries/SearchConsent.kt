package com.mkz.tessavar.telemetry.usecases.queries

import com.mkz.tessavar.telemetry.domain.Telemetry
import com.mkz.tessavar.telemetry.domain.TelemetryRepository

class SearchConsent(
    private val telemetryRepository: TelemetryRepository,
    private val now: () -> Double,
) {
    operator fun invoke(): Boolean = (telemetryRepository.search() ?: Telemetry.notAsked()).isConsentAsked(now())
}
