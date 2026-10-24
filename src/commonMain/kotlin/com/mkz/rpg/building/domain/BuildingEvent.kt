package com.mkz.rpg.building.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface BuildingEvent : DomainEvent {
    // Row and column: its top-left tile.
    data class BuildingPlaced(
        val buildingId: String,
        val row: Int,
        val column: Int,
    ) : BuildingEvent

    object RequestInitialiseBuildings : BuildingEvent
}
