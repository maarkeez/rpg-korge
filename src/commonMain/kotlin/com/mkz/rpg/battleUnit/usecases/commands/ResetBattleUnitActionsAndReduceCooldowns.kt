package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

class ResetBattleUnitActionsAndReduceCooldowns(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(playerId: String) {
        battleUnitRepository
            .searchByPlayerId(playerId)
            .map(BattleUnit::resetActions)
            .map(BattleUnit::reduceCoolDowns)
            .forEach(battleUnitRepository::update)
    }
}
