package battleUnit.usecases.queries

import battleUnit.domain.BattleUnitRepository
import battlefield.domain.Battlefield
import battlefield.usecases.queries.SearchTilesThatCanBeOccupied

class WhereCanMove(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchTilesThatCanBeOccupied: SearchTilesThatCanBeOccupied,
    private val canMoveTo: CanMoveTo,
) {
    operator fun invoke(battleUnitId: String): List<Battlefield.Dto.PositionDto> {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return emptyList()
        return searchTilesThatCanBeOccupied(
            battleUnitId = battleUnit.toDto().id,
            distance = battleUnit.toDto().remainingTurnActions.remainingSteps,
        ).filter { position ->
            canMoveTo(
                battleUnitId = battleUnit.toDto().id,
                moveToRow = position.row,
                moveToColumn = position.column,
            )
        }
    }
}
