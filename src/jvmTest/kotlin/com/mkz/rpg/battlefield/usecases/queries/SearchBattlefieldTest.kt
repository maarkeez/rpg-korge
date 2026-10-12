package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SearchBattlefieldTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val searchBattlefield = SearchBattlefield(battlefieldRepository)

    @Test
    fun `should return battlefield when one exists`() {
        // Given
        val battlefield = battlefield()
        battlefieldRepository.create(battlefield)
        // When
        val result = searchBattlefield()
        // Then
        result shouldBe battlefield.toDto()
    }

    @Test
    fun `should return null when no battlefield exists`() {
        // Given
        // When
        val result = searchBattlefield()
        // Then
        result.shouldBeNull()
    }
}
