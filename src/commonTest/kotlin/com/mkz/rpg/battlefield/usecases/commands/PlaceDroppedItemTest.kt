package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.DroppedItemPlaced
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PlaceDroppedItemTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val eventBus = FakeEventBus()
    private val placeDroppedItem = PlaceDroppedItem(battlefieldRepository, eventBus)

    @Test
    fun `should place the item on its tile when the battlefield exists`() {
        // Given
        battlefieldRepository.create(battlefield(". ."))
        // When
        placeDroppedItem(lootableItemId = "health-potion", row = 0, column = 1)
        // Then
        battlefieldRepository.search()?.droppedItems() shouldBe mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))
        eventBus.shouldHavePublishedEvents(DroppedItemPlaced(lootableItemId = "health-potion", row = 0, column = 1))
    }

    @Test
    fun `should not place the item when no battlefield exists`() {
        // Given
        // When
        placeDroppedItem(lootableItemId = "health-potion", row = 0, column = 1)
        // Then
        battlefieldRepository.search().shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }
}
