package com.mkz.rpg.wandering.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.wandering.usecases.commands.ResolveRespawn

class OnBattleUnitRespawned(
    resolveRespawn: ResolveRespawn,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitRespawned> { event ->
            resolveRespawn(battleUnitId = event.battleUnitId)
        }
}
