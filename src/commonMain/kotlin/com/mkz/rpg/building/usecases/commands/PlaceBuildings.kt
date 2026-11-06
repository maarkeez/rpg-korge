package com.mkz.rpg.building.usecases.commands

import com.mkz.rpg.battlefield.usecases.queries.IsTileVacant
import com.mkz.rpg.building.domain.Building
import com.mkz.rpg.building.domain.BuildingError.BuildingTileNotVacant
import com.mkz.rpg.building.domain.BuildingRepository
import com.mkz.rpg.building.usecases.services.BuildingLoader
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.EventBus

/**
 * Places every loaded building on its tiles, unless it is already placed, failing when any of its tiles is not
 * vacant (e.g.: void, out of the battlefield). Each building requests the battlefield to occupy its tiles, right
 * after `BuildingPlaced`.
 */
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
