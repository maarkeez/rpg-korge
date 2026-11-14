package com.mkz.tessavar.telemetry.adapters.events

import com.mkz.tessavar.quest.domain.QuestEvent.QuestAbandoned
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.telemetry.usecases.commands.RecordQuestAbandoned

class OnQuestAbandoned(
    recordQuestAbandoned: RecordQuestAbandoned,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<QuestAbandoned> { event ->
            recordQuestAbandoned(questId = event.questId)
        }
}
