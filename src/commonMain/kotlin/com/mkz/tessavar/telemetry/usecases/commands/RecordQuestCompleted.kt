package com.mkz.tessavar.telemetry.usecases.commands

import com.mkz.tessavar.telemetry.usecases.services.RecordProductEvent

class RecordQuestCompleted(
    private val recordProductEvent: RecordProductEvent,
) {
    operator fun invoke(questId: String) {
        recordProductEvent { telemetry, occurredAt -> telemetry.questCompleted(questId, occurredAt) }
    }
}
