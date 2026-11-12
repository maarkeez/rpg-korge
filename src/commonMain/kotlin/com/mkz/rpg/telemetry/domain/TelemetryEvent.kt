package com.mkz.rpg.telemetry.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface TelemetryEvent : DomainEvent {
    object RequestRecordMainMenuOpened : TelemetryEvent

    object RequestStartGame : TelemetryEvent

    data class RequestRecordGameSaved(
        val slot: Int,
    ) : TelemetryEvent

    data class RequestRecordGameLoaded(
        val slot: Int,
    ) : TelemetryEvent
}
