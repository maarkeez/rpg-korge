package battleunit.usecases.queries

import battlefield.usecases.queries.SearchPosition
import battleunit.domain.BattleUnitRepository
import battleunit.usecases.services.DistanceService
import kotlin.math.abs

class CanMoveTo(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchPosition: SearchPosition,
    private val distanceService: DistanceService,
) {
    operator fun invoke(
        battleUnitId: String,
        moveToRow: Int,
        moveToColumn: Int,
    ): Boolean {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return false
        val currentPosition = searchPosition(battleUnitId) ?: return false
        val distance = distanceService.manhattanDistance(
            fromRow = currentPosition.row,
            fromColumn = currentPosition.column,
            toRow = moveToRow,
            toColumn = moveToColumn
        )
        return battleUnit.canMoveDistance(distance)
    }
}
