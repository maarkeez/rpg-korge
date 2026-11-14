package com.mkz.tessavar.battle.usecases.commands

import com.mkz.tessavar.battle.domain.BattleRepository

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
