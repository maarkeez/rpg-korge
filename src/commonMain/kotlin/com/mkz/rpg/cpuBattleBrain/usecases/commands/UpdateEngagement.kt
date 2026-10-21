package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.cpuBattleBrain.usecases.queries.SearchEnemiesWithinReach
import com.mkz.rpg.player.usecases.queries.SearchEnemyPlayer
import com.mkz.rpg.player.usecases.queries.SearchHumanPlayer
import com.mkz.rpg.shared.domain.EventBus

/**
 * Once a battle round finishes and the CPU battle units out of the battle have wandered, those that now have an enemy
 * within reach join it, so they play from the next round, and the CPU battle units in the battle without any enemy
 * within reach leave it (e.g.: left behind), but the summons, which never leave it. The engagement is updated even when nobody joins nor leaves, as the next round starts on it.
 * The next round is requested to start right after, on it.
 * Battle units: those in the battle.
 */
class UpdateEngagement(
    private val searchHumanPlayer: SearchHumanPlayer,
    private val searchEnemyPlayer: SearchEnemyPlayer,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val searchEnemiesWithinReach: SearchEnemiesWithinReach,
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitIds: Set<String>) {
        val cpuPlayer = searchHumanPlayer()?.let { humanPlayer -> searchEnemyPlayer(playerId = humanPlayer.id) }
        val (cpuBattleUnitsInBattle, cpuBattleUnitsOutOfBattle) =
            cpuPlayer
                ?.let { searchBattleUnitsByPlayerId(playerId = it.id) }
                .orEmpty()
                .partition { battleUnit -> battleUnit.id in battleUnitIds }
        val joiningBattleUnitIds = cpuBattleUnitsOutOfBattle.map { battleUnit -> battleUnit.id }.filter { battleUnitId -> hasEnemyWithinReach(battleUnitId) }
        val leavingBattleUnitIds =
            cpuBattleUnitsInBattle
                .filter { battleUnit -> battleUnit.remainingTurns == null }
                .map { battleUnit -> battleUnit.id }
                .filterNot { battleUnitId -> hasEnemyWithinReach(battleUnitId) }
        eventBus.publish(
            setOf(
                CpuBattleBrainEvent.EngagementUpdated(playerId = cpuPlayer?.id, joiningBattleUnitIds = joiningBattleUnitIds, leavingBattleUnitIds = leavingBattleUnitIds),
                BattleEvent.RequestStartNextRound(joiningPlayerId = cpuPlayer?.id, joiningBattleUnitIds = joiningBattleUnitIds, leavingBattleUnitIds = leavingBattleUnitIds),
            ),
        )
    }

    private fun hasEnemyWithinReach(battleUnitId: String) = searchEnemiesWithinReach(battleUnitId = battleUnitId).isNotEmpty()
}
