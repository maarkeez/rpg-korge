package com.mkz.tessavar.telemetry.usecases.commands

import com.mkz.tessavar.telemetry.domain.Telemetry
import com.mkz.tessavar.telemetry.domain.TelemetryRepository

class GiveConsent(
    private val telemetryRepository: TelemetryRepository,
    private val now: () -> Double,
    private val newInstallId: () -> String,
) {
    operator fun invoke() {
        val telemetry = telemetryRepository.search() ?: Telemetry.notAsked()
        telemetryRepository.save(telemetry.giveConsent(installId = newInstallId(), decidedAt = now()))
    }
}
