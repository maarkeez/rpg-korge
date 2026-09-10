package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SearchTilesThatCanBeOccupiedTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val searchTilesThatCanBeOccupied = SearchTilesThatCanBeOccupied(battlefieldRepository)

    @Test
    fun `should return tiles that can be occupied when the battle unit is deployed`() {
        // Given
        val battleUnitId = "battle-unit-1"
        val battlefield =
            battlefield()
                .occupy(1, 1, battleUnitId)
                .pullEvents()
                .second
        battlefieldRepository.create(battlefield)
        // When
        val result = searchTilesThatCanBeOccupied(battleUnitId, distance = 1)
        // Then
        assertThat(result).containsExactlyInAnyOrder(
            Battlefield.Dto.PositionDto(0, 0),
            Battlefield.Dto.PositionDto(0, 1),
            Battlefield.Dto.PositionDto(0, 2),
            Battlefield.Dto.PositionDto(1, 0),
            Battlefield.Dto.PositionDto(1, 2),
            Battlefield.Dto.PositionDto(2, 0),
            Battlefield.Dto.PositionDto(2, 1),
            Battlefield.Dto.PositionDto(2, 2),
        )
    }

    @Test
    fun `should return empty list when distance is less than or equal to zero`() {
        // Given
        val battleUnitId = "battle-unit-1"
        val battlefield =
            battlefield()
                .occupy(1, 1, battleUnitId)
                .pullEvents()
                .second
        battlefieldRepository.create(battlefield)
        // When
        val result = searchTilesThatCanBeOccupied(battleUnitId, distance = 0)
        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `should return empty list when no battlefield exists`() {
        // Given
        // When
        val result = searchTilesThatCanBeOccupied("battle-unit-1", distance = 1)
        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `should return empty list when the battle unit is not deployed`() {
        // Given
        battlefieldRepository.create(
            battlefield(),
        )
        // When
        val result = searchTilesThatCanBeOccupied("unknown-battle-unit", distance = 1)
        // Then
        assertThat(result).isEmpty()
    }
}
