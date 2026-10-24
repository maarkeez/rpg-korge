package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.BattlefieldRepository

/** The terrain of the tile, or `null` out of the battlefield, without copying the battlefield. */
class SearchTerrainId(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(
        row: Int,
        column: Int,
    ): String? = battlefieldRepository.search()?.terrainId(row = row, column = column)
}
