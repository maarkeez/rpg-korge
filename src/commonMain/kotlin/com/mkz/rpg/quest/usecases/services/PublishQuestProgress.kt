package com.mkz.rpg.quest.usecases.services

import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.shared.domain.EventBus

class PublishQuestProgress(
    private val questRepository: QuestRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(quest: Quest) {
        val (events, updatedQuest) = quest.pullEvents()
        if (events.isEmpty()) return
        questRepository.save(updatedQuest)
        eventBus.publish(events)
    }
}
