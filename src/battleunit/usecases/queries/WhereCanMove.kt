package battleunit.usecases.queries

import battlefield.domain.Battlefield
import battlefield.usecases.queries.SearchPosition
import battlefield.usecases.queries.SearchTilesThatCanBeOccupied
import battleunit.domain.BattleUnitRepository
import battleunit.usecases.services.DistanceService
import kotlin.math.abs

class WhereCanMove(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchTilesThatCanBeOccupied: SearchTilesThatCanBeOccupied,
    private val canMoveTo: CanMoveTo,
) {
    operator fun invoke(
        battleUnitId: String,
    ): List<Battlefield.Dto.PositionDto> {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return emptyList()
        return searchTilesThatCanBeOccupied(
            battleUnitId = battleUnit.toDto().id,
            distance = battleUnit.toDto().remainingTurnActions.remainingSteps
        ).filter { position ->
            canMoveTo(
                battleUnitId = battleUnit.toDto().id,
                moveToRow = position.row,
                moveToColumn = position.column,
            )
        }
    }
}
