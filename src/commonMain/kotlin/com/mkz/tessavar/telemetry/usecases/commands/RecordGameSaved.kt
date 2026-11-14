package com.mkz.tessavar.telemetry.usecases.commands

import com.mkz.tessavar.telemetry.usecases.services.RecordProductEvent

class RecordGameSaved(
    private val recordProductEvent: RecordProductEvent,
) {
    operator fun invoke(slot: Int) {
        recordProductEvent { telemetry, occurredAt -> telemetry.gameSaved(slot, occurredAt) }
    }
}
