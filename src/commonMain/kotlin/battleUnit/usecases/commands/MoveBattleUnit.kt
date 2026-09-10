package battleUnit.usecases.commands

import battleUnit.domain.BattleUnitRepository
import battleUnit.usecases.services.DistanceService
import battlefield.usecases.queries.SearchPosition
import shared.domain.EventBus

class MoveBattleUnit(
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
    private val searchPosition: SearchPosition,
    private val distanceService: DistanceService,
) {
    operator fun invoke(
        battleUnitId: String,
        moveToRow: Int,
        moveToColumn: Int,
    ) {
        val storedBattleUnit = battleUnitRepository.searchById(battleUnitId) ?: return
        val currentPosition = searchPosition(battleUnitId) ?: return
        val distance =
            distanceService.manhattanDistance(
                fromRow = currentPosition.row,
                fromColumn = currentPosition.column,
                toRow = moveToRow,
                toColumn = moveToColumn,
            )
        val (events, battleUnit) =
            storedBattleUnit
                .move(
                    distance = distance,
                    fromRow = currentPosition.row,
                    fromColumn = currentPosition.column,
                    toRow = moveToRow,
                    toColumn = moveToColumn,
                ).pullEvents()
        battleUnitRepository.create(battleUnit)
        eventBus.publish(events)
    }
}
