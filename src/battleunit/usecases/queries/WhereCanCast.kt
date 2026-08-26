package battleunit.usecases.queries

import ability.usecases.queries.SearchAbilityById
import battlefield.domain.Battlefield
import battlefield.usecases.queries.SearchOccupant
import battlefield.usecases.queries.SearchPosition
import battleunit.domain.BattleUnit
import battleunit.domain.BattleUnitRepository

class WhereCanCast(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchPosition: SearchPosition,
    private val searchAbilityById: SearchAbilityById,
    private val searchOccupant: SearchOccupant,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
    ): List<PositionDto> {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return emptyList()
        val currentPosition = searchPosition(battleUnitId) ?: return emptyList()
        val ability = searchAbilityById(abilityId) ?: return emptyList()
        return when(ability.targetPattern) {
            "ADJACENT_ENEMY" -> searchAdjacentEnemyPositions(battleUnit, currentPosition)
            else -> emptyList()
        }
    }

    fun searchAdjacentEnemyPositions(battleUnit: BattleUnit, currentPosition: Battlefield.Dto.PositionDto) =
        buildList {
            val distance = 1
            for (row in currentPosition.row - distance..currentPosition.row + distance) {
                for (column in currentPosition.column - distance..currentPosition.column + distance) {
                    val occupantId = searchOccupant(row = row, column = column) ?: continue
                    val occupantBattleUnit = battleUnitRepository.searchById(occupantId) ?: continue
                    if(occupantBattleUnit.isSamePlayer(battleUnit)) continue
                    val enemyPosition = searchPosition(occupantBattleUnit.toDto().id)!!
                    add(PositionDto(row = enemyPosition.row, column = enemyPosition.column))
                }
            }
        }

    data class PositionDto(val row: Int, val column: Int)
}
