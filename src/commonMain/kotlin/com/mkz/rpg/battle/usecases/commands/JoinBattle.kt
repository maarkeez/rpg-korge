package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.BattleRepository

/** Adds the battle unit, of the player, to the battle, when there is one, e.g.: when it is deployed during the battle. */
class JoinBattle(
    private val battleRepository: BattleRepository,
) {
    operator fun invoke(
        battleUnitId: String,
        playerId: String,
    ) {
        val storedBattle = battleRepository.search() ?: return
        battleRepository.update(storedBattle.join(battleUnitId, playerId))
    }
}
