package com.mkz.rpg.mapBuilder.domain

import com.mkz.rpg.mapBuilder.domain.MapBuilderError.TileNotFound
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.MapCreated
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.MapImportRejected
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.MapImportRejected.Reason
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.MapImported
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.MapResized
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.TerrainSelected
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.TilePaintRejected
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.TilePainted
import kotlin.jvm.JvmInline

/**
 * The map the player is designing: a rectangular battlefield with the terrain id of each tile, and the terrain the
 * player paints the tiles with.
 */
@ConsistentCopyVisibility
data class MapBuilder private constructor(
    private val tiles: Tiles,
    private val selectedTerrainId: TerrainId,
    private val events: Set<MapBuilderEvent>,
) {
    companion object {
        const val MINIMUM_SIZE = 1
        const val MAXIMUM_SIZE = 128
        const val DEFAULT_SIZE = 16
        const val DEFAULT_TERRAIN_ID = "sand"

        /** A new map, with every tile of the default terrain, which is selected. */
        fun create(): MapBuilder {
            val tiles = Tiles.filled(rows = DEFAULT_SIZE, columns = DEFAULT_SIZE, terrainId = TerrainId(DEFAULT_TERRAIN_ID))
            return MapBuilder(
                tiles = tiles,
                selectedTerrainId = TerrainId(DEFAULT_TERRAIN_ID),
                events = setOf(MapCreated(rows = tiles.rows, columns = tiles.columns)),
            )
        }
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            rows = tiles.rows,
            columns = tiles.columns,
            tiles = tiles.toDto(),
            selectedTerrainId = selectedTerrainId.value,
        )

    /**
     * Changes the size, keeping the tiles that still fit and filling the new ones with the default terrain. A size out of
     * [MINIMUM_SIZE] to [MAXIMUM_SIZE] is ignored.
     */
    fun resize(
        rows: Int,
        columns: Int,
    ): MapBuilder {
        if (!isValidSize(rows) || !isValidSize(columns)) return this
        if (rows == tiles.rows && columns == tiles.columns) return this
        return copy(
            tiles = tiles.resize(rows = rows, columns = columns, fillTerrainId = TerrainId(DEFAULT_TERRAIN_ID)),
            events = events + MapResized(rows = rows, columns = columns),
        )
    }

    fun selectTerrain(terrainId: String): MapBuilder = copy(selectedTerrainId = TerrainId(terrainId), events = events + TerrainSelected(terrainId = terrainId))

    /**
     * Paints the tile with the selected terrain. It is rejected, leaving the tile unchanged, when the terrain could not
     * be next to one of the neighbouring tiles' terrain: [canBeNeighbours] tells whether two different terrains can.
     */
    fun paint(
        row: Int,
        column: Int,
        canBeNeighbours: (terrainId: String, neighbourTerrainId: String) -> Boolean,
    ): MapBuilder {
        val currentTerrainId = tiles.terrainId(row, column) ?: throw TileNotFound()
        if (currentTerrainId == selectedTerrainId) return this
        val terrainId = selectedTerrainId.value
        val forbiddenNeighbourTerrainId =
            tiles
                .neighbourTerrainIds(row, column)
                .map(TerrainId::value)
                .firstOrNull { neighbourTerrainId -> neighbourTerrainId != terrainId && !canBeNeighbours(terrainId, neighbourTerrainId) }
        if (forbiddenNeighbourTerrainId != null) {
            return copy(events = events + TilePaintRejected(row = row, column = column, terrainId = terrainId, neighbourTerrainId = forbiddenNeighbourTerrainId))
        }
        return copy(
            tiles = tiles.paint(row, column, selectedTerrainId),
            events = events + TilePainted(row = row, column = column, terrainId = terrainId),
        )
    }

    /**
     * Replaces the map with the imported tiles, keeping the selected terrain. The import is rejected, leaving the map
     * unchanged, when [tiles] is `null` (the imported text is not a map), when its size is out of [MINIMUM_SIZE] to
     * [MAXIMUM_SIZE], when [isKnownTerrain] does not know one of its terrains, or when two neighbouring tiles have
     * terrains that [canBeNeighbours] tells can not be next to each other.
     */
    fun import(
        tiles: List<List<String>>?,
        isKnownTerrain: (terrainId: String) -> Boolean,
        canBeNeighbours: (terrainId: String, neighbourTerrainId: String) -> Boolean,
    ): MapBuilder {
        if (tiles == null) return rejectImport(Reason.NOT_A_MAP)
        if (!isValidSize(tiles.size) || !isValidSize(tiles.first().size)) return rejectImport(Reason.SIZE_OUT_OF_RANGE)
        tiles.flatten().firstOrNull { terrainId -> !isKnownTerrain(terrainId) }?.let { terrainId ->
            return rejectImport(Reason.UNKNOWN_TERRAIN, terrainId = terrainId)
        }
        forbiddenNeighbours(tiles, canBeNeighbours)?.let { (terrainId, neighbourTerrainId) ->
            return rejectImport(Reason.FORBIDDEN_NEIGHBOURS, terrainId = terrainId, neighbourTerrainId = neighbourTerrainId)
        }
        val importedTiles = Tiles(tiles.map { row -> row.map(::TerrainId) })
        return copy(tiles = importedTiles, events = events + MapImported(rows = importedTiles.rows, columns = importedTiles.columns))
    }

    private fun rejectImport(
        reason: Reason,
        terrainId: String? = null,
        neighbourTerrainId: String? = null,
    ): MapBuilder = copy(events = events + MapImportRejected(reason = reason, terrainId = terrainId, neighbourTerrainId = neighbourTerrainId))

    // The first two different terrains next to each other that can not be neighbours, checking each tile with the tiles
    // to its east and to its south.
    private fun forbiddenNeighbours(
        tiles: List<List<String>>,
        canBeNeighbours: (terrainId: String, neighbourTerrainId: String) -> Boolean,
    ): Pair<String, String>? {
        tiles.forEachIndexed { row, terrainIds ->
            terrainIds.forEachIndexed { column, terrainId ->
                listOfNotNull(terrainIds.getOrNull(column + 1), tiles.getOrNull(row + 1)?.get(column)).forEach { neighbourTerrainId ->
                    if (neighbourTerrainId != terrainId && !canBeNeighbours(terrainId, neighbourTerrainId)) return terrainId to neighbourTerrainId
                }
            }
        }
        return null
    }

    private fun isValidSize(size: Int): Boolean = size in MINIMUM_SIZE..MAXIMUM_SIZE

    @JvmInline
    private value class TerrainId(
        val value: String,
    )

    // The terrain id of each tile: first are rows, second are columns.
    @JvmInline
    private value class Tiles(
        val value: List<List<TerrainId>>,
    ) {
        companion object {
            fun filled(
                rows: Int,
                columns: Int,
                terrainId: TerrainId,
            ) = Tiles(List(rows) { List(columns) { terrainId } })
        }

        val rows: Int get() = value.size

        val columns: Int get() = value.first().size

        fun terrainId(
            row: Int,
            column: Int,
        ): TerrainId? = value.getOrNull(row)?.getOrNull(column)

        fun neighbourTerrainIds(
            row: Int,
            column: Int,
        ): List<TerrainId> =
            listOfNotNull(
                terrainId(row - 1, column),
                terrainId(row, column + 1),
                terrainId(row + 1, column),
                terrainId(row, column - 1),
            )

        fun paint(
            row: Int,
            column: Int,
            terrainId: TerrainId,
        ) = Tiles(value.mapIndexed { rowIndex, terrainIds -> if (rowIndex == row) terrainIds.mapIndexed { columnIndex, current -> if (columnIndex == column) terrainId else current } else terrainIds })

        fun resize(
            rows: Int,
            columns: Int,
            fillTerrainId: TerrainId,
        ) = Tiles(List(rows) { row -> List(columns) { column -> terrainId(row, column) ?: fillTerrainId } })

        fun toDto(): List<List<String>> = value.map { terrainIds -> terrainIds.map(TerrainId::value) }
    }

    data class Dto(
        val rows: Int,
        val columns: Int,
        val tiles: List<List<String>>,
        val selectedTerrainId: String,
    )
}
