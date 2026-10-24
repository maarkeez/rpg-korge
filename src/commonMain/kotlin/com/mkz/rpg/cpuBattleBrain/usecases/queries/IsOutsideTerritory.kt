package com.mkz.rpg.cpuBattleBrain.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.cpuBattleBrain.usecases.services.WANDER_DISTANCE

/**
 * Whether the battle unit stands outside its territory: further than [WANDER_DISTANCE] tiles from home, walking around
 * void and harmful terrain ([SearchDistancesFromHome]) (e.g.: after chasing the party in a battle). It is not when it is
 * not on the battlefield, or when home can not be walked to, as it could not head back home anyway.
 *
 * Distances from home: those of the battle unit's home, as computed once it claimed it.
 */
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
