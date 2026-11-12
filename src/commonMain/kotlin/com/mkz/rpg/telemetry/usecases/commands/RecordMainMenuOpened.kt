package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent

class RecordMainMenuOpened(
    private val recordProductEvent: RecordProductEvent,
) {
    operator fun invoke() {
        recordProductEvent { telemetry, occurredAt -> telemetry.mainMenuOpened(occurredAt) }
    }
}
