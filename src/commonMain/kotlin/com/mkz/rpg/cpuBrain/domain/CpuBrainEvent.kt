package com.mkz.rpg.cpuBrain.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface CpuBrainEvent : DomainEvent {
    data class CpuBrainInitialized(
        val playerId: String,
    ) : CpuBrainEvent

    data class CpuBattleUnitActionResolved(
        val playerId: String,
        val battleUnitId: String,
    ) : CpuBrainEvent

    data class CpuBattleUnitManaged(
        val playerId: String,
        val battleUnitId: String,
    ) : CpuBrainEvent

    data class CpuTurnFinished(
        val playerId: String,
    ) : CpuBrainEvent
}
