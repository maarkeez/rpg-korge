package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory.Dto.GiveRefusalDto
import com.mkz.rpg.inventory.domain.InventoryMother.fullInventory
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.GiveMother
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class SearchGiveRefusalTest {
    private val give = GiveMother()
    private val searchGiveReceivers = SearchGiveReceivers(give.inventoryRepository, give.giveRule)
    private val searchGiveRefusal = SearchGiveRefusal(give.inventoryRepository, give.giveRule, searchGiveReceivers)

    // The knight carries a hammer, equipped, and a health potion. The viking and the mage have space left.
    @BeforeTest
    fun setUp() {
        give.inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("hammer", "health-potion"), equippedItems = mapOf(WEAPON to "hammer")))
        give.inventoryRepository.create(inventory(battleUnitId = "viking-1"))
        give.inventoryRepository.create(inventory(battleUnitId = "mage-1"))
    }

    @Test
    fun `should not refuse giving a stored copy that is not equipped while some member has space left`() {
        // Given
        give.inventoryRepository.create(fullInventory(battleUnitId = "viking-1"))
        // Then
        searchGiveRefusal(giverBattleUnitId = "knight-1", lootableItemId = "health-potion").shouldBeNull()
    }

    @Test
    fun `should refuse giving an item that is not stored, or the only stored copy when it is equipped`() {
        // Then
        searchGiveRefusal(giverBattleUnitId = "knight-1", lootableItemId = "mana-potion") shouldBe GiveRefusalDto.NOT_STORED
        searchGiveRefusal(giverBattleUnitId = "knight-1", lootableItemId = "hammer") shouldBe GiveRefusalDto.EQUIPPED
    }

    @Test
    fun `should refuse giving when the giver is alone in the party`() {
        // Given
        give.explore(listOf("knight-1"))
        // Then
        searchGiveRefusal(giverBattleUnitId = "knight-1", lootableItemId = "health-potion") shouldBe GiveRefusalDto.NO_ONE
    }

    @Test
    fun `should refuse giving when every other member has no space left`() {
        // Given
        give.inventoryRepository.create(fullInventory(battleUnitId = "viking-1"))
        give.inventoryRepository.create(fullInventory(battleUnitId = "mage-1"))
        // Then
        searchGiveRefusal(giverBattleUnitId = "knight-1", lootableItemId = "health-potion") shouldBe GiveRefusalDto.BAGS_FULL
    }

    @Test
    fun `should refuse giving when the party does not explore`() {
        // Given
        give.battle()
        // Then
        searchGiveRefusal(giverBattleUnitId = "knight-1", lootableItemId = "health-potion") shouldBe GiveRefusalDto.NOT_EXPLORING
    }
}
