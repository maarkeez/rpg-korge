package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory.Dto.GiveItemRefusalDto
import com.mkz.rpg.inventory.domain.InventoryMother.fullInventory
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.GiveMother
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class SearchGiveItemRefusalTest {
    private val give = GiveMother()
    private val searchItemReceivers = SearchItemReceivers(give.inventoryRepository, give.giveRule)
    private val searchGiveItemRefusal = SearchGiveItemRefusal(give.inventoryRepository, give.giveRule, searchItemReceivers)

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
        searchGiveItemRefusal(giverBattleUnitId = "knight-1", lootableItemId = "health-potion").shouldBeNull()
    }

    @Test
    fun `should refuse giving an item that is not stored, or the only stored copy when it is equipped`() {
        // Then
        searchGiveItemRefusal(giverBattleUnitId = "knight-1", lootableItemId = "mana-potion") shouldBe GiveItemRefusalDto.NOT_STORED
        searchGiveItemRefusal(giverBattleUnitId = "knight-1", lootableItemId = "hammer") shouldBe GiveItemRefusalDto.EQUIPPED
    }

    @Test
    fun `should refuse giving when the giver is alone in the party`() {
        // Given
        give.explore(listOf("knight-1"))
        // Then
        searchGiveItemRefusal(giverBattleUnitId = "knight-1", lootableItemId = "health-potion") shouldBe GiveItemRefusalDto.NO_ONE
    }

    @Test
    fun `should refuse giving when every other member has no space left`() {
        // Given
        give.inventoryRepository.create(fullInventory(battleUnitId = "viking-1"))
        give.inventoryRepository.create(fullInventory(battleUnitId = "mage-1"))
        // Then
        searchGiveItemRefusal(giverBattleUnitId = "knight-1", lootableItemId = "health-potion") shouldBe GiveItemRefusalDto.INVENTORIES_FULL
    }

    @Test
    fun `should refuse giving when the party does not explore`() {
        // Given
        give.battle()
        // Then
        searchGiveItemRefusal(giverBattleUnitId = "knight-1", lootableItemId = "health-potion") shouldBe GiveItemRefusalDto.NOT_EXPLORING
    }
}
