package com.mkz.rpg.battlefield.domain

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.TerrainTransitionDto
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.TerrainTransitionRuleDto
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.TileDto
import com.mkz.rpg.battlefield.domain.BattlefieldError.TileIsOccupied
import com.mkz.rpg.battlefield.domain.BattlefieldError.TileNotFound
import com.mkz.rpg.battlefield.domain.BattlefieldError.TileTerrainTransitionNotAllowed
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.BattlefieldCreated
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.OccupantRemoved
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.TileOccupied
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Battlefield private constructor(
    private val rows: Rows,
    private val columns: Columns,
    private val tiles: Tiles,
    private val events: Set<BattlefieldEvent>,
) {
    companion object {
        fun create(
            rows: Int,
            columns: Int,
            tiles: List<List<String>>,
            terrainCanBeOccupied: Map<String, Boolean>,
            terrainTransitionRules: Set<TerrainTransitionRuleDto>,
        ): Battlefield =
            Battlefield(
                rows = Rows(rows),
                columns = Columns(columns),
                tiles = Tiles.create(tiles, terrainCanBeOccupied, terrainTransitionRules),
                events = setOf(BattlefieldCreated),
            )
    }

    fun toDto() =
        Dto(
            rows = rows.value,
            columns = columns.value,
            tiles = tiles.toDto(),
        )

    fun occupy(
        row: Int,
        column: Int,
        occupantId: String,
    ): Battlefield {
        if (!tiles.hasNoOccupant(row, column)) throw TileIsOccupied()
        val updatedTiles = tiles.occupy(row, column, occupantId)
        val tileOccupiedEvent =
            TileOccupied(
                row = row,
                column = column,
                occupantId = occupantId,
            )
        return copy(tiles = updatedTiles, events = events + tileOccupiedEvent)
    }

    fun occupyTiles(
        row: Int,
        column: Int,
        rows: Int,
        columns: Int,
        occupantId: String,
    ): Battlefield {
        val positions = (row until row + rows).flatMap { areaRow -> (column until column + columns).map { areaColumn -> PositionDto(areaRow, areaColumn) } }
        if (positions.any { position -> !tiles.hasNoOccupant(position.row, position.column) }) throw TileIsOccupied()
        val occupiedEvents =
            positions.map { position ->
                TileOccupied(row = position.row, column = position.column, occupantId = occupantId)
            }
        return copy(tiles = tiles.occupyTiles(positions, occupantId), events = events + occupiedEvents)
    }

    fun removeOccupant(occupantId: String): Battlefield {
        if (!tiles.hasOccupant(occupantId)) return this
        val occupantPosition = tiles.position(occupantId)!!
        val updatedTiles = tiles.removeOccupant(occupantId)
        val occupantRemovedEvent =
            OccupantRemoved(
                occupantId = occupantId,
                row = occupantPosition.row,
                column = occupantPosition.column,
            )
        return copy(tiles = updatedTiles, events = events + occupantRemovedEvent)
    }

    fun swapOccupants(
        occupantId: String,
        otherOccupantId: String,
    ): Battlefield {
        val position = tiles.position(occupantId) ?: return this
        val otherPosition = tiles.position(otherOccupantId) ?: return this
        val swappedEvents =
            setOf(
                TileOccupied(row = otherPosition.row, column = otherPosition.column, occupantId = occupantId),
                TileOccupied(row = position.row, column = position.column, occupantId = otherOccupantId),
            )
        return copy(tiles = tiles.swapOccupants(position, otherPosition), events = events + swappedEvents)
    }

    fun pullEvents(): Pair<Set<BattlefieldEvent>, Battlefield> = events to copy(events = emptySet())

    fun occupants(): List<Dto.OccupantDto> = tiles.occupants()

    fun loadOccupants(occupants: List<Dto.OccupantDto>): Battlefield = copy(tiles = tiles.loadOccupants(occupants))

    fun isVacant(
        row: Int,
        column: Int,
    ): Boolean = tiles.hasNoOccupant(row, column) && tiles.isOccupiable(row, column)

    fun isOccupiable(
        row: Int,
        column: Int,
    ): Boolean = tiles.isOccupiable(row, column)

    fun occupant(
        row: Int,
        column: Int,
    ): String? = tiles.occupant(row, column)

    fun terrainEffectsOnOccupants(terrainEffectIds: Map<String, String>): List<Dto.TerrainEffectOnOccupantDto> = tiles.terrainEffectsOnOccupants(terrainEffectIds)

    fun position(occupantId: String): PositionDto? = tiles.position(occupantId)

    fun terrainId(
        row: Int,
        column: Int,
    ): String? = tiles.terrainId(row, column)

    fun size() = Dto.SizeDto(rows = rows.value, columns = columns.value)

    fun isInBoundaries(
        row: Int,
        column: Int,
    ): Boolean {
        if (row < 0 || row >= rows.value) return false
        if (column < 0 || column >= columns.value) return false
        return true
    }

    @JvmInline private value class Rows(
        val value: Int,
    )

    @JvmInline private value class Columns(
        val value: Int,
    )

    @JvmInline private value class Tiles(
        val tiles: Map<Position, Tile>,
    ) {
        fun hasNoOccupant(
            row: Int,
            column: Int,
        ): Boolean {
            val tile = tiles[Position(row = row, column = column)] ?: throw TileNotFound()
            return tile.hasNoOccupant()
        }

        fun isOccupiable(
            row: Int,
            column: Int,
        ): Boolean {
            val tile = tiles[Position(row = row, column = column)] ?: throw TileNotFound()
            return tile.isOccupiable()
        }

        fun hasOccupant(occupantId: String) = tiles.values.any { tile -> tile.isOccupiedBy(occupantId) }

        fun occupy(
            row: Int,
            column: Int,
            occupantId: String,
        ): Tiles {
            val tileToBeOccupied = tiles[Position(row = row, column = column)] ?: throw TileNotFound()
            val occupiedTile = tileToBeOccupied.occupy(occupantId)
            val previousOccupiedTile = tiles.values.firstOrNull { tile -> tile.isOccupiedBy(occupantId) }
            val tileLeft = previousOccupiedTile?.removeOccupant()
            val updatedTiles =
                buildMap {
                    putAll(tiles)
                    put(Position(row, column), occupiedTile)
                    tileLeft?.let {
                        put(Position(row = tileLeft.row(), column = tileLeft.column()), tileLeft)
                    }
                }
            return Tiles(updatedTiles)
        }

        fun occupyTiles(
            positions: List<PositionDto>,
            occupantId: String,
        ): Tiles =
            Tiles(
                tiles +
                    positions.associate { position ->
                        val tilePosition = Position(row = position.row, column = position.column)
                        tilePosition to tiles.getValue(tilePosition).occupy(occupantId)
                    },
            )

        fun swapOccupants(
            position: PositionDto,
            otherPosition: PositionDto,
        ): Tiles {
            val tile = tiles.getValue(Position(row = position.row, column = position.column))
            val otherTile = tiles.getValue(Position(row = otherPosition.row, column = otherPosition.column))
            val updatedTiles =
                buildMap {
                    putAll(tiles)
                    put(Position(row = position.row, column = position.column), tile.occupy(otherTile.occupantId()!!))
                    put(Position(row = otherPosition.row, column = otherPosition.column), otherTile.occupy(tile.occupantId()!!))
                }
            return Tiles(updatedTiles)
        }

        fun removeOccupant(occupantId: String): Tiles {
            val previousOccupiedTile = tiles.values.firstOrNull { tile -> tile.isOccupiedBy(occupantId) }
            val tileLeft = previousOccupiedTile?.removeOccupant()
            val updatedTiles =
                buildMap {
                    putAll(tiles)
                    tileLeft?.let {
                        put(Position(row = tileLeft.row(), column = tileLeft.column()), tileLeft)
                    }
                }
            return Tiles(updatedTiles)
        }

        fun occupants(): List<Dto.OccupantDto> =
            tiles.values.mapNotNull { tile -> tile.occupantId()?.let { occupantId -> Dto.OccupantDto(occupantId = occupantId, row = tile.row(), column = tile.column()) } }

        fun loadOccupants(occupants: List<Dto.OccupantDto>): Tiles {
            val tilesWithNoOccupant = tiles.mapValues { (_, tile) -> tile.removeOccupant() }
            val occupiedTiles =
                occupants.associate { occupant ->
                    val position = Position(row = occupant.row, column = occupant.column)
                    val tile = tilesWithNoOccupant[position] ?: throw TileNotFound()
                    position to tile.occupy(occupant.occupantId)
                }
            return Tiles(tilesWithNoOccupant + occupiedTiles)
        }

        fun toDto(): Map<PositionDto, TileDto> =
            this.tiles
                .map { entry ->
                    PositionDto(entry.key.row, entry.key.column) to entry.value.toDto()
                }.toMap()

        fun occupant(
            row: Int,
            column: Int,
        ): String? = tiles[Position(row = row, column = column)]?.toDto()?.occupantId

        fun terrainId(
            row: Int,
            column: Int,
        ): String? = tiles[Position(row = row, column = column)]?.terrainIdValue()

        fun terrainEffectsOnOccupants(terrainEffectIds: Map<String, String>): List<Dto.TerrainEffectOnOccupantDto> =
            tiles.values
                .sortedWith(compareBy({ it.row() }, { it.column() }))
                .mapNotNull { tile ->
                    val terrainId = tile.terrainIdValue()
                    val occupantId = tile.occupantId()
                    if (occupantId != null && terrainId in terrainEffectIds) {
                        Dto.TerrainEffectOnOccupantDto(
                            occupantId = occupantId,
                            terrainId = terrainId,
                            effectId = terrainEffectIds.getValue(terrainId),
                        )
                    } else {
                        null
                    }
                }

        fun position(occupantId: String): PositionDto? =
            tiles.entries
                .firstOrNull { (_, tile) -> tile.isOccupiedBy(occupantId) }
                ?.key
                ?.toDto()

        companion object {
            fun create(
                tiles: List<List<String>>,
                terrainCanBeOccupied: Map<String, Boolean>,
                terrainTransitionRules: Set<TerrainTransitionRuleDto>,
            ): Tiles {
                validateTileTerrainTransitions(tiles, terrainTransitionRules)
                val tiles =
                    tiles
                        .flatMapIndexed { rowIndex, row ->
                            row.mapIndexed { columnIndex, terrainId ->
                                val position = Position(rowIndex, columnIndex)
                                position to
                                    Tile.create(
                                        position = position,
                                        terrainId = terrainId,
                                        occupiable =
                                            isOccupiable(
                                                tiles = tiles,
                                                terrainCanBeOccupied = terrainCanBeOccupied,
                                                row = rowIndex,
                                                column = columnIndex,
                                                terrainId = terrainId,
                                            ),
                                        terrainTransition =
                                            terrainTransition(
                                                tiles = tiles,
                                                terrainTransitionRules = terrainTransitionRules,
                                                row = rowIndex,
                                                column = columnIndex,
                                            ),
                                    )
                            }
                        }.toMap()
                return Tiles(tiles)
            }

            private fun validateTileTerrainTransitions(
                tiles: List<List<String>>,
                terrainTransitionRules: Set<TerrainTransitionRuleDto>,
            ) {
                tiles.forEachIndexed { rowIndex, row ->
                    row.forEachIndexed { columnIndex, terrainId ->
                        adjacentTerrainIds(tiles, rowIndex, columnIndex).forEach { adjacentTerrainId ->
                            if (terrainId == adjacentTerrainId) return@forEach
                            if (!hasTerrainTransition(terrainTransitionRules, terrainId, adjacentTerrainId)) {
                                throw TileTerrainTransitionNotAllowed(terrainId, adjacentTerrainId)
                            }
                        }
                    }
                }
            }

            private fun hasTerrainTransition(
                terrainTransitionRules: Set<TerrainTransitionRuleDto>,
                terrainId: String,
                adjacentTerrainId: String,
            ): Boolean =
                terrainTransitionRules.any { it.fromTerrainId == terrainId && it.toTerrainId == adjacentTerrainId } ||
                    terrainTransitionRules.any { it.fromTerrainId == adjacentTerrainId && it.toTerrainId == terrainId }

            private fun adjacentTerrainIds(
                tiles: List<List<String>>,
                rowIndex: Int,
                columnIndex: Int,
            ): List<String> =
                listOfNotNull(
                    tiles.getOrNull(rowIndex - 1)?.getOrNull(columnIndex),
                    tiles.getOrNull(rowIndex)?.getOrNull(columnIndex + 1),
                    tiles.getOrNull(rowIndex + 1)?.getOrNull(columnIndex),
                    tiles.getOrNull(rowIndex)?.getOrNull(columnIndex - 1),
                )

            private fun terrainTransition(
                tiles: List<List<String>>,
                terrainTransitionRules: Set<TerrainTransitionRuleDto>,
                row: Int,
                column: Int,
            ): TerrainTransition? {
                val terrainId = tiles[row][column]
                val adjacentTerrainIds = adjacentTerrainIds(tiles, row, column).filter { it != terrainId }
                val rule =
                    terrainTransitionRules.firstOrNull { candidateRule ->
                        candidateRule.fromTerrainId == terrainId && adjacentTerrainIds.contains(candidateRule.toTerrainId)
                    } ?: return null
                val targetTerrainId = rule.toTerrainId
                var wangIndex = NO_TERRAIN_TRANSITION
                if (hasAdjacentTerrain(tiles, targetTerrainId, row - 1, column)) wangIndex += NORTH_TERRAIN_TRANSITION_WEIGHT
                if (hasAdjacentTerrain(tiles, targetTerrainId, row, column + 1)) wangIndex += EAST_TERRAIN_TRANSITION_WEIGHT
                if (hasAdjacentTerrain(tiles, targetTerrainId, row + 1, column)) wangIndex += SOUTH_TERRAIN_TRANSITION_WEIGHT
                if (hasAdjacentTerrain(tiles, targetTerrainId, row, column - 1)) wangIndex += WEST_TERRAIN_TRANSITION_WEIGHT
                return TerrainTransition(terrainId, targetTerrainId, wangIndex)
            }

            private fun hasAdjacentTerrain(
                tiles: List<List<String>>,
                terrainId: String,
                row: Int,
                column: Int,
            ): Boolean {
                val adjacentTerrainId = tiles.getOrNull(row)?.getOrNull(column) ?: return false
                return adjacentTerrainId == terrainId
            }

            private fun isOccupiable(
                tiles: List<List<String>>,
                terrainCanBeOccupied: Map<String, Boolean>,
                row: Int,
                column: Int,
                terrainId: String,
            ): Boolean {
                if (terrainCanBeOccupied[terrainId] != true) return false
                return OCCUPIABLE_TERRAIN_TRANSITION_WANG_INDICES.contains(
                    terrainTransitionWangIndex(
                        tiles = tiles,
                        terrainCanBeOccupied = terrainCanBeOccupied,
                        row = row,
                        column = column,
                    ),
                )
            }

            private fun terrainTransitionWangIndex(
                tiles: List<List<String>>,
                terrainCanBeOccupied: Map<String, Boolean>,
                row: Int,
                column: Int,
            ): Int {
                var wangIndex = NO_TERRAIN_TRANSITION
                if (hasNonOccupiableAdjacentTerrain(tiles, terrainCanBeOccupied, row - 1, column)) wangIndex += NORTH_TERRAIN_TRANSITION_WEIGHT
                if (hasNonOccupiableAdjacentTerrain(tiles, terrainCanBeOccupied, row, column + 1)) wangIndex += EAST_TERRAIN_TRANSITION_WEIGHT
                if (hasNonOccupiableAdjacentTerrain(tiles, terrainCanBeOccupied, row + 1, column)) wangIndex += SOUTH_TERRAIN_TRANSITION_WEIGHT
                if (hasNonOccupiableAdjacentTerrain(tiles, terrainCanBeOccupied, row, column - 1)) wangIndex += WEST_TERRAIN_TRANSITION_WEIGHT
                return wangIndex
            }

            private fun hasNonOccupiableAdjacentTerrain(
                tiles: List<List<String>>,
                terrainCanBeOccupied: Map<String, Boolean>,
                row: Int,
                column: Int,
            ): Boolean {
                val adjacentTerrainId = tiles.getOrNull(row)?.getOrNull(column) ?: return false
                return terrainCanBeOccupied[adjacentTerrainId] != true
            }

            private const val NO_TERRAIN_TRANSITION = 0
            private const val NORTH_TERRAIN_TRANSITION_WEIGHT = 1
            private const val EAST_TERRAIN_TRANSITION_WEIGHT = 2
            private const val SOUTH_TERRAIN_TRANSITION_WEIGHT = 4
            private const val WEST_TERRAIN_TRANSITION_WEIGHT = 8
            private val OCCUPIABLE_TERRAIN_TRANSITION_WANG_INDICES = setOf(0, 1, 2, 4, 5, 8, 10)
        }

        @ConsistentCopyVisibility
        private data class Tile private constructor(
            private val position: Position,
            private val occupant: OccupantId?,
            private val terrainId: TerrainId,
            private val occupiable: Occupiable,
            private val terrainTransition: TerrainTransition?,
        ) {
            companion object {
                fun create(
                    position: Position,
                    terrainId: String,
                    occupiable: Boolean,
                    terrainTransition: TerrainTransition?,
                ) = Tile(
                    position = position,
                    occupant = null,
                    terrainId = TerrainId(terrainId),
                    occupiable = Occupiable(occupiable),
                    terrainTransition = terrainTransition,
                )
            }

            fun hasNoOccupant() = occupant == null

            fun isOccupied() = occupant != null

            fun isOccupiable() = occupiable.value

            fun occupy(occupantId: String) = copy(occupant = OccupantId(occupantId))

            fun removeOccupant() = copy(occupant = null)

            fun isOccupiedBy(occupantId: String) = occupant?.value == occupantId

            fun terrainIdValue() = terrainId.value

            fun occupantId(): String? = occupant?.value

            fun row() = position.row

            fun column() = position.column

            fun toDto() =
                TileDto(
                    occupantId = occupant?.value,
                    terrainId = terrainId.value,
                    terrainTransition = terrainTransition?.toDto(),
                )
        }

        private data class TerrainTransition(
            val fromTerrainId: String,
            val toTerrainId: String,
            val wangIndex: Int,
        ) {
            fun toDto() =
                TerrainTransitionDto(
                    fromTerrainId = fromTerrainId,
                    toTerrainId = toTerrainId,
                    wangIndex = wangIndex,
                )
        }

        private data class Position(
            val row: Int,
            val column: Int,
        ) {
            fun toDto() = PositionDto(row = row, column = column)
        }

        @JvmInline private value class OccupantId(
            val value: String,
        )

        @JvmInline private value class TerrainId(
            val value: String,
        )

        @JvmInline private value class Occupiable(
            val value: Boolean,
        )
    }

    data class Dto(
        val rows: Int,
        val columns: Int,
        val tiles: Map<PositionDto, TileDto>,
    ) {
        data class TileDto(
            val occupantId: String?,
            val terrainId: String,
            val terrainTransition: TerrainTransitionDto?,
        )

        data class TerrainTransitionDto(
            val fromTerrainId: String,
            val toTerrainId: String,
            val wangIndex: Int,
        )

        data class PositionDto(
            val row: Int,
            val column: Int,
        )

        data class SizeDto(
            val rows: Int,
            val columns: Int,
        )

        data class TerrainTransitionRuleDto(
            val fromTerrainId: String,
            val toTerrainId: String,
        )

        data class OccupantDto(
            val occupantId: String,
            val row: Int,
            val column: Int,
        )

        data class TerrainEffectOnOccupantDto(
            val occupantId: String,
            val terrainId: String,
            val effectId: String,
        )
    }
}
