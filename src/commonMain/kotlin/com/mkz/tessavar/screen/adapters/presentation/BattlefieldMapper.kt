package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.battlefield.domain.Battlefield

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
