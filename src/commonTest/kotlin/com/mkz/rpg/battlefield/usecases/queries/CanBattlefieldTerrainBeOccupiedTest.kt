package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class CanBattlefieldTerrainBeOccupiedTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val canBattlefieldTerrainBeOccupied = CanBattlefieldTerrainBeOccupied(battlefieldRepository)

    @Test
    fun `should return true when the terrain can be occupied and a battle unit stands on it`() {
        // Given
        battlefieldRepository.create(battlefield("A .", occupants = mapOf('A' to "battle-unit-a")))
        // When
        val result = canBattlefieldTerrainBeOccupied(0, 0)
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `should return false when the terrain can not be occupied`() {
        // Given
        battlefieldRepository.create(battlefield(". #"))
        // When
        val result = canBattlefieldTerrainBeOccupied(0, 1)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when the tile is out of the battlefield`() {
        // Given
        battlefieldRepository.create(battlefield(". ."))
        // When
        val result = canBattlefieldTerrainBeOccupied(0, 2)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when there is no battlefield`() {
        // When
        val result = canBattlefieldTerrainBeOccupied(0, 0)
        // Then
        result.shouldBeFalse()
    }
}
