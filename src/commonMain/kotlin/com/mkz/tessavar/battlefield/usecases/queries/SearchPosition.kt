package com.mkz.tessavar.battlefield.usecases.queries

import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.battlefield.domain.BattlefieldRepository

class SearchPosition(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(occupantId: String): PositionDto? {
        val battlefield = battlefieldRepository.search() ?: return null
        return battlefield.position(occupantId)
    }
}
