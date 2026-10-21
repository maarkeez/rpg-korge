package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.RequestEngageEnemies
import com.mkz.rpg.cpuBattleBrain.usecases.commands.EngageEnemies
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestEngageEnemies(
    engageEnemies: EngageEnemies,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<RequestEngageEnemies> { event ->
            engageEnemies(exploringPlayerId = event.exploringPlayerId)
        }
}
