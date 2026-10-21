package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestPickUpDroppedItem
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.commands.PickUpDroppedItem
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.maps.shouldBeEmpty
import kotlin.test.Test

class OnRequestPickUpDroppedItemTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val onRequestPickUpDroppedItem = OnRequestPickUpDroppedItem(PickUpDroppedItem(battlefieldRepository, eventBus), eventBus)

    @Test
    fun `should pick up the dropped item from its tile when its pick-up is requested`() {
        // Given
        battlefieldRepository.create(battlefield(". .", droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))))
        eventBus.publish(RequestPickUpDroppedItem(lootableItemId = "health-potion", row = 0, column = 1))
        // When
        eventBus.dispatch()
        // Then
        battlefieldRepository.search()?.droppedItems()?.shouldBeEmpty()
    }
}
