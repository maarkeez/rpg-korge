package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.shared.domain.EventBus

class StartNextRound(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        joiningPlayerId: String?,
        joiningBattleUnitIds: List<String>,
        leavingBattleUnitIds: List<String>,
    ) {
        val storedBattle = battleRepository.search() ?: return
        if (storedBattle.toDto().isFinished) return
        val joinedBattle =
            joiningPlayerId
                ?.let { playerId -> joiningBattleUnitIds.fold(storedBattle) { battle, battleUnitId -> battle.join(battleUnitId, playerId) } }
                ?: storedBattle
        val updatedBattle = leavingBattleUnitIds.fold(joinedBattle) { battle, battleUnitId -> battle.leave(battleUnitId) }
        val nextBattle = if (updatedBattle.isDisengaged()) updatedBattle.finish() else updatedBattle.startNextRound()
        val (events, battle) = nextBattle.pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
