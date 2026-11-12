package com.mkz.rpg.telemetry.usecases.queries

import com.mkz.rpg.telemetry.domain.Telemetry
import com.mkz.rpg.telemetry.domain.TelemetryRepository

class SearchConsent(
    private val telemetryRepository: TelemetryRepository,
    private val now: () -> Double,
) {
    operator fun invoke(): Boolean = (telemetryRepository.search() ?: Telemetry.notAsked()).isConsentAsked(now())
}
