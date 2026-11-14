package com.mkz.tessavar.terrain.adapters.events

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.terrain.domain.TerrainEvent
import com.mkz.tessavar.terrain.usecases.commands.InitialiseTerrains

class OnRequestInitialiseTerrains(
    initialiseTerrains: InitialiseTerrains,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<TerrainEvent.RequestInitialiseTerrains> {
            initialiseTerrains()
        }
}
