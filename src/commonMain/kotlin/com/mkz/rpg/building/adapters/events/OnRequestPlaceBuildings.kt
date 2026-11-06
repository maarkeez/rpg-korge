package com.mkz.rpg.building.adapters.events

import com.mkz.rpg.building.domain.BuildingEvent
import com.mkz.rpg.building.usecases.commands.PlaceBuildings
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestPlaceBuildings(
    placeBuildings: PlaceBuildings,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BuildingEvent.RequestPlaceBuildings> {
            placeBuildings()
        }
}
