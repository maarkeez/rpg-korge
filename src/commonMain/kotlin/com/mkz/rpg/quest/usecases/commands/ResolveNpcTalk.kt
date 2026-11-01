package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress

/**
 * Once the party finishes talking to an npc, completes the current task of every quest asking for it, and starts the side quests it gives. The
 * quests to start are listed first and started last: so the talk that starts a quest does not complete its first task, and the talk that
 * completes a repeatable quest does not restart it.
 */
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
