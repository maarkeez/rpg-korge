package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress

class ResolveNpcTalk(
    private val questRepository: QuestRepository,
    private val publishQuestProgress: PublishQuestProgress,
) {
    operator fun invoke(npcId: String) {
        val quests = questRepository.searchAll()
        val startableQuests = quests.filter { quest -> quest.canBeStartedBy(npcId) }
        quests.forEach { quest -> publishQuestProgress(quest.talkedTo(npcId)) }
        startableQuests.forEach { quest -> publishQuestProgress(quest.start()) }
    }
}
