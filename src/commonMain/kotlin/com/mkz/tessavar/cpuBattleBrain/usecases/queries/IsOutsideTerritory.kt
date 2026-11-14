package com.mkz.tessavar.cpuBattleBrain.usecases.queries

import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.cpuBattleBrain.usecases.services.WANDER_DISTANCE

class IsOutsideTerritory(
    private val searchPosition: SearchPosition,
) {
    operator fun invoke(
        battleUnitId: String,
        distancesFromHome: Map<PositionDto, Int>,
    ): Boolean {
        val position = searchPosition(occupantId = battleUnitId) ?: return false
        val distanceFromHome = distancesFromHome[position] ?: return false
        return distanceFromHome > WANDER_DISTANCE
    }
}
