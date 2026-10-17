package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.BattleRepository

/** Removes the battle unit from the battle, when there is one, e.g.: when it is defeated. */
class LeaveBattle(
    private val battleRepository: BattleRepository,
) {
    operator fun invoke(battleUnitId: String) {
        val storedBattle = battleRepository.search() ?: return
        battleRepository.update(storedBattle.leave(battleUnitId))
    }
}
