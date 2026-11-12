package com.mkz.rpg.telemetry.adapters.events

import com.mkz.rpg.quest.domain.QuestEvent.QuestAbandoned
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.telemetry.usecases.commands.RecordQuestAbandoned

class OnQuestAbandoned(
    recordQuestAbandoned: RecordQuestAbandoned,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<QuestAbandoned> { event ->
            recordQuestAbandoned(questId = event.questId)
        }
}
