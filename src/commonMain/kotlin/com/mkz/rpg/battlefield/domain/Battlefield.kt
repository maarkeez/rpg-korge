package com.mkz.rpg.battlefield.domain

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.TerrainTransitionDto
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.TerrainTransitionRuleDto
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.TileDto
import com.mkz.rpg.battlefield.domain.BattlefieldError.TileIsNotVacant
import com.mkz.rpg.battlefield.domain.BattlefieldError.TileNotFound
import com.mkz.rpg.battlefield.domain.BattlefieldError.TileTerrainTransitionNotAllowed
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.BattlefieldCreated
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.BattlefieldTileOccupied
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.OccupantRemoved
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Battlefield private constructor(
    private val rows: Rows,
    private val columns: Columns,
    private val tiles: Tiles,
    private val events: Set<BattlefieldEvent>,
) {
    companion object {
        /**
         * Tiles: first are rows, second are columns
         */
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
        battleUnitId: String,
    ): Battlefield {
        if (!tiles.isVacant(row, column)) throw TileIsNotVacant()
        val updatedTiles = tiles.occupy(row, column, battleUnitId)
        val battlefieldTileOccupiedEvent =
            BattlefieldTileOccupied(
                row = row,
                column = column,
                battlefieldUnitId = battleUnitId,
            )
        return copy(tiles = updatedTiles, events = events + battlefieldTileOccupiedEvent)
    }

    fun removeOccupant(battleUnitId: String): Battlefield {
        if (!tiles.isDeployed(battleUnitId)) return this
        val occupantPosition = tiles.position(battleUnitId)!!
        val updatedTiles = tiles.removeOccupant(battleUnitId)
        val occupantRemovedEvent =
            OccupantRemoved(
                battleUnitId = battleUnitId,
                row = occupantPosition.row,
                column = occupantPosition.column,
            )
        return copy(tiles = updatedTiles, events = events + occupantRemovedEvent)
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun canBeOccupied(
        row: Int,
        column: Int,
    ): Boolean = tiles.isVacant(row, column) && tiles.isTerrainOccupiable(row, column)

    fun occupant(
        row: Int,
        column: Int,
    ): String? = tiles.occupant(row, column)

    fun effectApplicationsToOccupants(terrainEffectIds: Map<String, String>): List<Dto.EffectApplicationToOccupantDto> = tiles.effectApplicationsToOccupants(terrainEffectIds)

    fun position(battleUnitId: String): PositionDto? = tiles.position(battleUnitId)

    fun isInBoundaries(
        row: Int,
        column: Int,
    ): Boolean {
        if (row < 0 || row >= rows.value) return false
        if (column < 0 || column >= rows.value) return false
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
        fun isVacant(
            row: Int,
            column: Int,
        ): Boolean {
            val tile = tiles[Position(row = row, column = column)] ?: throw TileNotFound()
            return tile.isVacant()
        }

        fun isTerrainOccupiable(
            row: Int,
            column: Int,
        ): Boolean {
            val tile = tiles[Position(row = row, column = column)] ?: throw TileNotFound()
            return tile.isTerrainOccupiable()
        }

        fun isDeployed(battlefieldUnitId: String) = tiles.values.any { tile -> tile.isOccupiedBy(battlefieldUnitId) }

        fun occupy(
            row: Int,
            column: Int,
            battleUnitId: String,
        ): Tiles {
            val tileToBeOccupied = tiles[Position(row = row, column = column)] ?: throw TileNotFound()
            val occupiedTile = tileToBeOccupied.occupy(battleUnitId)
            val previousOccupiedTile = tiles.values.firstOrNull { tile -> tile.isOccupiedBy(battleUnitId) }
            val vacantTile = previousOccupiedTile?.removeOccupant()
            val updatedTiles =
                buildMap {
                    putAll(tiles)
                    put(Position(row, column), occupiedTile)
                    vacantTile?.let {
                        put(Position(row = vacantTile.row(), column = vacantTile.column()), vacantTile)
                    }
                }
            return Tiles(updatedTiles)
        }

        fun removeOccupant(battleUnitId: String): Tiles {
            val previousOccupiedTile = tiles.values.firstOrNull { tile -> tile.isOccupiedBy(battleUnitId) }
            val vacantTile = previousOccupiedTile?.removeOccupant()
            val updatedTiles =
                buildMap {
                    putAll(tiles)
                    vacantTile?.let {
                        put(Position(row = vacantTile.row(), column = vacantTile.column()), vacantTile)
                    }
                }
            return Tiles(updatedTiles)
        }

        fun toDto(): Map<PositionDto, TileDto> =
            this.tiles
                .map { entry ->
                    PositionDto(entry.key.row, entry.key.column) to entry.value.toDto()
                }.toMap()

        fun occupant(
            row: Int,
            column: Int,
        ): String? = tiles[Position(row = row, column = column)]?.toDto()?.battleUnitId

        fun effectApplicationsToOccupants(terrainEffectIds: Map<String, String>): List<Dto.EffectApplicationToOccupantDto> =
            tiles.values
                .sortedWith(compareBy({ it.row() }, { it.column() }))
                .mapNotNull { tile ->
                    val terrainId = tile.terrainIdValue()
                    val occupantBattleUnitId = tile.occupantBattleUnitId()
                    if (occupantBattleUnitId != null && terrainId in terrainEffectIds) {
                        Dto.EffectApplicationToOccupantDto(
                            occupantBattleUnitId = occupantBattleUnitId,
                            terrainId = terrainId,
                            effectId = terrainEffectIds.getValue(terrainId),
                        )
                    } else {
                        null
                    }
                }

        fun position(battleUnitId: String): PositionDto? =
            tiles.entries
                .firstOrNull { (_, tile) -> tile.isOccupiedBy(battleUnitId) }
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
                                        terrainCanBeOccupied =
                                            isTerrainOccupiable(
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

            private fun isTerrainOccupiable(
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
            private val occupyingBattleUnitId: OccupyingBattleUnitId?,
            private val terrainId: TerrainId,
            private val terrainCanBeOccupied: TerrainCanBeOccupied,
            private val terrainTransition: TerrainTransition?,
        ) {
            companion object {
                fun create(
                    position: Position,
                    terrainId: String,
                    terrainCanBeOccupied: Boolean,
                    terrainTransition: TerrainTransition?,
                ) = Tile(
                    position = position,
                    occupyingBattleUnitId = null,
                    terrainId = TerrainId(terrainId),
                    terrainCanBeOccupied = TerrainCanBeOccupied(terrainCanBeOccupied),
                    terrainTransition = terrainTransition,
                )
            }

            fun isVacant() = occupyingBattleUnitId == null

            fun isOccupied() = occupyingBattleUnitId != null

            fun isTerrainOccupiable() = terrainCanBeOccupied.value

            fun occupy(battleUnitId: String) = copy(occupyingBattleUnitId = OccupyingBattleUnitId(battleUnitId))

            fun removeOccupant() = copy(occupyingBattleUnitId = null)

            fun isOccupiedBy(battleUnitId: String) = occupyingBattleUnitId?.value == battleUnitId

            fun terrainIdValue() = terrainId.value

            fun occupantBattleUnitId(): String? = occupyingBattleUnitId?.value

            fun row() = position.row

            fun column() = position.column

            fun toDto() =
                TileDto(
                    battleUnitId = occupyingBattleUnitId?.value,
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

        @JvmInline private value class OccupyingBattleUnitId(
            val value: String,
        )

        @JvmInline private value class TerrainId(
            val value: String,
        )

        @JvmInline private value class TerrainCanBeOccupied(
            val value: Boolean,
        )
    }

    data class Dto(
        val rows: Int,
        val columns: Int,
        val tiles: Map<PositionDto, TileDto>,
    ) {
        data class TileDto(
            val battleUnitId: String?,
            val terrainId: String,
            val terrainTransition: TerrainTransitionDto? = null,
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

        data class TerrainTransitionRuleDto(
            val fromTerrainId: String,
            val toTerrainId: String,
        )

        data class EffectApplicationToOccupantDto(
            val occupantBattleUnitId: String,
            val terrainId: String,
            val effectId: String,
        )
    }
}
