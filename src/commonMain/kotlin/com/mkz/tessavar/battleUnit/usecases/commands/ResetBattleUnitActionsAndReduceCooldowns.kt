package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnit
import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository

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
