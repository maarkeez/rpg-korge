package com.mkz.tessavar.cpuBattleBrain.adapters.events

import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.tessavar.cpuBattleBrain.usecases.commands.MakeNextDecision
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnCpuBattleUnitDecisionResolved(
    makeNextDecision: MakeNextDecision,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<CpuBattleBrainEvent.CpuBattleUnitDecisionResolved> { event ->
            makeNextDecision(playerId = event.playerId)
        }
}
