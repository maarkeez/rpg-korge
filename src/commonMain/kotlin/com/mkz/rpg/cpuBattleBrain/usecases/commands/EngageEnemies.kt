package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.cpuBattleBrain.usecases.queries.SearchEnemiesWithinReach
import com.mkz.rpg.player.usecases.queries.SearchOpponent
import com.mkz.rpg.shared.domain.EventBus

class EngageEnemies(
    private val searchOpponent: SearchOpponent,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val searchEnemiesWithinReach: SearchEnemiesWithinReach,
    private val eventBus: EventBus,
) {
    operator fun invoke(exploringPlayerId: String) {
        val cpuPlayer = searchOpponent(playerId = exploringPlayerId) ?: return
        val engagingBattleUnitIds =
            searchBattleUnitsByPlayerId(playerId = cpuPlayer.id)
                .map { battleUnit -> battleUnit.id }
                .filter { battleUnitId -> searchEnemiesWithinReach(battleUnitId = battleUnitId).isNotEmpty() }
        if (engagingBattleUnitIds.isEmpty()) return
        val engagedBattleUnitIds = searchBattleUnitsByPlayerId(playerId = exploringPlayerId).map { battleUnit -> battleUnit.id }
        eventBus.publish(
            setOf(
                CpuBattleBrainEvent.EnemiesEngaged(
                    playerId = cpuPlayer.id,
                    engagedPlayerId = exploringPlayerId,
                    battleUnitIds = engagingBattleUnitIds,
                    engagedBattleUnitIds = engagedBattleUnitIds,
                ),
                BattleEvent.RequestStartBattle(
                    engagingPlayerId = cpuPlayer.id,
                    engagedPlayerId = exploringPlayerId,
                    engagingBattleUnitIds = engagingBattleUnitIds,
                    engagedBattleUnitIds = engagedBattleUnitIds,
                ),
            ),
        )
    }
}
