package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldError
import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.usecases.queries.IsTransitionAllowed
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById

class InitializeBattlefield(
    private val battlefieldRepository: BattlefieldRepository,
    private val isTransitionAllowed: IsTransitionAllowed,
    private val searchTerrainById: SearchTerrainById,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        rows: Int,
        columns: Int,
        tiles: List<List<String>>,
    ) {
        if (battlefieldRepository.search() != null) return
        validateTileTerrainTransitions(tiles)
        val (events, battlefield) = Battlefield.create(rows, columns, tiles, terrainOccupancy(tiles)).pullEvents()
        battlefieldRepository.create(battlefield)
        eventBus.publish(events)
    }

    private fun terrainOccupancy(tiles: List<List<String>>): Map<String, Boolean> =
        tiles
            .flatten()
            .distinct()
            .associateWith { terrainId -> searchTerrainById(terrainId)?.canBeOccupied ?: false }

    private fun validateTileTerrainTransitions(tiles: List<List<String>>) {
        tiles.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { columnIndex, terrainId ->
                adjacentTerrainIds(tiles, rowIndex, columnIndex).forEach { adjacentTerrainId ->
                    if (!isTransitionAllowed(terrainId, adjacentTerrainId)) {
                        throw BattlefieldError.TileTerrainTransitionNotAllowed(terrainId, adjacentTerrainId)
                    }
                }
            }
        }
    }

    private fun adjacentTerrainIds(
        tiles: List<List<String>>,
        rowIndex: Int,
        columnIndex: Int,
    ) = listOfNotNull(
        tiles.getOrNull(rowIndex - 1)?.getOrNull(columnIndex),
        tiles.getOrNull(rowIndex)?.getOrNull(columnIndex + 1),
        tiles.getOrNull(rowIndex + 1)?.getOrNull(columnIndex),
        tiles.getOrNull(rowIndex)?.getOrNull(columnIndex - 1),
    )
}
