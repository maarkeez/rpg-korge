package com.mkz.tessavar.battle.usecases.commands

import com.mkz.tessavar.battle.domain.BattleRepository
import com.mkz.tessavar.shared.domain.EventBus

class ResolveDisengagedBattle(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        joiningPlayerId: String?,
        joiningBattleUnitIds: List<String>,
    ) {
        val storedBattle = battleRepository.search() ?: return
        if (storedBattle.toDto().isFinished) return
        val joinedBattle =
            joiningPlayerId
                ?.let { playerId -> joiningBattleUnitIds.fold(storedBattle) { battle, battleUnitId -> battle.join(battleUnitId, playerId) } }
                ?: storedBattle
        val nextBattle = if (joinedBattle.isDisengaged()) joinedBattle.finish() else joinedBattle
        val (events, battle) = nextBattle.pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
