package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.services.AdjacencyService
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.usecases.queries.IsTileVacant
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchVacantTiles

class WhereCanCast(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchPosition: SearchPosition,
    private val searchAbilityById: SearchAbilityById,
    private val searchOccupant: SearchOccupant,
    private val searchVacantTiles: SearchVacantTiles,
    private val adjacencyService: AdjacencyService,
    private val isTileVacant: IsTileVacant,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
    ): List<CastGroup> {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return emptyList()
        val currentPosition = searchPosition(battleUnitId) ?: return emptyList()
        val ability = searchAbilityById(abilityId) ?: return emptyList()
        return when (ability.targeting) {
            Ability.Dto.TargetingDto.ADJACENT_ENEMY -> searchAdjacentEnemyPositions(battleUnit, currentPosition).map { position -> CastGroup(listOf(position)) }
            Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES -> {
                val positions = searchAdjacentEnemyPositions(battleUnit, currentPosition)
                if (positions.isEmpty()) emptyList() else listOf(CastGroup(positions))
            }
            Ability.Dto.TargetingDto.CASTER -> listOf(CastGroup(listOf(PositionDto(row = currentPosition.row, column = currentPosition.column))))
            Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT -> searchVacantTilesAdjacentToBattleUnitsExcluding(battleUnit.toDto().id)
            Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_CASTER ->
                searchVacantTiles(occupantId = battleUnit.toDto().id, tilesAround = 1)
                    .filter { tilePosition ->
                        adjacencyService.isAdjacent(fromRow = currentPosition.row, fromColumn = currentPosition.column, toRow = tilePosition.row, toColumn = tilePosition.column)
                    }.map { tilePosition -> CastGroup(listOf(PositionDto(row = tilePosition.row, column = tilePosition.column))) }
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
                    if (isTileVacant(position.row, position.column)) {
                        add(CastGroup(listOf(position)))
                    }
                }
            }
        }
    }

    private fun searchAdjacentEnemyPositions(
        battleUnit: BattleUnit,
        currentPosition: Battlefield.Dto.PositionDto,
    ): List<PositionDto> =
        buildList {
            val tilesAround = 1
            for (row in currentPosition.row - tilesAround..currentPosition.row + tilesAround) {
                for (column in currentPosition.column - tilesAround..currentPosition.column + tilesAround) {
                    val occupantId = searchOccupant(row = row, column = column) ?: continue
                    val occupantBattleUnit = battleUnitRepository.searchById(occupantId) ?: continue
                    if (occupantBattleUnit.isSamePlayer(battleUnit)) continue
                    val enemyPosition = searchPosition(occupantBattleUnit.toDto().id)!!
                    if (!adjacencyService.isAdjacent(fromRow = currentPosition.row, fromColumn = currentPosition.column, toRow = row, toColumn = column)) continue
                    add(PositionDto(row = enemyPosition.row, column = enemyPosition.column))
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
