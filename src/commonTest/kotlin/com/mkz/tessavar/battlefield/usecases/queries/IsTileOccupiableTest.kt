package com.mkz.tessavar.battlefield.usecases.queries

import com.mkz.tessavar.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class IsTileOccupiableTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val isTileOccupiable = IsTileOccupiable(battlefieldRepository)

    @Test
    fun `should return true when the tile is occupiable and a battle unit stands on it`() {
        // Given
        battlefieldRepository.create(battlefield("A .", occupants = mapOf('A' to "battle-unit-a")))
        // When
        val result = isTileOccupiable(0, 0)
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `should return false when the tile is not occupiable`() {
        // Given
        battlefieldRepository.create(battlefield(". #"))
        // When
        val result = isTileOccupiable(0, 1)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when the tile is out of the battlefield`() {
        // Given
        battlefieldRepository.create(battlefield(". ."))
        // When
        val result = isTileOccupiable(0, 2)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when there is no battlefield`() {
        // When
        val result = isTileOccupiable(0, 0)
        // Then
        result.shouldBeFalse()
    }
}
