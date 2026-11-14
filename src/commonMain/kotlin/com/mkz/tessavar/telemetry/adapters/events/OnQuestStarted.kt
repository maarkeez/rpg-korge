package com.mkz.tessavar.telemetry.adapters.events

import com.mkz.tessavar.quest.domain.QuestEvent.QuestStarted
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.telemetry.usecases.commands.RecordQuestStarted

class OnQuestStarted(
    recordQuestStarted: RecordQuestStarted,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<QuestStarted> { event ->
            recordQuestStarted(questId = event.questId)
        }
}
