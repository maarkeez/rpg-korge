package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchVacantTiles

class WhereCanMove(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchVacantTiles: SearchVacantTiles,
    private val canMoveTo: CanMoveTo,
) {
    operator fun invoke(battleUnitId: String): List<Battlefield.Dto.PositionDto> {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return emptyList()
        return searchVacantTiles(
            occupantId = battleUnit.toDto().id,
            tilesAround = battleUnit.toDto().remainingTurnActions.remainingSteps,
        ).filter { position ->
            canMoveTo(
                battleUnitId = battleUnit.toDto().id,
                moveToRow = position.row,
                moveToColumn = position.column,
            )
        }
    }
}
