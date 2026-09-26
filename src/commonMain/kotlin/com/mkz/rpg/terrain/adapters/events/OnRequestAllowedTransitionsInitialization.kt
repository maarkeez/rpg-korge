package com.mkz.rpg.terrain.adapters.events

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.terrain.usecases.commands.InitialiseAllowedTransitions

class OnRequestAllowedTransitionsInitialization(
    initialiseAllowedTransitions: InitialiseAllowedTransitions,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<TerrainEvent.RequestAllowedTransitionsInitialization> {
            initialiseAllowedTransitions()
        }
}
