package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.party.usecases.queries.SearchParty
import com.mkz.rpg.quest.domain.Quest.Dto.PositionDto
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress

/** Completes the current task of every quest asking for a tile of the path, when a party member walked or teleported it. A summon is no party member. */
class ProgressReachTasks(
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
        questRepository.searchAll().forEach { quest -> publishQuestProgress(quest.reached(path)) }
    }
}
