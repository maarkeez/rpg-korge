package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto

class SearchWalkingDistance(
    private val isTileVacant: IsTileVacant,
) {
    operator fun invoke(
        fromRow: Int,
        fromColumn: Int,
        toRow: Int,
        toColumn: Int,
        maximumDistance: Int,
    ): Int? {
        val origin = PositionDto(fromRow, fromColumn)
        val destination = PositionDto(toRow, toColumn)
        if (origin == destination) return 0
        val distances = mutableMapOf(origin to 0)
        val positionsInDistanceOrder = ArrayDeque(listOf(origin))
        while (positionsInDistanceOrder.isNotEmpty()) {
            val currentPosition = positionsInDistanceOrder.removeFirst()
            val distance = distances.getValue(currentPosition) + 1
            if (distance > maximumDistance) return null
            for (nextPosition in adjacentPositions(currentPosition)) {
                if (nextPosition == destination) return distance
                if (nextPosition !in distances && isTileVacant(nextPosition.row, nextPosition.column)) {
                    distances[nextPosition] = distance
                    positionsInDistanceOrder.addLast(nextPosition)
                }
            }
        }
        return null
    }

    private fun adjacentPositions(position: PositionDto): List<PositionDto> =
        listOf(
            PositionDto(position.row - 1, position.column),
            PositionDto(position.row + 1, position.column),
            PositionDto(position.row, position.column - 1),
            PositionDto(position.row, position.column + 1),
        )
}
