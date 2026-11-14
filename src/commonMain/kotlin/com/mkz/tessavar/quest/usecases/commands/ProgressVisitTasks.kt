package com.mkz.tessavar.quest.usecases.commands

import com.mkz.tessavar.party.usecases.queries.SearchParty
import com.mkz.tessavar.quest.domain.Quest.Dto.PositionDto
import com.mkz.tessavar.quest.domain.QuestRepository
import com.mkz.tessavar.quest.usecases.services.PublishQuestProgress

class ProgressVisitTasks(
    private val questRepository: QuestRepository,
    private val searchParty: SearchParty,
    private val publishQuestProgress: PublishQuestProgress,
) {
    operator fun invoke(
        battleUnitId: String,
        path: List<PositionDto>,
    ) {
        val party = searchParty() ?: return
        if (battleUnitId !in party.battleUnitIds) return
        questRepository.searchAll().forEach { quest -> publishQuestProgress(quest.visited(path)) }
    }
}
