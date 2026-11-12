package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.domain.Telemetry
import com.mkz.rpg.telemetry.domain.TelemetryRepository

class StartSession(
    private val telemetryRepository: TelemetryRepository,
) {
    operator fun invoke(sessionId: String) {
        val telemetry = telemetryRepository.search() ?: Telemetry.notAsked()
        telemetryRepository.save(telemetry.startSession(sessionId))
    }
}
