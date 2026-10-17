package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.exploration.domain.ExplorationRepository

/** Removes the battle unit from the party, when it is in it, e.g.: when it is defeated. The next one leads when it was leading. */
class LeaveParty(
    private val explorationRepository: ExplorationRepository,
) {
    operator fun invoke(battleUnitId: String) {
        val exploration = explorationRepository.search() ?: return
        if (battleUnitId !in exploration.toDto().battleUnitIds) return
        explorationRepository.save(exploration.leave(battleUnitId))
    }
}
