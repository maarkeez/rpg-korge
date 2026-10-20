package com.mkz.rpg.cpuWanderBrain.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.cpuWanderBrain.usecases.commands.ResolveRespawn
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitRespawned(
    resolveRespawn: ResolveRespawn,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitRespawned> { event ->
            resolveRespawn(battleUnitId = event.battleUnitId)
        }
}
