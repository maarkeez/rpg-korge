package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * Starts the next round, once the battle units joining the battle (e.g.: the CPU battle units engaging) have joined it,
 * and those leaving it (e.g.: the CPU battle units left behind) have left it. The battle finishes without a winner
 * instead, when only one player is left with battle units in it. Nothing starts once the battle is finished (e.g.: the
 * last battle unit of a player vanished as the round finished, which defeated it).
 * Joining player: the player of the battle units joining, null when none joins.
 */
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
        if (storedBattle.toDto().isFinished || storedBattle.isBattleFinished()) return
        val joinedBattle =
            joiningPlayerId
                ?.let { playerId -> joiningBattleUnitIds.fold(storedBattle) { battle, battleUnitId -> battle.join(battleUnitId, playerId) } }
                ?: storedBattle
        val updatedBattle = leavingBattleUnitIds.fold(joinedBattle) { battle, battleUnitId -> battle.leave(battleUnitId) }
        val nextBattle = if (updatedBattle.isDisengaged()) updatedBattle.finishWithoutWinner() else updatedBattle.startNextRound()
        val (events, battle) = nextBattle.pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
