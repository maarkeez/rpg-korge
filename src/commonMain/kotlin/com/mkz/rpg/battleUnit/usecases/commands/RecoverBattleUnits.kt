package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

/** Each battle unit recovers the percentage of its maximum health and mana, but the defeated ones. */
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
