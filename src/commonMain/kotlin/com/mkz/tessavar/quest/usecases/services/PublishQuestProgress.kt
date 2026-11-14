package com.mkz.tessavar.quest.usecases.services

import com.mkz.tessavar.quest.domain.Quest
import com.mkz.tessavar.quest.domain.QuestRepository
import com.mkz.tessavar.shared.domain.EventBus

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
