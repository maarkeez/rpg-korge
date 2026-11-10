package com.mkz.rpg.cpuWanderBrain.usecases.queries

import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefieldSize
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.party.usecases.queries.SearchParty

const val VISIBLE_TILES = 6

const val SIGHT_MARGIN = 1

class IsInSight(
    private val searchParty: SearchParty,
    private val searchPosition: SearchPosition,
    private val searchBattlefieldSize: SearchBattlefieldSize,
) {
    operator fun invoke(battleUnitId: String): Boolean {
        val leaderId = searchParty()?.leaderBattleUnitId ?: return false
        val leaderPosition = searchPosition(occupantId = leaderId) ?: return false
        val position = searchPosition(occupantId = battleUnitId) ?: return false
        val size = searchBattlefieldSize() ?: return false
        return isInSightOnAxis(position.row, leaderPosition.row, size.rows) && isInSightOnAxis(position.column, leaderPosition.column, size.columns)
    }

    private fun isInSightOnAxis(
        tile: Int,
        leaderTile: Int,
        tiles: Int,
    ): Boolean {
        val sightTiles = VISIBLE_TILES + 1
        val firstTile = (leaderTile - VISIBLE_TILES / 2).coerceAtMost(tiles - sightTiles).coerceAtLeast(0)
        return tile in (firstTile - SIGHT_MARGIN)..(firstTile + sightTiles - 1 + SIGHT_MARGIN)
    }
}
