package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SearchOccupantTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val searchOccupant = SearchOccupant(battlefieldRepository)

    @Test
    fun `should return occupant when the tile is occupied`() {
        // Given
        val battleUnitId = "battle-unit-1"
        val battlefield =
            battlefield()
                .occupy(1, 1, battleUnitId)
                .pullEvents()
                .second
        battlefieldRepository.create(battlefield)
        // When
        val result = searchOccupant(1, 1)
        // Then
        assertThat(result).isEqualTo(battleUnitId)
    }

    @Test
    fun `should return null when the tile is vacant`() {
        // Given
        battlefieldRepository.create(battlefield())
        // When
        val result = searchOccupant(1, 1)
        // Then
        assertThat(result).isNull()
    }
}
