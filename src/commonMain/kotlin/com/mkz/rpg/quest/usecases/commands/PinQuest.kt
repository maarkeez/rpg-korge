package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.QuestError.MainQuestNotUnique
import com.mkz.rpg.quest.domain.QuestError.QuestNotFound
import com.mkz.rpg.quest.domain.QuestEvent.QuestPinned
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress
import com.mkz.rpg.shared.domain.EventBus

class PinQuest(
    private val questRepository: QuestRepository,
    private val publishQuestProgress: PublishQuestProgress,
    private val eventBus: EventBus,
) {
    operator fun invoke(questId: String) {
        val quest = questRepository.searchById(questId) ?: throw QuestNotFound()
        val dto = quest.toDto()
        if (dto.type == QuestTypeDto.MAIN || dto.progress.isPinned) {
            unpinEveryQuest()
            eventBus.publish(QuestPinned(questId = mainQuest().toDto().id))
        } else {
            val pinnedQuest = quest.pin()
            unpinEveryQuest()
            publishQuestProgress(pinnedQuest)
        }
    }

    private fun unpinEveryQuest() = questRepository.searchAll().filter { quest -> quest.toDto().progress.isPinned }.forEach { quest -> questRepository.save(quest.unpin()) }

    private fun mainQuest(): Quest = questRepository.searchAll().firstOrNull { quest -> quest.toDto().type == QuestTypeDto.MAIN } ?: throw MainQuestNotUnique()
}
