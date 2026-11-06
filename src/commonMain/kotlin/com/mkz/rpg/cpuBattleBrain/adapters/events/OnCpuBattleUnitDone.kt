package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.cpuBattleBrain.usecases.commands.MakeNextDecision
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnCpuBattleUnitDone(
    makeNextDecision: MakeNextDecision,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<CpuBattleBrainEvent.CpuBattleUnitDone> { event ->
            makeNextDecision(playerId = event.playerId)
        }
}
