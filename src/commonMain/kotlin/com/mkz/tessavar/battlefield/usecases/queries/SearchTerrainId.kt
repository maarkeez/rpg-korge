package com.mkz.tessavar.battlefield.usecases.queries

import com.mkz.tessavar.battlefield.domain.BattlefieldRepository

class SearchTerrainId(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(
        row: Int,
        column: Int,
    ): String? = battlefieldRepository.search()?.terrainId(row = row, column = column)
}
