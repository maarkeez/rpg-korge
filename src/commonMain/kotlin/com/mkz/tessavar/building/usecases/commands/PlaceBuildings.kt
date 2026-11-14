package com.mkz.tessavar.building.usecases.commands

import com.mkz.tessavar.battlefield.usecases.queries.IsTileVacant
import com.mkz.tessavar.building.domain.Building
import com.mkz.tessavar.building.domain.BuildingError.BuildingTileNotVacant
import com.mkz.tessavar.building.domain.BuildingRepository
import com.mkz.tessavar.building.usecases.services.BuildingLoader
import com.mkz.tessavar.shared.domain.DomainEvent
import com.mkz.tessavar.shared.domain.EventBus

class PlaceBuildings(
    private val buildingLoader: BuildingLoader,
    private val buildingRepository: BuildingRepository,
    private val isTileVacant: IsTileVacant,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val events = mutableSetOf<DomainEvent>()
        buildingLoader.loadBuildings().forEach { buildingDto ->
            if (buildingRepository.searchById(buildingDto.id) != null) return@forEach
            val (buildingEvents, building) = Building.place(buildingDto).pullEvents()
            if (!areTilesVacant(buildingDto)) throw BuildingTileNotVacant()
            buildingRepository.save(building)
            events += buildingEvents
        }
        eventBus.publish(events)
    }

    private fun areTilesVacant(building: Building.Dto): Boolean =
        (building.row until building.row + building.rows).all { row ->
            (building.column until building.column + building.columns).all { column -> isTileVacant(row, column) }
        }
}
