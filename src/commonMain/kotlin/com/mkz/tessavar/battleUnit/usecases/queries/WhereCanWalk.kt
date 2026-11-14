package com.mkz.tessavar.battleUnit.usecases.queries

import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.battlefield.usecases.queries.SearchVacantTiles

class WhereCanWalk(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchVacantTiles: SearchVacantTiles,
    private val canWalkTo: CanWalkTo,
) {
    operator fun invoke(battleUnitId: String): List<Battlefield.Dto.PositionDto> {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return emptyList()
        return searchVacantTiles(
            occupantId = battleUnit.toDto().id,
            tilesAround = battleUnit.toDto().remainingActions.remainingSteps,
        ).filter { position ->
            canWalkTo(
                battleUnitId = battleUnit.toDto().id,
                walkToRow = position.row,
                walkToColumn = position.column,
            )
        }
    }
}
