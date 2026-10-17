package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.exploration.domain.ExplorationRepository
import com.mkz.rpg.exploration.usecases.services.PartyWalking

/** Lets the party continue walking once the step it awaits has been walked. Other occupied tiles are ignored. */
class ResolvePartyStep(
    private val explorationRepository: ExplorationRepository,
    private val partyWalking: PartyWalking,
) {
    operator fun invoke(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) {
        val exploration = explorationRepository.search() ?: return
        val resolvedExploration = exploration.resolveStep(battleUnitId = battleUnitId, row = row, column = column)
        if (resolvedExploration == exploration) return
        partyWalking.continueWalking(resolvedExploration)
    }
}
