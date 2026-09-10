package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldRepository

class SearchTilesThatCanBeOccupied(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(
        battleUnitId: String,
        distance: Int,
    ): List<PositionDto> {
        if (distance <= 0) return emptyList()
        val battlefield = battlefieldRepository.search() ?: return emptyList()
        val position = battlefield.position(battleUnitId) ?: return emptyList()
        val tilesThatCanBeOccupied =
            buildList {
                for (row in position.row - distance..position.row + distance) {
                    for (column in position.column - distance..position.column + distance) {
                        if (battlefield.isInBoundaries(row, column) && battlefield.canBeOccupied(row, column)) {
                            add(PositionDto(row, column))
                        }
                    }
                }
            }
        return tilesThatCanBeOccupied
    }
}
