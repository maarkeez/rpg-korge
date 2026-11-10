package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

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
            .map(BattleUnit::reduceCooldowns)
            .forEach(battleUnitRepository::update)
    }
}
