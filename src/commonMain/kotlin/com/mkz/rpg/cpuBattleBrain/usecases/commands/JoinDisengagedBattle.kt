package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.cpuBattleBrain.usecases.queries.SearchEnemiesWithinReach
import com.mkz.rpg.player.usecases.queries.SearchHumanPlayer
import com.mkz.rpg.player.usecases.queries.SearchOpponent
import com.mkz.rpg.shared.domain.EventBus

/**
 * Once the battle is disengaged during a round (e.g.: the last CPU battle unit in the battle was defeated), the CPU battle units
 * out of the battle with an enemy within reach join it, as when they join and leave the battle after a round, but without
 * wandering first, so the battle goes on. They play from the next round. Nobody leaves it. The battle is requested to
 * finish right after, unless some join it.
 * Battle units: those left in the battle.
 */
class JoinDisengagedBattle(
    private val searchHumanPlayer: SearchHumanPlayer,
    private val searchOpponent: SearchOpponent,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val searchEnemiesWithinReach: SearchEnemiesWithinReach,
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitIds: Set<String>) {
        val cpuPlayer = searchHumanPlayer()?.let { humanPlayer -> searchOpponent(playerId = humanPlayer.id) }
        val joiningBattleUnitIds =
            cpuPlayer
                ?.let { searchBattleUnitsByPlayerId(playerId = it.id) }
                .orEmpty()
                .map { battleUnit -> battleUnit.id }
                .filter { battleUnitId -> battleUnitId !in battleUnitIds && searchEnemiesWithinReach(battleUnitId = battleUnitId).isNotEmpty() }
        eventBus.publish(
            setOf(
                CpuBattleBrainEvent.CpuBattleUnitsJoinedAndLeft(playerId = cpuPlayer?.id, joiningBattleUnitIds = joiningBattleUnitIds, leavingBattleUnitIds = emptyList()),
                BattleEvent.RequestResolveDisengagedBattle(joiningPlayerId = cpuPlayer?.id, joiningBattleUnitIds = joiningBattleUnitIds),
            ),
        )
    }
}
