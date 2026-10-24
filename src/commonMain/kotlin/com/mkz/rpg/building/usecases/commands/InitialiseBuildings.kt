package com.mkz.rpg.building.usecases.commands

import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.building.domain.Building
import com.mkz.rpg.building.domain.BuildingError.BuildingTileCanNotBeOccupied
import com.mkz.rpg.building.domain.BuildingRepository
import com.mkz.rpg.building.usecases.services.BuildingLoader
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.EventBus

/**
 * Places every loaded building on its tiles, unless it is already placed, failing when any of its tiles can not be
 * occupied (e.g.: void, out of the battlefield). Each building requests the battlefield to occupy its tiles, right
 * after `BuildingPlaced`.
 */
class InitialiseBuildings(
    private val buildingLoader: BuildingLoader,
    private val buildingRepository: BuildingRepository,
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val events = mutableSetOf<DomainEvent>()
        buildingLoader.loadBuildings().forEach { buildingDto ->
            if (buildingRepository.searchById(buildingDto.id) != null) return@forEach
            val (buildingEvents, building) = Building.place(buildingDto).pullEvents()
            if (!canTilesBeOccupied(buildingDto)) throw BuildingTileCanNotBeOccupied()
            buildingRepository.save(building)
            events += buildingEvents
        }
        eventBus.publish(events)
    }

    private fun canTilesBeOccupied(building: Building.Dto): Boolean =
        (building.row until building.row + building.rows).all { row ->
            (building.column until building.column + building.columns).all { column -> canBattlefieldTileBeOccupied(row, column) }
        }
}
