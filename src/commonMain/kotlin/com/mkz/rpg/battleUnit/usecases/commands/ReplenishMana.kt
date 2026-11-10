package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

class ReplenishMana(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(
        playerId: String,
        battleUnitIds: Set<String>,
    ) {
        battleUnitRepository
            .searchByPlayerId(playerId)
            .filter { battleUnit -> battleUnit.toDto().id in battleUnitIds }
            .map { battleUnit -> battleUnit.replenishMana() }
            .forEach(battleUnitRepository::update)
    }
}
