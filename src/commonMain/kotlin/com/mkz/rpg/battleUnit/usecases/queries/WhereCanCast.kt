package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.services.DistanceService
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition

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
    ): List<CastGroup> {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return emptyList()
        val currentPosition = searchPosition(battleUnitId) ?: return emptyList()
        val ability = searchAbilityById(abilityId) ?: return emptyList()
        return when (ability.targetPattern) {
            Ability.Dto.TargetPatternDto.ADJACENT_ENEMY -> searchAdjacentEnemyPositions(battleUnit, currentPosition)
            Ability.Dto.TargetPatternDto.SELF -> listOf(CastGroup(listOf(PositionDto(row = currentPosition.row, column = currentPosition.column))))
            Ability.Dto.TargetPatternDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT -> searchVacantTilesAdjacentToBattleUnitsExcluding(battleUnit.toDto().id)
        }
    }

    private fun searchVacantTilesAdjacentToBattleUnitsExcluding(id: String): List<CastGroup> {
        return buildList {
            val allBattleUnits =
                battleUnitRepository
                    .searchAll()
                    .filter { !it.isDefeated() }
                    .filter { it.toDto().id != id }

            allBattleUnits.forEach { battleUnit ->
                val battleUnitPosition = searchPosition(battleUnit.toDto().id) ?: return@forEach
                val northPosition =
                    PositionDto(
                        row = battleUnitPosition.row - 1,
                        column = battleUnitPosition.column,
                    )
                val southPosition =
                    PositionDto(
                        row = battleUnitPosition.row + 1,
                        column = battleUnitPosition.column,
                    )
                val eastPosition =
                    PositionDto(
                        row = battleUnitPosition.row,
                        column = battleUnitPosition.column + 1,
                    )
                val westPosition =
                    PositionDto(
                        row = battleUnitPosition.row,
                        column = battleUnitPosition.column - 1,
                    )
                listOf(northPosition, southPosition, eastPosition, westPosition).forEach { position ->
                    if (canBattlefieldTileBeOccupied(position.row, position.column)) {
                        add(CastGroup(listOf(position)))
                    }
                }
            }
        }
    }

    private fun searchAdjacentEnemyPositions(
        battleUnit: BattleUnit,
        currentPosition: Battlefield.Dto.PositionDto,
    ): List<CastGroup> =
        buildList {
            val distance = 1
            for (row in currentPosition.row - distance..currentPosition.row + distance) {
                for (column in currentPosition.column - distance..currentPosition.column + distance) {
                    val occupantId = searchOccupant(row = row, column = column) ?: continue
                    val occupantBattleUnit = battleUnitRepository.searchById(occupantId) ?: continue
                    if (occupantBattleUnit.isSamePlayer(battleUnit)) continue
                    val enemyPosition = searchPosition(occupantBattleUnit.toDto().id)!!
                    val enemyDistance =
                        distanceService.manhattanDistance(
                            fromRow = currentPosition.row,
                            fromColumn = currentPosition.column,
                            toRow = row,
                            toColumn = column,
                        )
                    if (enemyDistance > 1) continue
                    add(CastGroup(listOf(PositionDto(row = enemyPosition.row, column = enemyPosition.column))))
                }
            }
        }

    data class CastGroup(
        val positions: List<PositionDto>,
    )

    data class PositionDto(
        val row: Int,
        val column: Int,
    )
}
