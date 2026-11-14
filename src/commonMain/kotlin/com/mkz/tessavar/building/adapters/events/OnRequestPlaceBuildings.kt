package com.mkz.tessavar.building.adapters.events

import com.mkz.tessavar.building.domain.BuildingEvent
import com.mkz.tessavar.building.usecases.commands.PlaceBuildings
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestPlaceBuildings(
    placeBuildings: PlaceBuildings,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BuildingEvent.RequestPlaceBuildings> {
            placeBuildings()
        }
}
