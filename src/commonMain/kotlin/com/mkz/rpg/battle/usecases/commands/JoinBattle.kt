package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.BattleRepository

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
