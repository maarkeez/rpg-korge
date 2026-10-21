package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.usecases.commands.ApplyEffect
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnTerrainEffectTriggered(
    applyEffect: ApplyEffect,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattlefieldEvent.TerrainEffectTriggered> { event ->
            applyEffect(event.application)
        }
}
