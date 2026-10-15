package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

class ReplenishMana(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(playerId: String) {
        battleUnitRepository
            .searchByPlayerId(playerId)
            .map { battleUnit -> battleUnit.replenishMana() }
            .forEach(battleUnitRepository::update)
    }
}
