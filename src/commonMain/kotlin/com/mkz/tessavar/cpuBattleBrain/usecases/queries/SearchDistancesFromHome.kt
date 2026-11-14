package com.mkz.tessavar.cpuBattleBrain.usecases.queries

import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.battlefield.usecases.queries.IsTileOccupiable

class SearchDistancesFromHome(
    private val isTileOccupiable: IsTileOccupiable,
    private val searchTerrainHealthEffect: SearchTerrainHealthEffect,
) {
    operator fun invoke(
        homeRow: Int,
        homeColumn: Int,
    ): Map<PositionDto, Int> {
        val home = PositionDto(row = homeRow, column = homeColumn)
        val distances = mutableMapOf(home to 0)
        val queue = ArrayDeque(listOf(home))
        while (queue.isNotEmpty()) {
            val currentPosition = queue.removeFirst()
            val distance = distances.getValue(currentPosition) + 1
            adjacentPositions(currentPosition)
                .filter { nextPosition -> nextPosition !in distances && isTileOccupiable(row = nextPosition.row, column = nextPosition.column) }
                .forEach { nextPosition ->
                    distances[nextPosition] = distance
                    if (!isHarmful(nextPosition)) queue.addLast(nextPosition)
                }
        }
        return distances
    }

    private fun isHarmful(position: PositionDto): Boolean = searchTerrainHealthEffect(row = position.row, column = position.column) < 0

    private fun adjacentPositions(position: PositionDto): List<PositionDto> =
        listOf(
            PositionDto(position.row - 1, position.column),
            PositionDto(position.row + 1, position.column),
            PositionDto(position.row, position.column - 1),
            PositionDto(position.row, position.column + 1),
        )
}
