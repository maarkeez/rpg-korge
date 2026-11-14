package com.mkz.tessavar.telemetry.adapters.events

import com.mkz.tessavar.quest.domain.QuestEvent.QuestCompleted
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.telemetry.usecases.commands.RecordQuestCompleted

class OnQuestCompleted(
    recordQuestCompleted: RecordQuestCompleted,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<QuestCompleted> { event ->
            recordQuestCompleted(questId = event.questId)
        }
}
