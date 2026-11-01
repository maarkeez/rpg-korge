package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.QuestError.MainQuestNotUnique
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.shared.domain.EventBus

/** Starts the main quest, once a new game is set up. */
class StartMainQuest(
    private val questRepository: QuestRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val mainQuest = questRepository.searchAll().firstOrNull { quest -> quest.toDto().type == QuestTypeDto.MAIN } ?: throw MainQuestNotUnique()
        val (events, startedQuest) = mainQuest.start().pullEvents()
        questRepository.save(startedQuest)
        eventBus.publish(events)
    }
}
