package com.mkz.tessavar.telemetry.usecases.commands

import com.mkz.tessavar.telemetry.usecases.services.RecordProductEvent

class RecordGameLoaded(
    private val recordProductEvent: RecordProductEvent,
    private val newGameId: () -> String,
) {
    operator fun invoke(slot: Int) {
        recordProductEvent { telemetry, occurredAt -> telemetry.gameLoaded(slot, newGameId(), occurredAt) }
    }
}
