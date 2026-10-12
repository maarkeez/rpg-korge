package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchPositionTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val searchPosition = SearchPosition(battlefieldRepository)

    @Test
    fun `should return position when the battle unit is deployed`() {
        // Given
        val battleUnitId = "battle-unit-1"
        val battlefield =
            battlefield()
                .occupy(1, 1, battleUnitId)
                .pullEvents()
                .second
        battlefieldRepository.create(battlefield)
        // When
        val result = searchPosition(battleUnitId)
        // Then
        result shouldBe Battlefield.Dto.PositionDto(1, 1)
    }

    @Test
    fun `should return null when the battle unit is not deployed`() {
        // Given
        battlefieldRepository.create(battlefield())
        // When
        val result = searchPosition("unknown-battle-unit")
        // Then
        result.shouldBeNull()
    }
}
