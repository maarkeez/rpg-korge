package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress

/** Completes the current task of every quest asking to talk to the npc, once the party finishes talking to it. */
class ResolveNpcTalk(
    private val questRepository: QuestRepository,
    private val publishQuestProgress: PublishQuestProgress,
) {
    operator fun invoke(npcId: String) {
        questRepository.searchAll().forEach { quest -> publishQuestProgress(quest.talkedTo(npcId)) }
    }
}
