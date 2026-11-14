package com.mkz.tessavar.cpuBattleBrain.usecases.commands

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.tessavar.cpuBattleBrain.usecases.queries.SearchEnemiesWithinReach
import com.mkz.tessavar.player.usecases.queries.SearchHumanPlayer
import com.mkz.tessavar.player.usecases.queries.SearchOpponent
import com.mkz.tessavar.shared.domain.EventBus

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
