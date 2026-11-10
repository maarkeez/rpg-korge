package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.party.usecases.queries.SearchParty
import com.mkz.rpg.quest.domain.Quest.Dto.PositionDto
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress

class StartMainQuest(
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
        questRepository.searchAll().filter { quest -> quest.canBeStartedOn(path) }.forEach { quest -> publishQuestProgress(quest.start()) }
    }
}
