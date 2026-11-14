package com.mkz.tessavar.quest.usecases.commands

import com.mkz.tessavar.party.usecases.queries.SearchParty
import com.mkz.tessavar.quest.domain.QuestRepository
import com.mkz.tessavar.quest.usecases.services.PublishQuestProgress

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
