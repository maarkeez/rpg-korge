package com.mkz.rpg.telemetry.adapters.events

import com.mkz.rpg.quest.domain.QuestEvent.QuestStarted
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.telemetry.usecases.commands.RecordQuestStarted

class OnQuestStarted(
    recordQuestStarted: RecordQuestStarted,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<QuestStarted> { event ->
            recordQuestStarted(questId = event.questId)
        }
}
