package com.mkz.rpg.battlefield.adapters.storage

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class InMemoryBattlefieldRepositoryTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()

    @Nested
    inner class Create {
        @Test
        fun `should store the battlefield when the battlefield is created`() {
            // Given
            val createdBattlefield = battlefield()
            // When
            battlefieldRepository.create(createdBattlefield)
            // Then
            val storedBattlefield = battlefieldRepository.search()
            assertThat(storedBattlefield).isEqualTo(createdBattlefield)
        }
    }

    @Nested
    inner class Update {
        @Test
        fun `should replace the stored battlefield when the battlefield is updated`() {
            // Given
            val storedBattlefield = battlefield()
            battlefieldRepository.create(storedBattlefield)
            val updatedBattlefield = storedBattlefield.occupy(row = 0, column = 0, battleUnitId = "battle-unit-1")
            // When
            battlefieldRepository.update(updatedBattlefield)
            // Then
            val searchResult = battlefieldRepository.search()
            assertThat(searchResult).isEqualTo(updatedBattlefield)
        }
    }

    @Nested
    inner class Search {
        @Test
        fun `should return null when no battlefield is stored`() {
            // When
            val storedBattlefield = battlefieldRepository.search()
            // Then
            assertThat(storedBattlefield).isNull()
        }
    }
}
