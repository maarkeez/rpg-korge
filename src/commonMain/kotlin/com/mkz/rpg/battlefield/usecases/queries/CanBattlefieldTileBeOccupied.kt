package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById

class CanBattlefieldTileBeOccupied(
    private val searchTerrainById: SearchTerrainById,
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(
        row: Int,
        column: Int,
    ): Boolean {
        val battlefield = battlefieldRepository.search() ?: return false
        val terrain =
            battlefield.toDto().tiles[PositionDto(row, column)]?.terrainId ?.let { terrainId ->
                searchTerrainById(terrainId)
            } ?: return false
        return battlefield.isInBoundaries(row, column) && battlefield.canBeOccupied(row, column) && terrain.canBeOccupied
    }
}
