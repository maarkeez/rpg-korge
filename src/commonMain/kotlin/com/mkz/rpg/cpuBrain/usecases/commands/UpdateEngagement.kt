package com.mkz.rpg.cpuBrain.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent
import com.mkz.rpg.cpuBrain.usecases.queries.SearchEnemiesWithinReach
import com.mkz.rpg.player.usecases.queries.SearchEnemyPlayer
import com.mkz.rpg.player.usecases.queries.SearchHumanPlayer
import com.mkz.rpg.shared.domain.EventBus

/**
 * Once a battle round finishes, the CPU battle units out of the battle that now have an enemy within reach join it, so
 * they play from the next round. The engagement is updated even when nobody joins, as the next round starts on it.
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
        val joiningBattleUnitIds =
            cpuPlayer
                ?.let { searchBattleUnitsByPlayerId(playerId = it.id) }
                .orEmpty()
                .map { battleUnit -> battleUnit.id }
                .filter { battleUnitId -> battleUnitId !in battleUnitIds }
                .filter { battleUnitId -> searchEnemiesWithinReach(battleUnitId = battleUnitId).isNotEmpty() }
        eventBus.publish(CpuBrainEvent.EngagementUpdated(joiningBattleUnitIds = joiningBattleUnitIds))
    }
}
