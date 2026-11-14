package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.usecases.commands.ApplyEffect
import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnTerrainEffectApplied(
    applyEffect: ApplyEffect,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattlefieldEvent.TerrainEffectApplied> { event ->
            applyEffect(event.application)
        }
}
