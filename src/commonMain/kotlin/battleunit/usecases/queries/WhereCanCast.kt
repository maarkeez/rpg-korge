package battleunit.usecases.queries

import ability.usecases.queries.SearchAbilityById
import battlefield.domain.Battlefield
import battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import battlefield.usecases.queries.SearchOccupant
import battlefield.usecases.queries.SearchPosition
import battleunit.domain.BattleUnit
import battleunit.domain.BattleUnitRepository
import battleunit.usecases.services.DistanceService

class WhereCanCast(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchPosition: SearchPosition,
    private val searchAbilityById: SearchAbilityById,
    private val searchOccupant: SearchOccupant,
    private val distanceService: DistanceService,
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
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
            "SELF" -> listOf(PositionDto(row = currentPosition.row, column = currentPosition.column))
            "VACANT_TILE_ADJACENT_TO_BATTLE_UNIT" -> searchVacantTilesAdjacentToBattleUnitsExcluding(battleUnit.toDto().id)
            else -> emptyList()
        }
    }

    private fun searchVacantTilesAdjacentToBattleUnitsExcluding(id: String): List<PositionDto> {
        return buildList {
            val allBattleUnits = battleUnitRepository.searchAll()
                .filter { !it.isDefeated() }
                .filter { it.toDto().id != id }

            allBattleUnits.forEach { battleUnit ->
                val battleUnitPosition = searchPosition(battleUnit.toDto().id) ?: return@forEach
                val northPosition = PositionDto(
                    row = battleUnitPosition.row - 1,
                    column = battleUnitPosition.column,
                )
                val southPosition = PositionDto(
                    row = battleUnitPosition.row + 1,
                    column = battleUnitPosition.column,
                )
                val eastPosition = PositionDto(
                    row = battleUnitPosition.row,
                    column = battleUnitPosition.column + 1,
                )
                val westPosition = PositionDto(
                    row = battleUnitPosition.row,
                    column = battleUnitPosition.column - 1,
                )
                listOf(northPosition, southPosition, eastPosition, westPosition).forEach { position ->
                    if(canBattlefieldTileBeOccupied(position.row, position.column)) {
                        add(position)
                    }
                }
            }
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
                    val enemyDistance = distanceService.manhattanDistance(
                        fromRow = currentPosition.row,
                        fromColumn = currentPosition.column,
                        toRow = row,
                        toColumn = column
                    )
                    if(enemyDistance > 1) continue
                    add(PositionDto(row = enemyPosition.row, column = enemyPosition.column))
                }
            }
        }

    data class PositionDto(val row: Int, val column: Int)
}
