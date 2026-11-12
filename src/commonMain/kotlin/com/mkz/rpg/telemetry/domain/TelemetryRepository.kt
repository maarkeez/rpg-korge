package com.mkz.rpg.telemetry.domain

interface TelemetryRepository {
    fun search(): Telemetry?

    fun save(telemetry: Telemetry)
}
