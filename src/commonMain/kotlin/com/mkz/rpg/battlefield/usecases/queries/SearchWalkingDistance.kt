package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto

class SearchWalkingDistance(
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
    private val canBattlefieldTerrainBeOccupied: CanBattlefieldTerrainBeOccupied,
) {
    /**
     * Returns how many tiles are walked from the origin to the destination, stepping only on tiles that can be occupied,
     * but the destination, which may be occupied (e.g.: by the battle unit walked to). Returns null when the destination
     * is further than [maximumDistance] or can not be reached.
     *
     * When walking [throughBattleUnits], only the terrain blocks the walk (e.g.: void), not the battle units standing on it.
     */
    operator fun invoke(
        fromRow: Int,
        fromColumn: Int,
        toRow: Int,
        toColumn: Int,
        maximumDistance: Int,
        throughBattleUnits: Boolean = false,
    ): Int? {
        val origin = PositionDto(fromRow, fromColumn)
        val destination = PositionDto(toRow, toColumn)
        if (origin == destination) return 0
        val distances = mutableMapOf(origin to 0)
        val queue = ArrayDeque(listOf(origin))
        while (queue.isNotEmpty()) {
            val currentPosition = queue.removeFirst()
            val distance = distances.getValue(currentPosition) + 1
            // The tiles are walked in distance order, so the next ones are not nearer either.
            if (distance > maximumDistance) return null
            for (nextPosition in adjacentPositions(currentPosition)) {
                if (nextPosition == destination) return distance
                if (nextPosition !in distances && canBeWalked(nextPosition, throughBattleUnits)) {
                    distances[nextPosition] = distance
                    queue.addLast(nextPosition)
                }
            }
        }
        return null
    }

    private fun canBeWalked(
        position: PositionDto,
        throughBattleUnits: Boolean,
    ): Boolean =
        if (throughBattleUnits) {
            canBattlefieldTerrainBeOccupied(position.row, position.column)
        } else {
            canBattlefieldTileBeOccupied(position.row, position.column)
        }

    private fun adjacentPositions(position: PositionDto): List<PositionDto> =
        listOf(
            PositionDto(position.row - 1, position.column),
            PositionDto(position.row + 1, position.column),
            PositionDto(position.row, position.column - 1),
            PositionDto(position.row, position.column + 1),
        )
}
