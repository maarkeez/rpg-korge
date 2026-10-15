package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldError.DroppedItemNotFound
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.DroppedItemPickedUp
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldBeEmpty
import kotlin.test.Test

class PickUpDroppedItemTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val eventBus = FakeEventBus()
    private val pickUpDroppedItem = PickUpDroppedItem(battlefieldRepository, eventBus)

    @Test
    fun `should remove the item from its tile when the item lies on the tile`() {
        // Given
        battlefieldRepository.create(battlefield(". .", droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))))
        // When
        pickUpDroppedItem(lootableItemId = "health-potion", row = 0, column = 1)
        // Then
        battlefieldRepository.search()?.droppedItems()?.shouldBeEmpty()
        eventBus.shouldHavePublishedEvents(DroppedItemPickedUp(lootableItemId = "health-potion", row = 0, column = 1))
    }

    @Test
    fun `should fail when the item does not lie on the tile`() {
        // Given
        battlefieldRepository.create(battlefield(". ."))
        // When
        val pickingUp = { pickUpDroppedItem(lootableItemId = "health-potion", row = 0, column = 1) }
        // Then
        shouldThrow<DroppedItemNotFound> { pickingUp() }
    }

    @Test
    fun `should not pick up the item when no battlefield exists`() {
        // Given
        // When
        pickUpDroppedItem(lootableItemId = "health-potion", row = 0, column = 1)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
