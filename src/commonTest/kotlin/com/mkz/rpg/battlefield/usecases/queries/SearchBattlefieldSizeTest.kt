package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchBattlefieldSizeTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val searchBattlefieldSize = SearchBattlefieldSize(battlefieldRepository)

    @Test
    fun `should return the rows and the columns of the battlefield`() {
        // Given
        battlefieldRepository.create(battlefield(rows = 2, columns = 5))
        // When
        val result = searchBattlefieldSize()
        // Then
        result shouldBe Battlefield.Dto.SizeDto(rows = 2, columns = 5)
    }

    @Test
    fun `should return null when no battlefield exists`() {
        // When
        val result = searchBattlefieldSize()
        // Then
        result.shouldBeNull()
    }
}
