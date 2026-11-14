package com.mkz.tessavar.quest.usecases.commands

import com.mkz.tessavar.quest.domain.QuestError.QuestNotFound
import com.mkz.tessavar.quest.domain.QuestRepository
import com.mkz.tessavar.quest.usecases.services.PublishQuestProgress

class AbandonQuest(
    private val questRepository: QuestRepository,
    private val publishQuestProgress: PublishQuestProgress,
) {
    operator fun invoke(questId: String) {
        val quest = questRepository.searchById(questId) ?: throw QuestNotFound()
        publishQuestProgress(quest.abandon())
    }
}
