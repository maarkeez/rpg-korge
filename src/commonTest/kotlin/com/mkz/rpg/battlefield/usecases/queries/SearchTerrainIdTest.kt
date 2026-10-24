package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchTerrainIdTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val searchTerrainId = SearchTerrainId(battlefieldRepository)

    @Test
    fun `should return the terrain of the tile`() {
        // Given
        battlefieldRepository.create(battlefield(". #"))
        // When
        val result = searchTerrainId(row = 0, column = 1)
        // Then
        result shouldBe "void"
    }

    @Test
    fun `should return null out of the battlefield`() {
        // Given
        battlefieldRepository.create(battlefield(". #"))
        // When
        val result = searchTerrainId(row = 1, column = 0)
        // Then
        result.shouldBeNull()
    }

    @Test
    fun `should return null when no battlefield exists`() {
        // When
        val result = searchTerrainId(row = 0, column = 0)
        // Then
        result.shouldBeNull()
    }
}
