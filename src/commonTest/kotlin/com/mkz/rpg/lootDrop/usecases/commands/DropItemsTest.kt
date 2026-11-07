package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropEvent.ItemDropped
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DropItemsTest {
    private val lootDropRepository = InMemoryLootDropRepository()
    private val eventBus = FakeEventBus()
    private val dropItems = DropItems(lootDropRepository, eventBus)

    @Test
    fun `DropItems should drop the item on the tile and announce it when requested`() {
        // Given
        lootDropRepository.create(LootDrop.ownerless())
        // When
        dropItems(lootableItemIds = listOf("health-potion"), row = 0, column = 1)
        // Then
        lootDropRepository.searchByUnitId(LootDrop.OWNERLESS_UNIT_ID)!!.toDto().droppedItems shouldBe mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))
        eventBus.shouldHavePublishedEvents(ItemDropped(lootableItemId = "health-potion", row = 0, column = 1))
    }

    @Test
    fun `DropItems should drop every copy and announce each when the same item is requested twice`() {
        // Given
        lootDropRepository.create(LootDrop.ownerless())
        // When
        dropItems(lootableItemIds = listOf("health-potion", "health-potion"), row = 0, column = 1)
        // Then
        lootDropRepository.searchByUnitId(LootDrop.OWNERLESS_UNIT_ID)!!.toDto().droppedItems shouldBe mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion", "health-potion"))
        eventBus.shouldHavePublishedEvents(
            ItemDropped(lootableItemId = "health-potion", row = 0, column = 1),
            ItemDropped(lootableItemId = "health-potion", row = 0, column = 1),
        )
    }

    @Test
    fun `DropItems should fail when the loot drops are not initialised`() {
        // When
        val dropping = { dropItems(lootableItemIds = listOf("health-potion"), row = 0, column = 1) }
        // Then
        shouldThrow<IllegalArgumentException> { dropping() }
    }
}
