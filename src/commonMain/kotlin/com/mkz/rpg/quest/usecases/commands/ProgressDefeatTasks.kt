package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.party.usecases.queries.SearchParty
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress

class ProgressDefeatTasks(
    private val questRepository: QuestRepository,
    private val searchParty: SearchParty,
    private val publishQuestProgress: PublishQuestProgress,
) {
    operator fun invoke(
        playerId: String,
        unitId: String,
    ) {
        val party = searchParty() ?: return
        if (party.playerId == playerId) return
        questRepository.searchAll().forEach { quest -> publishQuestProgress(quest.defeated(unitId)) }
    }
}
