package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.domain.Telemetry
import com.mkz.rpg.telemetry.domain.TelemetryRepository

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
