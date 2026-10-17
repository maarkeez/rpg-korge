package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.BattlefieldRepository

/** Whether the terrain of the tile can be occupied (e.g.: not void), whether a battle unit stands on it or not. */
class CanBattlefieldTerrainBeOccupied(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(
        row: Int,
        column: Int,
    ): Boolean {
        val battlefield = battlefieldRepository.search() ?: return false
        return battlefield.isInBoundaries(row, column) && battlefield.canTerrainBeOccupied(row, column)
    }
}
