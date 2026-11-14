package com.mkz.tessavar.battlefield.usecases.queries

import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto

class SearchShortestPath(
    private val isTileVacant: IsTileVacant,
) {
    operator fun invoke(
        fromRow: Int,
        fromColumn: Int,
        toRow: Int,
        toColumn: Int,
    ): List<PositionDto>? {
        val origin = PositionDto(fromRow, fromColumn)
        val destination = PositionDto(toRow, toColumn)
        val previousPositions = mutableMapOf<PositionDto, PositionDto?>(origin to null)
        val queue = ArrayDeque(listOf(origin))
        while (queue.isNotEmpty()) {
            val currentPosition = queue.removeFirst()
            if (currentPosition == destination) return pathTo(destination, previousPositions)
            for (nextPosition in adjacentPositions(currentPosition)) {
                if (nextPosition !in previousPositions && isTileVacant(nextPosition.row, nextPosition.column)) {
                    previousPositions[nextPosition] = currentPosition
                    queue.addLast(nextPosition)
                }
            }
        }
        return null
    }

    private fun pathTo(
        destination: PositionDto,
        previousPositions: Map<PositionDto, PositionDto?>,
    ): List<PositionDto> =
        generateSequence(destination) { position -> previousPositions[position] }
            .toList()
            .reversed()
            .drop(1)

    private fun adjacentPositions(position: PositionDto): List<PositionDto> =
        listOf(
            PositionDto(position.row - 1, position.column),
            PositionDto(position.row + 1, position.column),
            PositionDto(position.row, position.column - 1),
            PositionDto(position.row, position.column + 1),
        )
}
