package battlefield.domain

import battlefield.domain.Battlefield.Dto.TileDto
import battlefield.domain.BattlefieldError.TileIsNotVacant
import battlefield.domain.BattlefieldError.TileNotFound
import battlefield.domain.BattlefieldEvent.BattlefieldCreated
import battlefield.domain.BattlefieldEvent.BattlefieldTileOccupied
import battlefield.domain.BattlefieldEvent.OccupantRemoved
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
        fun create(rows: Int, columns: Int, tiles: List<List<String>>): Battlefield {
            return Battlefield(
                rows = Rows(rows),
                columns = Columns(columns),
                tiles = Tiles.create(tiles),
                events = setOf(BattlefieldCreated)
            )
        }
    }

    fun toDto() = Dto(
        rows = rows.value,
        columns = columns.value,
        tiles = tiles.toDto()
    )

    fun occupy(row: Int, column: Int, battleUnitId: String): Battlefield {
        if(!tiles.isVacant(row, column)) throw TileIsNotVacant()
        val updatedTiles = tiles.occupy(row, column, battleUnitId)
        val battlefieldTileOccupiedEvent = BattlefieldTileOccupied(
            row = row,
            column = column,
            battlefieldUnitId = battleUnitId
        )
        return copy(tiles = updatedTiles, events = events + battlefieldTileOccupiedEvent)
    }

    fun removeOccupant(battleUnitId: String): Battlefield {
        if(!tiles.isDeployed(battleUnitId)) return this
        val updatedTiles = tiles.removeOccupant(battleUnitId)
        val occupantRemovedEvent = OccupantRemoved(battleUnitId = battleUnitId)
        return copy(tiles = updatedTiles, events = events + occupantRemovedEvent)
    }

    fun pullEvents() = events to copy(events = emptySet())
    fun canBeOccupied(row: Int, column: Int): Boolean {
        return tiles.isVacant(row, column)
    }

    fun occupant(row: Int, column: Int): String? {
        return tiles.occupant(row, column)
    }

    @JvmInline private value class Rows(val value: Int)
    @JvmInline private value class Columns(val value: Int)
    @JvmInline private value class Tiles(val tiles: Map<Position, Tile>) {

        fun isVacant(row: Int, column: Int): Boolean {
            val tile = tiles[Position(row = row, column = column)] ?: throw TileNotFound()
            return tile.isVacant()
        }

        fun isDeployed(battlefieldUnitId: String) = tiles.values.any { tile -> tile.isOccupiedBy(battlefieldUnitId)}

        fun occupy(row: Int, column: Int, battleUnitId: String): Tiles {
            val tileToBeOccupied = tiles[Position(row = row, column = column)] ?: throw TileNotFound()
            val occupiedTile = tileToBeOccupied.occupy(battleUnitId)
            val previousOccupiedTile = tiles.values.firstOrNull { tile -> tile.isOccupiedBy(battleUnitId) }
            val vacantTile = previousOccupiedTile?.removeOccupant()
            val updatedTiles = buildMap {
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
            val updatedTiles = buildMap {
                putAll(tiles)
                vacantTile?.let {
                    put(Position(row = vacantTile.row(), column = vacantTile.column()), vacantTile)
                }
            }
            return Tiles(updatedTiles)
        }

        fun toDto(): Map<Dto.PositionDto, TileDto> = this.tiles.map { entry ->
            Dto.PositionDto(entry.key.row, entry.key.column) to entry.value.toDto()
        }.toMap()

        fun occupant(row: Int, column: Int): String? {
            return tiles[Position(row = row, column = column)]?.toDto()?.battleUnitId
        }

        companion object {
            fun create(tiles: List<List<String>>): Tiles {
                val tiles = tiles.flatMapIndexed { rowIndex, row ->
                    row.mapIndexed { columnIndex, terrainId ->
                        Position(rowIndex, columnIndex) to Tile.create(rowIndex, columnIndex, terrainId)
                    }
                }.toMap()
                return Tiles(tiles)
            }
        }

        @ConsistentCopyVisibility
        private data class Tile private constructor(
            private val position: Position,
            private val occupyingBattleUnitId: OccupyingBattleUnitId?,
            private val terrainId: TerrainId,
        ) {
            companion object {
                fun create(row: Int, column: Int, terrainId: String) = Tile(
                    position = Position(row = row, column = column),
                    occupyingBattleUnitId = null,
                    terrainId = TerrainId(terrainId),
                )
            }
            fun isVacant() = occupyingBattleUnitId == null
            fun occupy(battleUnitId: String) = copy(occupyingBattleUnitId = OccupyingBattleUnitId(battleUnitId))
            fun removeOccupant() = copy(occupyingBattleUnitId = null)
            fun isOccupiedBy(battleUnitId: String) = occupyingBattleUnitId?.value == battleUnitId
            fun row() = position.row
            fun column() = position.column
            fun toDto() = TileDto(
                battleUnitId = this.occupyingBattleUnitId?.value
            )
        }

        private data class Position(val row: Int, val column: Int)
        @JvmInline private value class OccupyingBattleUnitId(val value: String)
        @JvmInline private value class TerrainId(val value: String)
    }

    data class Dto(
        val rows: Int,
        val columns: Int,
        val tiles: Map<PositionDto, TileDto>
    ){
        data class TileDto(
            val battleUnitId: String?
        )
        data class PositionDto(val row: Int, val column: Int)
    }
}
