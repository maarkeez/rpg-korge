package com.mkz.rpg.quest.adapters.events

import com.mkz.rpg.quest.domain.QuestEvent.QuestCompleted
import com.mkz.rpg.quest.usecases.commands.RequestQuestReward
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnQuestCompleted(
    requestQuestReward: RequestQuestReward,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<QuestCompleted> { event ->
            requestQuestReward(questId = event.questId)
        }
}
