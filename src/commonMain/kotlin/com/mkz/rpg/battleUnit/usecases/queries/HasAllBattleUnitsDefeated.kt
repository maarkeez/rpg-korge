package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

class HasAllBattleUnitsDefeated(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(playerId: String): Boolean = battleUnitRepository.searchByPlayerId(playerId).all(BattleUnit::isDefeated)
}
