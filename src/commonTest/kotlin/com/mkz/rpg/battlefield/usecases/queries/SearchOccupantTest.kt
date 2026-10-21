package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchOccupantTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val searchOccupant = SearchOccupant(battlefieldRepository)

    @Test
    fun `should return occupant when the tile is occupied`() {
        // Given
        val occupantId = "battle-unit-1"
        val battlefield =
            battlefield()
                .occupy(1, 1, occupantId)
                .pullEvents()
                .second
        battlefieldRepository.create(battlefield)
        // When
        val result = searchOccupant(1, 1)
        // Then
        result shouldBe occupantId
    }

    @Test
    fun `should return null when the tile is vacant`() {
        // Given
        battlefieldRepository.create(battlefield())
        // When
        val result = searchOccupant(1, 1)
        // Then
        result.shouldBeNull()
    }
}
