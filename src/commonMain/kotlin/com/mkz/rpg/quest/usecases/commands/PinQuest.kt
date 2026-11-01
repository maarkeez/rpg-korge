package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.QuestError.MainQuestNotUnique
import com.mkz.rpg.quest.domain.QuestError.QuestNotFound
import com.mkz.rpg.quest.domain.QuestEvent.QuestPinned
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress
import com.mkz.rpg.shared.domain.EventBus

/**
 * Makes the quest the one the player follows, unpinning any other, so exactly one is followed. The main quest is followed
 * when no other quest is pinned, so it holds no pin: pinning it, or a quest already pinned, returns to the main quest.
 */
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
