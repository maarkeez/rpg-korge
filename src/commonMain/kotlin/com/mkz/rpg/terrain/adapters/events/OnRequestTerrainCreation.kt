package com.mkz.rpg.terrain.adapters.events

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.terrain.usecases.commands.RequestTerrainCreation

class OnRequestTerrainCreation(
    requestTerrainCreation: RequestTerrainCreation,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<TerrainEvent.RequestTerrainCreation> { event ->
            requestTerrainCreation(event.terrainDto)
        }
}
