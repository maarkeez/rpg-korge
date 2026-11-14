package com.mkz.tessavar.telemetry.usecases.commands

import com.mkz.tessavar.telemetry.domain.Telemetry
import com.mkz.tessavar.telemetry.domain.TelemetryRepository

class DeclineConsent(
    private val telemetryRepository: TelemetryRepository,
    private val now: () -> Double,
) {
    operator fun invoke() {
        val telemetry = telemetryRepository.search() ?: Telemetry.notAsked()
        telemetryRepository.save(telemetry.declineConsent(decidedAt = now()))
    }
}
