package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battlefield.domain.Battlefield

/** Maps a battlefield and its positions to what the views of the battlefield draw, for the presenters displaying them. */
internal object BattlefieldMapper {
    fun terrain(battlefield: Battlefield.Dto): BattlefieldTerrain =
        BattlefieldTerrain(
            rows = battlefield.rows,
            columns = battlefield.columns,
            tiles =
                battlefield.tiles.entries.associate { (position, tile) ->
                    position(position) to
                        BattlefieldTerrain.Tile(
                            terrainId = tile.terrainId,
                            transition =
                                tile.terrainTransition?.let { transition ->
                                    TerrainTransition(
                                        fromTerrainId = transition.fromTerrainId,
                                        toTerrainId = transition.toTerrainId,
                                        wangIndex = transition.wangIndex,
                                    )
                                },
                        )
                },
        )

    fun position(position: Battlefield.Dto.PositionDto): TilePosition = TilePosition(row = position.row, column = position.column)

    fun positions(positions: Collection<Battlefield.Dto.PositionDto>): Set<TilePosition> = positions.map(::position).toSet()
}
