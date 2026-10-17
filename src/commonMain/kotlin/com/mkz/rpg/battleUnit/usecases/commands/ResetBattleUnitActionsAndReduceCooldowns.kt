package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

/** Battle units: those in the battle. The battle units of the player outside it stay as they are. */
class ResetBattleUnitActionsAndReduceCooldowns(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(
        playerId: String,
        battleUnitIds: Set<String>,
    ) {
        battleUnitRepository
            .searchByPlayerId(playerId)
            .filter { battleUnit -> battleUnit.toDto().id in battleUnitIds }
            .map(BattleUnit::resetActions)
            .map(BattleUnit::reduceCoolDowns)
            .forEach(battleUnitRepository::update)
    }
}
