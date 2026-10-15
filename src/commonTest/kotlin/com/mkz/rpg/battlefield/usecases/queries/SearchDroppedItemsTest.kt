package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchDroppedItemsTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val searchDroppedItems = SearchDroppedItems(battlefieldRepository)

    @Test
    fun `should return the items of each tile with dropped items when items were dropped`() {
        // Given
        val droppedItems =
            mapOf(
                PositionDto(row = 0, column = 0) to listOf("health-potion"),
                PositionDto(row = 0, column = 2) to listOf("health-potion", "helmet"),
            )
        battlefieldRepository.create(battlefield(". . .", droppedItems = droppedItems))
        // When
        val result = searchDroppedItems()
        // Then
        result shouldBe droppedItems
    }

    @Test
    fun `should return no items when no battlefield exists`() {
        // Given
        // When
        val result = searchDroppedItems()
        // Then
        result.shouldBeEmpty()
    }
}
