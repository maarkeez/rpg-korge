package com.mkz.tessavar.cpuWanderBrain.domain

import com.mkz.tessavar.shared.domain.DomainEvent

sealed interface CpuWanderBrainEvent : DomainEvent {
    data class CpuBattleUnitsWanderedWhileExploring(
        val exploringPlayerId: String,
    ) : CpuWanderBrainEvent

    data class CpuBattleUnitsWanderedAfterRound(
        val battleUnitIds: Set<String>,
    ) : CpuWanderBrainEvent
}
