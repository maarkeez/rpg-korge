package com.mkz.rpg.building.adapters.events

import com.mkz.rpg.building.domain.BuildingEvent
import com.mkz.rpg.building.usecases.commands.InitialiseBuildings
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestInitialiseBuildings(
    initialiseBuildings: InitialiseBuildings,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BuildingEvent.RequestInitialiseBuildings> {
            initialiseBuildings()
        }
}
