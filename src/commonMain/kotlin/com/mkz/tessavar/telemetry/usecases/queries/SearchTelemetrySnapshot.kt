package com.mkz.tessavar.telemetry.usecases.queries

import com.mkz.tessavar.telemetry.domain.TelemetryRepository

class SearchTelemetrySnapshot(
    private val telemetryRepository: TelemetryRepository,
) {
    operator fun invoke(): String? = telemetryRepository.search()?.toDto()?.gameId
}
