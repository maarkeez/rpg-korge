package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent

class RecordGameSaved(
    private val recordProductEvent: RecordProductEvent,
) {
    operator fun invoke(slot: Int) {
        recordProductEvent { telemetry, occurredAt -> telemetry.gameSaved(slot, occurredAt) }
    }
}
