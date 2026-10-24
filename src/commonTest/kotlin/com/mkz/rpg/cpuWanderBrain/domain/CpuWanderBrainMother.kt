package com.mkz.rpg.cpuWanderBrain.domain

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import kotlin.math.abs

object CpuWanderBrainMother {
    /**
     * Homes: the tile each CPU battle unit was deployed on, by battle unit id. Distances from home: how far each tile is
     * from a home, by default as on an open field of [OPEN_FIELD_TILES] x [OPEN_FIELD_TILES] tiles.
     */
    fun cpuWanderBrain(
        homes: Map<String, Pair<Int, Int>> = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2)),
        distancesFromHome: (Pair<Int, Int>) -> Map<PositionDto, Int> = ::openFieldDistances,
    ): CpuWanderBrain =
        homes.entries.fold(CpuWanderBrain.create()) { cpuWanderBrain, (battleUnitId, home) ->
            cpuWanderBrain.claimHome(battleUnitId = battleUnitId, row = home.first, column = home.second, distancesFromHome = distancesFromHome(home))
        }

    private const val OPEN_FIELD_TILES = 10

    // Without void nor harmful terrain, a battle unit walks as many tiles as rows and columns apart.
    fun openFieldDistances(home: Pair<Int, Int>): Map<PositionDto, Int> =
        (0 until OPEN_FIELD_TILES)
            .flatMap { row -> (0 until OPEN_FIELD_TILES).map { column -> PositionDto(row = row, column = column) } }
            .associateWith { position -> abs(position.row - home.first) + abs(position.column - home.second) }

    /**
     * The map, with the party leader ('K') standing apart: in a new corner, bottom right, walled off by void. The battle
     * units of a small map are in its sight, and wander as if the leader was not there.
     */
    fun withLeaderApart(map: String): String {
        val rows = map.trimIndent().lines().map(String::trim)
        val columns = rows.first().split(" ").count(String::isNotBlank)
        return (rows.map { row -> "$row #" } + (List(columns) { "#" } + "K").joinToString(" ")).joinToString("\n")
    }
}
