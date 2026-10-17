package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.shared.domain.EventBus

/**
 * Starts the next round, once the battle units joining the battle (e.g.: the CPU battle units engaging) have joined it,
 * and those leaving it (e.g.: the CPU battle units left behind) have left it. The battle finishes without a winner
 * instead, when only one player is left with battle units in it.
 */
class StartNextRound(
    private val searchBattleUnitById: SearchBattleUnitById,
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        joiningBattleUnitIds: List<String>,
        leavingBattleUnitIds: List<String>,
    ) {
        val storedBattle = battleRepository.search() ?: return
        val joinedBattle = joiningBattleUnitIds.fold(storedBattle) { battle, battleUnitId -> battle.join(battleUnitId) }
        val updatedBattle = leavingBattleUnitIds.fold(joinedBattle) { battle, battleUnitId -> battle.leave(battleUnitId) }
        val playersInBattle =
            updatedBattle
                .toDto()
                .battleUnitIds
                .mapNotNull { battleUnitId -> searchBattleUnitById(battleUnitId)?.playerId }
                .toSet()
        val nextBattle = if (updatedBattle.isDisengaged(playersInBattle)) updatedBattle.finishWithoutWinner() else updatedBattle.startNextRound()
        val (events, battle) = nextBattle.pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
