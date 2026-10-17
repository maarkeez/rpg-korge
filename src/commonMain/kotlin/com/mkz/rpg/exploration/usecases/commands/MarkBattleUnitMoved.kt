package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.exploration.domain.ExplorationRepository

/** Remembers that the battle unit moved during the current second while exploring, so it does not receive the effect of its terrain. */
class MarkBattleUnitMoved(
    private val explorationRepository: ExplorationRepository,
) {
    operator fun invoke(battleUnitId: String) {
        val exploration = explorationRepository.search() ?: return
        explorationRepository.save(exploration.markMoved(battleUnitId))
    }
}
