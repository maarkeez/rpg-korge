package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.usecases.commands.TriggerTerrainEffects
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestEffectApplicationToOccupants(
    private val triggerTerrainEffects: TriggerTerrainEffects,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattlefieldEvent.RequestEffectApplicationToOccupants> { event ->
            triggerTerrainEffects(event.battleUnitIds)
        }
}
