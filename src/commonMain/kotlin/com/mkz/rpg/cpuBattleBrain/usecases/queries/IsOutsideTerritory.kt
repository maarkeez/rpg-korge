package com.mkz.rpg.cpuBattleBrain.usecases.queries

import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.cpuBattleBrain.usecases.services.WANDER_DISTANCE

/**
 * Whether the battle unit stands outside its territory: further than [WANDER_DISTANCE] tiles from home, walking around
 * void and harmful terrain ([SearchDistancesFromHome]) (e.g.: after chasing the party in a battle). It is not when it is
 * not on the battlefield, or when home can not be walked to, as it could not head back home anyway.
 */
class IsOutsideTerritory(
    private val searchPosition: SearchPosition,
    private val searchDistancesFromHome: SearchDistancesFromHome,
) {
    operator fun invoke(
        battleUnitId: String,
        homeRow: Int,
        homeColumn: Int,
    ): Boolean {
        val position = searchPosition(occupantId = battleUnitId) ?: return false
        val distanceFromHome = searchDistancesFromHome(homeRow = homeRow, homeColumn = homeColumn)[position] ?: return false
        return distanceFromHome > WANDER_DISTANCE
    }
}
