package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.usecases.commands.ApplyTerrainEffects
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestTerrainEffects(
    private val applyTerrainEffects: ApplyTerrainEffects,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattlefieldEvent.RequestTerrainEffects> { event ->
            applyTerrainEffects(event.occupantIds)
        }
}
