package com.mkz.rpg.battlefield.domain

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.TerrainTransitionRuleDto
import kotlin.random.Random

object BattlefieldMother {
    fun battlefield(
        rows: Int = 3,
        columns: Int = 3,
    ): Battlefield = battlefield(rows, columns, List(rows) { List(columns) { "sand" } })

    fun battlefield(
        rows: Int,
        columns: Int,
        tiles: List<List<String>>,
        terrainTransitionRules: Set<TerrainTransitionRuleDto> = emptySet(),
    ): Battlefield =
        Battlefield
            .create(rows, columns, tiles, terrainOccupancy(tiles), terrainTransitionRules)
            .pullEvents()
            .second

    /**
     * Creates a valid battlefield from a map, with one line per row and one character per tile, separated by spaces:
     * - `.` is sand, which can be occupied.
     * - `#` is void, which can not be occupied.
     * - A letter is sand occupied by the battle unit that [occupants] maps the letter to.
     *
     * As in the game, sand in a corner between two void tiles (e.g.: void to the north and to the east) can not be occupied.
     *
     * For example: `battlefield("K . #\n. . #", occupants = mapOf('K' to "knight-1"))`.
     */
    fun battlefield(
        map: String,
        occupants: Map<Char, String> = emptyMap(),
    ): Battlefield {
        val mapTiles = map.trimIndent().lines().map { line -> line.split(" ").filter(String::isNotBlank).map(String::single) }
        require(mapTiles.all { it.size == mapTiles.first().size }) { "Every row of the battlefield map must have the same number of tiles" }
        val tiles = mapTiles.map { row -> row.map { tile -> if (tile == '#') "void" else "sand" } }
        val emptyBattlefield = battlefield(mapTiles.size, mapTiles.first().size, tiles, terrainTransitionRules(tiles))
        return mapTiles
            .flatMapIndexed { row, rowTiles -> rowTiles.mapIndexedNotNull { column, tile -> if (tile.isLetter()) Triple(row, column, tile) else null } }
            .fold(emptyBattlefield) { battlefield, (row, column, letter) ->
                val battleUnitId = requireNotNull(occupants[letter]) { "The battlefield map occupant '$letter' has no battle unit id" }
                battlefield.occupy(row, column, battleUnitId).pullEvents().second
            }
    }

    fun terrainId() = listOf("sand", "void").random()

    fun position(
        row: Int = Random.nextInt(0, 100),
        column: Int = Random.nextInt(0, 100),
    ) = Battlefield.Dto.PositionDto(row = row, column = column)

    fun terrainOccupancy(tiles: List<List<String>>): Map<String, Boolean> =
        tiles
            .flatten()
            .distinct()
            .associateWith { terrainId ->
                when (terrainId) {
                    "void" -> false
                    else -> true
                }
            }

    fun terrainTransitionRule(
        fromTerrainId: String,
        toTerrainId: String,
    ): TerrainTransitionRuleDto = TerrainTransitionRuleDto(fromTerrainId, toTerrainId)

    fun terrainTransitionRules(tiles: List<List<String>>): Set<TerrainTransitionRuleDto> {
        val terrainIds = tiles.flatten().distinct()
        return terrainIds
            .flatMap { fromTerrainId ->
                terrainIds
                    .filter { it != fromTerrainId }
                    .map { toTerrainId -> terrainTransitionRule(fromTerrainId, toTerrainId) }
            }.toSet()
    }
}
