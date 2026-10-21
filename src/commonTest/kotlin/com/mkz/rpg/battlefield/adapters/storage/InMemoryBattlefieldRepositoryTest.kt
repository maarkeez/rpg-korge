package com.mkz.rpg.battlefield.adapters.storage

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryBattlefieldRepositoryTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()

    @Test
    fun `Create should store the battlefield when the battlefield is created`() {
        // Given
        val createdBattlefield = battlefield()
        // When
        battlefieldRepository.create(createdBattlefield)
        // Then
        val storedBattlefield = battlefieldRepository.search()
        storedBattlefield shouldBe createdBattlefield
    }

    @Test
    fun `Update should replace the stored battlefield when the battlefield is updated`() {
        // Given
        val storedBattlefield = battlefield()
        battlefieldRepository.create(storedBattlefield)
        val updatedBattlefield = storedBattlefield.occupy(row = 0, column = 0, occupantId = "battle-unit-1")
        // When
        battlefieldRepository.update(updatedBattlefield)
        // Then
        val searchResult = battlefieldRepository.search()
        searchResult shouldBe updatedBattlefield
    }

    @Test
    fun `Search should return null when no battlefield is stored`() {
        // When
        val storedBattlefield = battlefieldRepository.search()
        // Then
        storedBattlefield.shouldBeNull()
    }
}
