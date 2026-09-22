package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CanBattlefieldTileBeOccupiedTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val searchTerrainById = mock<SearchTerrainById>()
    private val canBattlefieldTileBeOccupied = CanBattlefieldTileBeOccupied(searchTerrainById, battlefieldRepository)

    @Test
    fun `should return true when the tile is vacant and within boundaries`() {
        // Given
        battlefieldRepository.create(
            battlefield(),
        )
        whenever(searchTerrainById(any())).thenReturn(occupiableTerrain().toDto())
        // When
        val result = canBattlefieldTileBeOccupied(1, 1)
        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when the tile is occupied`() {
        // Given
        val battlefield =
            battlefield()
                .occupy(1, 1, "battle-unit-1")
                .pullEvents()
                .second
        battlefieldRepository.create(battlefield)
        // When
        val result = canBattlefieldTileBeOccupied(1, 1)
        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when the tile is out of boundaries`() {
        // Given
        battlefieldRepository.create(
            battlefield(),
        )
        // When
        val result = canBattlefieldTileBeOccupied(5, 5)
        // Then
        assertThat(result).isFalse()
    }
}
