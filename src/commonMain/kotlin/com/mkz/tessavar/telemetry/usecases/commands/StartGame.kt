package com.mkz.tessavar.telemetry.usecases.commands

import com.mkz.tessavar.telemetry.usecases.services.RecordProductEvent

class StartGame(
    private val recordProductEvent: RecordProductEvent,
    private val newGameId: () -> String,
) {
    operator fun invoke() {
        recordProductEvent { telemetry, occurredAt -> telemetry.startGame(newGameId(), occurredAt) }
    }
}
