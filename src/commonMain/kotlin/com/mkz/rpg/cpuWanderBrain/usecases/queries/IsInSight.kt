package com.mkz.rpg.cpuWanderBrain.usecases.queries

import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefieldSize
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.party.usecases.queries.SearchParty

/** The tiles the camera shows on each axis, as the battlefield view does (`BattlefieldView.VISIBLE_TILES`). */
const val VIEW_TILES = 6

/** The tiles around the ones the camera can show where the battle units are still in sight, as they may step into view. */
const val SIGHT_MARGIN = 1

/**
 * Whether the battle unit stands where the player may see it: around the party leader, on the tiles the camera can show,
 * and [SIGHT_MARGIN] tiles around them. The camera keeps the leader in its 2x2 centre, so on each axis it shows from
 * `VIEW_TILES / 2` tiles before to `VIEW_TILES / 2` tiles after the leader. Near a border, the camera stops scrolling, so
 * the area shifts to stay inside the battlefield.
 *
 * It is not without a party leader, or when either of them is not on the battlefield.
 */
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
        return isInSight(position.row, leaderPosition.row, size.rows) && isInSight(position.column, leaderPosition.column, size.columns)
    }

    // On one axis: the tiles the camera can show around the leader, shifted inside the battlefield, plus the margin.
    private fun isInSight(
        tile: Int,
        leaderTile: Int,
        tiles: Int,
    ): Boolean {
        val sightTiles = VIEW_TILES + 1
        val firstTile = (leaderTile - VIEW_TILES / 2).coerceAtMost(tiles - sightTiles).coerceAtLeast(0)
        return tile in (firstTile - SIGHT_MARGIN)..(firstTile + sightTiles - 1 + SIGHT_MARGIN)
    }
}
