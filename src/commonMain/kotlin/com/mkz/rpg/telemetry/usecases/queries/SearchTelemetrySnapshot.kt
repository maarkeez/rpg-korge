package com.mkz.rpg.telemetry.usecases.queries

import com.mkz.rpg.telemetry.domain.TelemetryRepository

class SearchTelemetrySnapshot(
    private val telemetryRepository: TelemetryRepository,
) {
    operator fun invoke(): String? = telemetryRepository.search()?.toDto()?.gameId
}
