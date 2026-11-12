package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.domain.Telemetry
import com.mkz.rpg.telemetry.domain.TelemetryRepository

class LoadTelemetrySnapshot(
    private val telemetryRepository: TelemetryRepository,
) {
    operator fun invoke(gameId: String?) {
        val telemetry = telemetryRepository.search() ?: Telemetry.notAsked()
        telemetryRepository.save(telemetry.loadGameId(gameId))
    }
}
