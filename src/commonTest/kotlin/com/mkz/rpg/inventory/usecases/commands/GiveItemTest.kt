package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.domain.InventoryError.EquippedItemCanNotBeGiven
import com.mkz.rpg.inventory.domain.InventoryError.NoStorageSpaceLeft
import com.mkz.rpg.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.rpg.inventory.domain.InventoryError.NotExploringTheParty
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemGiven
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemReceived
import com.mkz.rpg.inventory.domain.InventoryMother.fullInventory
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.GiveMother
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.BeforeTest
import kotlin.test.Test

class GiveItemTest {
    private val give = GiveMother()
    private val eventBus = FakeEventBus()
    private val giveItem = GiveItem(give.inventoryRepository, give.giveRule, eventBus)

    // The knight carries a hammer, equipped, and two health potions. The viking carries a mana potion, and the mage's bag is full.
    @BeforeTest
    fun setUp() {
        give.inventoryRepository.create(inventory(battleUnitId = "knight-1", storedItems = listOf("hammer", "health-potion", "health-potion"), equippedItems = mapOf(WEAPON to "hammer")))
        give.inventoryRepository.create(inventory(battleUnitId = "viking-1", storedItems = listOf("mana-potion")))
        give.inventoryRepository.create(fullInventory(battleUnitId = "mage-1"))
        give.inventoryRepository.create(inventory(battleUnitId = "rat-1"))
    }

    @Test
    fun `should move one copy of the item to the end of the receiver's bag`() {
        // When
        giveItem(giverBattleUnitId = "knight-1", receiverBattleUnitId = "viking-1", lootableItemId = "health-potion")
        // Then
        give.storedItems("knight-1")!!.shouldContainExactly("hammer", "health-potion")
        give.storedItems("viking-1")!!.shouldContainExactly("mana-potion", "health-potion")
        eventBus.shouldHavePublishedEvents(
            ItemGiven(battleUnitId = "knight-1", receiverBattleUnitId = "viking-1", lootableItemId = "health-potion"),
            ItemReceived(battleUnitId = "viking-1", giverBattleUnitId = "knight-1", lootableItemId = "health-potion"),
        )
    }

    @Test
    fun `should fail, keeping both bags, when the only stored copy is equipped`() {
        // Then
        shouldThrow<EquippedItemCanNotBeGiven> { giveItem(giverBattleUnitId = "knight-1", receiverBattleUnitId = "viking-1", lootableItemId = "hammer") }
        give.storedItems("viking-1")!!.shouldContainExactly("mana-potion")
    }

    @Test
    fun `should fail, keeping both bags, when the receiver has no space left`() {
        // Then
        shouldThrow<NoStorageSpaceLeft> { giveItem(giverBattleUnitId = "knight-1", receiverBattleUnitId = "mage-1", lootableItemId = "health-potion") }
        give.storedItems("knight-1")!!.shouldContainExactly("hammer", "health-potion", "health-potion")
    }

    @Test
    fun `should fail when the receiver is no party member, or the giver itself`() {
        // Then
        shouldThrow<NotAPartyMember> { giveItem(giverBattleUnitId = "knight-1", receiverBattleUnitId = "rat-1", lootableItemId = "health-potion") }
        shouldThrow<NotAPartyMember> { giveItem(giverBattleUnitId = "knight-1", receiverBattleUnitId = "knight-1", lootableItemId = "health-potion") }
    }

    @Test
    fun `should fail when the party does not explore, in a battle, or while talking to an npc`() {
        // Given
        give.battle()
        // Then
        shouldThrow<NotExploringTheParty> { giveItem(giverBattleUnitId = "knight-1", receiverBattleUnitId = "viking-1", lootableItemId = "health-potion") }
        // Given
        give.talkTo("merchant")
        // Then
        shouldThrow<NotExploringTheParty> { giveItem(giverBattleUnitId = "knight-1", receiverBattleUnitId = "viking-1", lootableItemId = "health-potion") }
    }

    @Test
    fun `should fail when the giver is no party member`() {
        // Then
        shouldThrow<NotExploringTheParty> { giveItem(giverBattleUnitId = "rat-1", receiverBattleUnitId = "viking-1", lootableItemId = "health-potion") }
    }
}
