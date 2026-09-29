package com.mkz.rpg.terrain.adapters.events

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.terrain.usecases.commands.InitialiseTerrains

class OnRequestInitialiseTerrains(
    initialiseTerrains: InitialiseTerrains,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<TerrainEvent.RequestInitialiseTerrains> {
            initialiseTerrains()
        }
}
