package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

class RecoverBattleUnits(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(
        battleUnitIds: List<String>,
        percentage: Int,
    ) {
        battleUnitIds
            .mapNotNull(battleUnitRepository::searchById)
            .map { battleUnit -> battleUnit.recover(percentage) }
            .forEach(battleUnitRepository::update)
    }
}
