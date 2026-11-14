package com.mkz.tessavar.lootDrop.adapters.events

import com.mkz.tessavar.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.tessavar.lootDrop.domain.LootDrop
import com.mkz.tessavar.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.tessavar.lootDrop.domain.LootDropEvent.RequestDropItems
import com.mkz.tessavar.lootDrop.usecases.commands.DropItems
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestDropItemsTest {
    private val eventBus = InMemoryEventBus()
    private val lootDropRepository = InMemoryLootDropRepository()
    private val onRequestDropItems = OnRequestDropItems(DropItems(lootDropRepository, eventBus), eventBus)

    @Test
    fun `should drop the items on their tile when their drop is requested`() {
        // Given
        lootDropRepository.create(LootDrop.ownerless())
        eventBus.publish(RequestDropItems(lootableItemIds = listOf("health-potion", "hammer"), row = 0, column = 1))
        // When
        eventBus.dispatch()
        // Then
        lootDropRepository.searchByUnitId(LootDrop.OWNERLESS_UNIT_ID)!!.toDto().droppedItems shouldBe mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion", "hammer"))
    }
}
