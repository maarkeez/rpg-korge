package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropEvent.RequestDropItems
import com.mkz.rpg.lootDrop.usecases.commands.DropItems
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
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
