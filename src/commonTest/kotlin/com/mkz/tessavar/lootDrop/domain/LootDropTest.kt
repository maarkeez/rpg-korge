package com.mkz.tessavar.lootDrop.domain

import com.mkz.tessavar.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.tessavar.lootDrop.domain.LootDropError.DroppedItemNotFound
import com.mkz.tessavar.lootDrop.domain.LootDropError.DuplicatedLootDropLootTable
import com.mkz.tessavar.lootDrop.domain.LootDropError.EmptyLootDropLootTables
import com.mkz.tessavar.lootDrop.domain.LootDropEvent.DroppedItemLooted
import com.mkz.tessavar.lootDrop.domain.LootDropEvent.ItemDropped
import com.mkz.tessavar.lootDrop.domain.LootDropEvent.LootDropCreated
import com.mkz.tessavar.lootTable.domain.LootTableMother.lootTable
import com.mkz.tessavar.lootTable.domain.LootTableMother.lootableItemEntry
import com.mkz.tessavar.lootTable.domain.LootTableMother.nothingEntry
import com.mkz.tessavar.shared.domain.FixedRandom
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class LootDropTest {
    private val tierOne = lootTable(id = "tier-1", entries = listOf(lootableItemEntry(lootableItemId = "health-potion", weight = 10), nothingEntry(weight = 5))).toDto()

    @Test
    fun `Create should create loot drop when the dto is valid`() {
        // Given
        val lootDropDto = LootDropMother.lootDrop(lootTableIds = listOf("tier-1", "tier-2")).toDto()
        // When
        val createdLootDrop = LootDrop.create(lootDropDto)
        // Then
        createdLootDrop.toDto() shouldBe lootDropDto
    }

    @Test
    fun `Create should fail when there are no loot tables`() {
        // Given
        val lootDropDto = LootDropMother.lootDrop().toDto().copy(lootTableIds = emptyList())
        // When
        val result = runCatching { LootDrop.create(lootDropDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EmptyLootDropLootTables>()
    }

    @Test
    fun `Create should fail when a loot table is repeated`() {
        // Given
        val lootDropDto = LootDropMother.lootDrop().toDto().copy(lootTableIds = listOf("tier-1", "tier-1"))
        // When
        val result = runCatching { LootDrop.create(lootDropDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<DuplicatedLootDropLootTable>()
    }

    @Test
    fun `Roll should roll the lootable item when the roll falls within its weight`() {
        // Given
        val lootDrop = LootDropMother.lootDrop(lootTableIds = listOf("tier-1"))
        // When
        val rolledLootableItemId = lootDrop.roll(lootTables = listOf(tierOne), random = FixedRandom(roll = 9))
        // Then
        rolledLootableItemId shouldBe "health-potion"
    }

    @Test
    fun `Roll should roll nothing when the roll falls within the nothing weight`() {
        // Given
        val lootDrop = LootDropMother.lootDrop(lootTableIds = listOf("tier-1"))
        // When
        val rolledLootableItemId = lootDrop.roll(lootTables = listOf(tierOne), random = FixedRandom(roll = 10))
        // Then
        rolledLootableItemId.shouldBeNull()
    }

    @Test
    fun `Roll should roll a lootable item of the next loot table when the roll falls after the weights of the previous loot table`() {
        // Given
        val tierTwo = lootTable(id = "tier-2", entries = listOf(lootableItemEntry(lootableItemId = "mana-potion", weight = 5))).toDto()
        val lootDrop = LootDropMother.lootDrop(lootTableIds = listOf("tier-1", "tier-2"))
        // When
        val rolledLootableItemId = lootDrop.roll(lootTables = listOf(tierOne, tierTwo), random = FixedRandom(roll = 15))
        // Then
        rolledLootableItemId shouldBe "mana-potion"
    }

    @Test
    fun `Create should keep the dropped items when the dto has dropped items`() {
        // Given
        val droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion", "helmet"))
        val lootDropDto = LootDropMother.lootDrop(droppedItems = droppedItems).toDto()
        // When
        val createdLootDrop = LootDrop.create(lootDropDto)
        // Then
        createdLootDrop.toDto().droppedItems shouldBe droppedItems
    }

    @Test
    fun `Drop should drop the item on its tile and publish the event when an item is dropped`() {
        // Given
        val lootDrop = LootDropMother.lootDrop()
        // When
        val updatedLootDrop = lootDrop.drop(lootableItemId = "health-potion", row = 0, column = 1)
        // Then
        updatedLootDrop.toDto().droppedItems shouldBe mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))
        val (events, _) = updatedLootDrop.pullEvents()
        events.shouldContainExactly(ItemDropped(lootableItemId = "health-potion", row = 0, column = 1))
    }

    @Test
    fun `Drop should keep the items already dropped first when the tile has dropped items`() {
        // Given
        val lootDrop = LootDropMother.lootDrop(droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion")))
        // When
        val updatedLootDrop = lootDrop.drop(lootableItemId = "helmet", row = 0, column = 1)
        // Then
        updatedLootDrop.toDto().droppedItems shouldBe mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion", "helmet"))
    }

    @Test
    fun `DroppedItems should return the items lying on the tile in drop order when the unit dropped items on it`() {
        // Given
        val lootDrop =
            LootDropMother.lootDrop(
                droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion", "helmet"), PositionDto(row = 0, column = 2) to listOf("hammer")),
            )
        // When
        val droppedItems = lootDrop.droppedItems(row = 0, column = 1)
        // Then
        droppedItems.shouldContainExactly("health-potion", "helmet")
    }

    @Test
    fun `DroppedItems should return no items when the unit dropped nothing on the tile`() {
        // Given
        val lootDrop = LootDropMother.lootDrop(droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion")))
        // When
        val droppedItems = lootDrop.droppedItems(row = 0, column = 2)
        // Then
        droppedItems.shouldBeEmpty()
    }

    @Test
    fun `Loot should remove one copy of the item and publish the event when the item lies on the tile`() {
        // Given
        val lootDrop = LootDropMother.lootDrop(droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion", "helmet", "health-potion")))
        // When
        val updatedLootDrop = lootDrop.loot(lootableItemId = "health-potion", row = 0, column = 1)
        // Then
        updatedLootDrop.toDto().droppedItems shouldBe mapOf(PositionDto(row = 0, column = 1) to listOf("helmet", "health-potion"))
        val (events, _) = updatedLootDrop.pullEvents()
        events.shouldContainExactly(DroppedItemLooted(lootableItemId = "health-potion", row = 0, column = 1))
    }

    @Test
    fun `Loot should leave out the tile when its last item is looted`() {
        // Given
        val lootDrop = LootDropMother.lootDrop(droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion")))
        // When
        val updatedLootDrop = lootDrop.loot(lootableItemId = "health-potion", row = 0, column = 1)
        // Then
        updatedLootDrop.toDto().droppedItems.shouldBeEmpty()
    }

    @Test
    fun `Loot should fail when the item does not lie on the tile`() {
        // Given
        val lootDrop = LootDropMother.lootDrop(droppedItems = mapOf(PositionDto(row = 0, column = 0) to listOf("health-potion")))
        // When
        val result = runCatching { lootDrop.loot(lootableItemId = "health-potion", row = 0, column = 1) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<DroppedItemNotFound>()
    }

    @Test
    fun `LootDrop should hold no loot tables when it is ownerless`() {
        // When
        val lootDrop = LootDrop.ownerless()
        // Then
        lootDrop.toDto() shouldBe LootDrop.Dto(unitId = LootDrop.OWNERLESS_UNIT_ID, lootTableIds = emptyList(), droppedItems = emptyMap())
        lootDrop.pullEvents().first.shouldBeEmpty()
    }

    @Test
    fun `IsOwnerlessAndEmpty should be true only while the ownerless loot drop holds nothing`() {
        // Given
        val ownerless = LootDrop.ownerless()
        // When / Then
        ownerless.isOwnerlessAndEmpty() shouldBe true
        ownerless.drop(lootableItemId = "hammer", row = 0, column = 1).isOwnerlessAndEmpty() shouldBe false
        LootDropMother.lootDrop().isOwnerlessAndEmpty() shouldBe false
    }

    @Test
    fun `PullEvents should pull the created event when the loot drop has pending events`() {
        // Given
        val createdLootDrop = LootDrop.create(LootDropMother.lootDrop().toDto())
        // When
        val (events, _) = createdLootDrop.pullEvents()
        // Then
        events.shouldContainExactly(LootDropCreated(unitId = createdLootDrop.toDto().unitId))
    }

    @Test
    fun `PullEvents should clear the pending events when the events are pulled`() {
        // Given
        val createdLootDrop = LootDrop.create(LootDropMother.lootDrop().toDto())
        // When
        val (_, clearedLootDrop) = createdLootDrop.pullEvents()
        // Then
        clearedLootDrop.pullEvents().first.shouldBeEmpty()
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // Given
        val lootDrop = LootDropMother.lootDrop()
        // When
        val (events, _) = lootDrop.pullEvents()
        // Then
        events.shouldBeEmpty()
    }
}
