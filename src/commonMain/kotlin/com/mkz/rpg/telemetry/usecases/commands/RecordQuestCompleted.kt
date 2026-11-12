package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent

class RecordQuestCompleted(
    private val recordProductEvent: RecordProductEvent,
) {
    operator fun invoke(questId: String) {
        recordProductEvent { telemetry, occurredAt -> telemetry.questCompleted(questId, occurredAt) }
    }
}
