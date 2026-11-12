package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent

class RecordQuestStarted(
    private val recordProductEvent: RecordProductEvent,
) {
    operator fun invoke(questId: String) {
        recordProductEvent { telemetry, occurredAt -> telemetry.questStarted(questId, occurredAt) }
    }
}
