package com.mkz.tessavar.telemetry.domain

interface TelemetryRepository {
    fun search(): Telemetry?

    fun save(telemetry: Telemetry)
}
