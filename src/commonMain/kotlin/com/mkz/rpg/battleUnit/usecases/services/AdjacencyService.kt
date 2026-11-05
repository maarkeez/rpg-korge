package com.mkz.rpg.battleUnit.usecases.services

import kotlin.math.abs

class AdjacencyService {
    /** Whether the two tiles share a side, which a tile and itself do not. */
    fun isAdjacent(
        fromRow: Int,
        fromColumn: Int,
        toRow: Int,
        toColumn: Int,
    ): Boolean = abs(fromRow - toRow) + abs(fromColumn - toColumn) == 1
}
