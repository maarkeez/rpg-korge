package com.mkz.rpg.cpuBrain.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent
import com.mkz.rpg.cpuBrain.usecases.queries.SearchEnemiesWithinReach
import com.mkz.rpg.player.usecases.queries.SearchEnemyPlayer
import com.mkz.rpg.shared.domain.EventBus

/**
 * Engages the exploring player when any CPU battle unit has one of their battle units within reach, with the same rule
 * the CPU battle units engage enemies in a battle. Otherwise, the CPU battle units stand still.
 */
class EngageEnemies(
    private val searchEnemyPlayer: SearchEnemyPlayer,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val searchEnemiesWithinReach: SearchEnemiesWithinReach,
    private val eventBus: EventBus,
) {
    operator fun invoke(exploringPlayerId: String) {
        val cpuPlayer = searchEnemyPlayer(playerId = exploringPlayerId) ?: return
        val engagingBattleUnitIds =
            searchBattleUnitsByPlayerId(playerId = cpuPlayer.id)
                .map { battleUnit -> battleUnit.id }
                .filter { battleUnitId -> searchEnemiesWithinReach(battleUnitId = battleUnitId).isNotEmpty() }
        if (engagingBattleUnitIds.isEmpty()) return
        eventBus.publish(CpuBrainEvent.CombatEngaged(playerId = cpuPlayer.id, engagedPlayerId = exploringPlayerId, battleUnitIds = engagingBattleUnitIds))
    }
}
