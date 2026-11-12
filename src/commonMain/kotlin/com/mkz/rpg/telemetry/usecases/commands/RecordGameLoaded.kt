package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent

class RecordGameLoaded(
    private val recordProductEvent: RecordProductEvent,
) {
    operator fun invoke(slot: Int) {
        recordProductEvent { telemetry, occurredAt -> telemetry.gameLoaded(slot, occurredAt) }
    }
}
