package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldRepository

class SearchPosition(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(battleUnitId: String): PositionDto? {
        val battlefield = battlefieldRepository.search() ?: return null
        return battlefield.position(battleUnitId)
    }
}
