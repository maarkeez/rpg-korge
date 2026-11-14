package com.mkz.tessavar.cpuBattleBrain.domain

import com.mkz.tessavar.shared.domain.DomainEvent

sealed interface CpuBattleBrainEvent : DomainEvent {
    data class CpuPlayerTurnStarted(
        val playerId: String,
    ) : CpuBattleBrainEvent

    data class CpuBattleUnitDecisionResolved(
        val playerId: String,
        val battleUnitId: String,
    ) : CpuBattleBrainEvent

    data class CpuBattleUnitDone(
        val playerId: String,
        val battleUnitId: String,
    ) : CpuBattleBrainEvent

    data class EnemiesEngaged(
        val playerId: String,
        val engagedPlayerId: String,
        val battleUnitIds: List<String>,
        val engagedBattleUnitIds: List<String>,
    ) : CpuBattleBrainEvent

    data class CpuBattleUnitsJoinedAndLeft(
        val playerId: String?,
        val joiningBattleUnitIds: List<String>,
        val leavingBattleUnitIds: List<String>,
    ) : CpuBattleBrainEvent

    data class RequestEngageEnemies(
        val exploringPlayerId: String,
    ) : CpuBattleBrainEvent

    data class RequestJoinAndLeaveBattle(
        val battleUnitIds: Set<String>,
    ) : CpuBattleBrainEvent

    data class CpuPlayerTurnFinished(
        val playerId: String,
    ) : CpuBattleBrainEvent
}
