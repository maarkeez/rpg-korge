package com.mkz.tessavar.telemetry.usecases.commands

import com.mkz.tessavar.telemetry.domain.Telemetry
import com.mkz.tessavar.telemetry.domain.TelemetryRepository

class StartSession(
    private val telemetryRepository: TelemetryRepository,
) {
    operator fun invoke(sessionId: String) {
        val telemetry = telemetryRepository.search() ?: Telemetry.notAsked()
        telemetryRepository.save(telemetry.startSession(sessionId))
    }
}
