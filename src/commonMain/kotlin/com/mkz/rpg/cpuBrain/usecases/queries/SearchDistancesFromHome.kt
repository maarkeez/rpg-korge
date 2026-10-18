package com.mkz.rpg.cpuBrain.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTerrainBeOccupied

/**
 * How many tiles a wandering battle unit walks from its home to each tile it can wander to: the tiles whose terrain can
 * be occupied (e.g.: not void) and is not harmful (e.g.: not lava), as it never steps on harmful terrain while it
 * wanders. The battle units standing on them do not count, as they walk around. Home is 0 tiles away, even on harmful
 * terrain. The tiles it can not walk to from home are left out.
 *
 * Walking only where it can step means every tile away from home has a tile next to it nearer to home, so a battle unit
 * heading home never gets stuck behind harmful terrain.
 */
class SearchDistancesFromHome(
    private val canBattlefieldTerrainBeOccupied: CanBattlefieldTerrainBeOccupied,
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
                .filter { nextPosition -> nextPosition !in distances && canBeWandered(nextPosition) }
                .forEach { nextPosition ->
                    distances[nextPosition] = distance
                    queue.addLast(nextPosition)
                }
        }
        return distances
    }

    private fun canBeWandered(position: PositionDto): Boolean =
        canBattlefieldTerrainBeOccupied(row = position.row, column = position.column) &&
            searchTerrainHealthEffect(row = position.row, column = position.column) >= 0

    private fun adjacentPositions(position: PositionDto): List<PositionDto> =
        listOf(
            PositionDto(position.row - 1, position.column),
            PositionDto(position.row + 1, position.column),
            PositionDto(position.row, position.column - 1),
            PositionDto(position.row, position.column + 1),
        )
}
