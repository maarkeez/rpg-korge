package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.services

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class MovementServiceTest {
    private val queries = InMemoryQueries()
    private val movementService =
        MovementService(
            searchBattleUnitById = queries.searchBattleUnitById,
            searchTilesThatCanBeOccupied = queries.searchTilesThatCanBeOccupied,
            canMoveTo = queries.canMoveTo,
        )

    @Test
    fun `should return only the tiles the battle unit can move to when computing the tiles where it can move`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1", unit = unit(movementRange = 2).toDto()))
        // The bottom right tile is in range, but too far to walk to.
        queries.battlefieldRepository.create(
            battlefield(
                """
                K . .
                # # .
                """,
                occupants = mapOf('K' to "battle-unit-1"),
            ),
        )
        // When
        val tilesWhereCanMove = movementService.tilesWhereCanMove("battle-unit-1")
        // Then
        tilesWhereCanMove.shouldContainExactly(
            TileDto(row = 0, column = 1),
            TileDto(row = 0, column = 2),
        )
    }

    @Test
    fun `should return an empty set when there are no tiles that can be occupied`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1"))
        queries.battlefieldRepository.create(battlefield("K #", occupants = mapOf('K' to "battle-unit-1")))
        // When
        val tilesWhereCanMove = movementService.tilesWhereCanMove("battle-unit-1")
        // Then
        tilesWhereCanMove.shouldBeEmpty()
    }
}
