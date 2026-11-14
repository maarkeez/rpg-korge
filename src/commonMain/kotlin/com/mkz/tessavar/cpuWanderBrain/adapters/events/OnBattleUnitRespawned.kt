package com.mkz.tessavar.cpuWanderBrain.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.ResolveRespawn
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnBattleUnitRespawned(
    resolveRespawn: ResolveRespawn,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitRespawned> { event ->
            resolveRespawn(battleUnitId = event.battleUnitId)
        }
}
