package com.mkz.tessavar.telemetry.usecases.services

import com.mkz.tessavar.telemetry.domain.Telemetry
import com.mkz.tessavar.telemetry.domain.TelemetryRepository

class RecordProductEvent(
    private val telemetryRepository: TelemetryRepository,
    private val now: () -> Double,
) {
    operator fun invoke(record: (telemetry: Telemetry, occurredAt: Double) -> Telemetry) {
        val telemetry = telemetryRepository.search() ?: Telemetry.notAsked()
        telemetryRepository.save(record(telemetry, now()))
    }
}
