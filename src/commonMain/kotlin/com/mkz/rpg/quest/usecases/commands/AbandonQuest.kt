package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.quest.domain.QuestError.QuestNotFound
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress

/** Abandons a side quest, so its giver can start it again from its first task, even when it is not repeatable. */
class AbandonQuest(
    private val questRepository: QuestRepository,
    private val publishQuestProgress: PublishQuestProgress,
) {
    operator fun invoke(questId: String) {
        val quest = questRepository.searchById(questId) ?: throw QuestNotFound()
        publishQuestProgress(quest.abandon())
    }
}
