package com.mkz.rpg.telemetry.adapters.events

import com.mkz.rpg.quest.domain.QuestEvent.QuestCompleted
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.telemetry.usecases.commands.RecordQuestCompleted

class OnQuestCompleted(
    recordQuestCompleted: RecordQuestCompleted,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<QuestCompleted> { event ->
            recordQuestCompleted(questId = event.questId)
        }
}
