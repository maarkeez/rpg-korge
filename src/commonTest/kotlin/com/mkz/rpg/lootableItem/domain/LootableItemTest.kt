package com.mkz.rpg.lootableItem.domain

import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto
import com.mkz.rpg.lootableItem.domain.LootableItemError.InvalidLootType
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippable
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
}
