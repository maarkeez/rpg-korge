package com.mkz.rpg.cpuBattleBrain.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface CpuBattleBrainEvent : DomainEvent {
    data class CpuBattleBrainInitialized(
        val playerId: String,
    ) : CpuBattleBrainEvent

    data class CpuBattleUnitActionResolved(
        val playerId: String,
        val battleUnitId: String,
    ) : CpuBattleBrainEvent

    data class CpuBattleUnitManaged(
        val playerId: String,
        val battleUnitId: String,
    ) : CpuBattleBrainEvent

    /**
     * Battle units: the CPU battle units with an enemy of the engaged player within reach, which engage it.
     * Engaged battle units: every living battle unit of the engaged player.
     */
    data class CombatEngaged(
        val playerId: String,
        val engagedPlayerId: String,
        val battleUnitIds: List<String>,
        val engagedBattleUnitIds: List<String>,
    ) : CpuBattleBrainEvent

    /**
     * Joining battle units: the CPU battle units out of the battle that now have an enemy within reach, so they join it.
     * Leaving battle units: the CPU battle units in the battle without any enemy within reach, so they leave it.
     * Player: the CPU player, whose battle units join and leave, null when there is none.
     */
    data class EngagementUpdated(
        val playerId: String?,
        val joiningBattleUnitIds: List<String>,
        val leavingBattleUnitIds: List<String>,
    ) : CpuBattleBrainEvent

    data class CpuTurnFinished(
        val playerId: String,
    ) : CpuBattleBrainEvent
}
