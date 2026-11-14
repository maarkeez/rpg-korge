package com.mkz.tessavar.battlefield.usecases.queries

import com.mkz.tessavar.battlefield.domain.BattlefieldRepository

class IsTileVacant(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(
        row: Int,
        column: Int,
    ): Boolean {
        val battlefield = battlefieldRepository.search() ?: return false
        return battlefield.isInBoundaries(row, column) && battlefield.isVacant(row, column)
    }
}
