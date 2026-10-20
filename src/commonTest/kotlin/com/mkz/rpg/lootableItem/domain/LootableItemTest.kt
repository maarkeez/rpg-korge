package com.mkz.rpg.lootableItem.domain

import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import com.mkz.rpg.lootableItem.domain.LootableItemError.InvalidLootType
import com.mkz.rpg.lootableItem.domain.LootableItemError.NegativePrice
import com.mkz.rpg.lootableItem.domain.LootableItemError.PriceAboveLimit
import com.mkz.rpg.lootableItem.domain.LootableItemEvent.LootableItemCreated
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippable
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class LootableItemTest {
    @Test
    fun `Create should create consumable lootable item when the dto is valid`() {
        // Given
        val lootableItemDto = LootableItemMother.consumableLootableItem().toDto()
        // When
        val createdLootableItem = LootableItem.create(lootableItemDto)
        // Then
        createdLootableItem.toDto() shouldBe lootableItemDto
    }

    @Test
    fun `Create should create equippable lootable item when the dto is valid`() {
        // Given
        val lootableItemDto = LootableItemMother.equippableLootableItem().toDto()
        // When
        val createdLootableItem = LootableItem.create(lootableItemDto)
        // Then
        createdLootableItem.toDto() shouldBe lootableItemDto
    }

    @Test
    fun `Create should create lootable item when the price is within the limits`() {
        // Given
        val lootableItemDtos = listOf(0, 999).map { price -> LootableItemMother.lootableItem(price = price).toDto() }
        // When
        val createdLootableItems = lootableItemDtos.map(LootableItem::create)
        // Then
        createdLootableItems.map { createdLootableItem -> createdLootableItem.toDto().price }.shouldContainExactly(0, 999)
    }

    @Test
    fun `Create should fail when the price is negative`() {
        // Given
        val lootableItemDto = LootableItemMother.lootableItem().toDto().copy(price = -1)
        // When
        val result = runCatching { LootableItem.create(lootableItemDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<NegativePrice>()
    }

    @Test
    fun `Create should fail when the price is above 999`() {
        // Given
        val lootableItemDto = LootableItemMother.lootableItem().toDto().copy(price = 1000)
        // When
        val result = runCatching { LootableItem.create(lootableItemDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<PriceAboveLimit>()
    }

    @Test
    fun `Create should fail when the consumable loot type is invalid`() {
        // Given
        val lootTypeDto = LootTypeDto.consumable(consumable()).copy(consumable = null)
        val lootableItemDto = LootableItemMother.lootableItem().toDto().copy(lootType = lootTypeDto)
        // When
        val result = runCatching { LootableItem.create(lootableItemDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<InvalidLootType>()
    }

    @Test
    fun `Create should fail when the equippable loot type is invalid`() {
        // Given
        val lootTypeDto = LootTypeDto.equippable(equippable()).copy(equippable = null)
        val lootableItemDto = LootableItemMother.lootableItem().toDto().copy(lootType = lootTypeDto)
        // When
        val result = runCatching { LootableItem.create(lootableItemDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<InvalidLootType>()
    }

    @Test
    fun `Create should create equippable lootable item when the dto has any equipment zone`() {
        // Given
        val lootableItemDtos = EquipmentZoneDto.entries.map { equipmentZone -> LootableItemMother.equippableLootableItem(equippable = equippable(equipmentZone = equipmentZone)).toDto() }
        // When
        val createdLootableItems = lootableItemDtos.map(LootableItem::create)
        // Then
        assertSoftly {
            createdLootableItems.zip(lootableItemDtos).forEach { (createdLootableItem, lootableItemDto) ->
                withClue("Equipment zone ${lootableItemDto.lootType.equippable?.equipmentZone}") {
                    createdLootableItem.toDto() shouldBe lootableItemDto
                }
            }
        }
    }

    @Test
    fun `PullEvents should pull the created event when the lootable item has pending events`() {
        // Given
        val createdLootableItem = LootableItem.create(LootableItemMother.lootableItem().toDto())
        // When
        val (events, _) = createdLootableItem.pullEvents()
        // Then
        events.shouldContainExactly(LootableItemCreated(lootableItemId = createdLootableItem.toDto().id))
    }

    @Test
    fun `PullEvents should clear the pending events when the events are pulled`() {
        // Given
        val createdLootableItem = LootableItem.create(LootableItemMother.lootableItem().toDto())
        // When
        val (_, clearedLootableItem) = createdLootableItem.pullEvents()
        // Then
        clearedLootableItem.pullEvents().first.shouldBeEmpty()
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // Given
        val lootableItem = LootableItemMother.lootableItem()
        // When
        val (events, _) = lootableItem.pullEvents()
        // Then
        events.shouldBeEmpty()
    }
}
