package com.mkz.tessavar.telemetry.usecases.commands

import com.mkz.tessavar.telemetry.usecases.services.RecordProductEvent

class RecordBattleStarted(
    private val recordProductEvent: RecordProductEvent,
) {
    operator fun invoke() {
        recordProductEvent { telemetry, occurredAt -> telemetry.battleStarted(occurredAt) }
    }
}
