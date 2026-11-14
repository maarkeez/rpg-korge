package com.mkz.tessavar.telemetry.usecases.commands

import com.mkz.tessavar.telemetry.domain.Telemetry
import com.mkz.tessavar.telemetry.domain.TelemetryRepository

class LoadTelemetrySnapshot(
    private val telemetryRepository: TelemetryRepository,
) {
    operator fun invoke(gameId: String?) {
        val telemetry = telemetryRepository.search() ?: Telemetry.notAsked()
        telemetryRepository.save(telemetry.loadGameId(gameId))
    }
}
