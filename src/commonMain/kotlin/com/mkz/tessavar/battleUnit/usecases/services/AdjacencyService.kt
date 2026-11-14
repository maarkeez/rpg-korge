package com.mkz.tessavar.battleUnit.usecases.services

import kotlin.math.abs

class AdjacencyService {
    fun isAdjacent(
        fromRow: Int,
        fromColumn: Int,
        toRow: Int,
        toColumn: Int,
    ): Boolean = abs(fromRow - toRow) + abs(fromColumn - toColumn) == 1
}
