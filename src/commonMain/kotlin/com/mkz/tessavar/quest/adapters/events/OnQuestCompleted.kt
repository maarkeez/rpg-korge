package com.mkz.tessavar.quest.adapters.events

import com.mkz.tessavar.quest.domain.QuestEvent.QuestCompleted
import com.mkz.tessavar.quest.usecases.commands.RequestQuestReward
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnQuestCompleted(
    requestQuestReward: RequestQuestReward,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<QuestCompleted> { event ->
            requestQuestReward(questId = event.questId)
        }
}
