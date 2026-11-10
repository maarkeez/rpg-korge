package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.shared.domain.EventBus

class ResolveDisengagedBattle(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        joiningPlayerId: String?,
        joiningBattleUnitIds: List<String>,
    ) {
        val storedBattle = battleRepository.search() ?: return
        if (storedBattle.toDto().isFinished || storedBattle.hasWinner()) return
        val joinedBattle =
            joiningPlayerId
                ?.let { playerId -> joiningBattleUnitIds.fold(storedBattle) { battle, battleUnitId -> battle.join(battleUnitId, playerId) } }
                ?: storedBattle
        val nextBattle = if (joinedBattle.isDisengaged()) joinedBattle.finishWithoutWinner() else joinedBattle
        val (events, battle) = nextBattle.pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
