package battleunit.usecases.services

import kotlin.math.abs

class DistanceService {
    fun manhattanDistance(fromRow: Int, fromColumn: Int, toRow: Int, toColumn: Int): Int =
        (abs(fromRow - toRow) + abs(fromColumn - toColumn))
}
