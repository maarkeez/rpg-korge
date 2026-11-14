package com.mkz.tessavar.quest.usecases.commands

import com.mkz.tessavar.quest.domain.Quest
import com.mkz.tessavar.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.tessavar.quest.domain.QuestError.MainQuestNotUnique
import com.mkz.tessavar.quest.domain.QuestError.QuestNotFound
import com.mkz.tessavar.quest.domain.QuestEvent.QuestPinned
import com.mkz.tessavar.quest.domain.QuestRepository
import com.mkz.tessavar.quest.usecases.services.PublishQuestProgress
import com.mkz.tessavar.shared.domain.EventBus

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
