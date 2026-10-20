package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.cpuBattleBrain.usecases.commands.EngageEnemies
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitsWanderedWhileExploring(
    engageEnemies: EngageEnemies,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<CpuWanderBrainEvent.BattleUnitsWanderedWhileExploring> { event ->
            engageEnemies(exploringPlayerId = event.exploringPlayerId)
        }
}
