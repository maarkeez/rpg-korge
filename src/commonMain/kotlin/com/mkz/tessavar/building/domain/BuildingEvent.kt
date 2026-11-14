package com.mkz.tessavar.building.domain

import com.mkz.tessavar.shared.domain.DomainEvent

sealed interface BuildingEvent : DomainEvent {
    data class BuildingPlaced(
        val buildingId: String,
        val row: Int,
        val column: Int,
    ) : BuildingEvent

    object RequestPlaceBuildings : BuildingEvent
}
