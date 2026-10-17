package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.cpuBrain.usecases.commands.EngageEnemies
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.wandering.domain.WanderingEvent

class OnBattleUnitsWanderedWhileExploring(
    engageEnemies: EngageEnemies,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<WanderingEvent.BattleUnitsWanderedWhileExploring> { event ->
            engageEnemies(exploringPlayerId = event.exploringPlayerId)
        }
}
