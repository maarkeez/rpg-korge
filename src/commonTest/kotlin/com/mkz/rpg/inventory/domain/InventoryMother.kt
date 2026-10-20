package com.mkz.rpg.inventory.domain

import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import korlibs.io.util.UUID

object InventoryMother {
    fun inventory(
        battleUnitId: String = battleUnitId(),
        storedItems: List<String> = emptyList(),
        equippedItems: Map<EquipmentZoneDto, String> = emptyMap(),
        gold: Int = 0,
    ): Inventory =
        storedItems
            .fold(Inventory.create(battleUnitId, gold = gold)) { inventory, lootableItemId -> inventory.store(lootableItemId, row = 0, column = 0) }
            .let { inventory -> equippedItems.entries.fold(inventory) { equipped, (zone, lootableItemId) -> equipped.equip(lootableItemId, zone) } }
            .pullEvents()
            .second

    fun fullInventory(
        battleUnitId: String = battleUnitId(),
        gold: Int = 0,
    ): Inventory = inventory(battleUnitId = battleUnitId, storedItems = List(12) { lootableItemId() }, gold = gold)

    fun battleUnitId(): String = "inventory-battle-unit-" + UUID.randomUUID().toString()

    fun lootableItemId(): String = "inventory-lootable-item-" + UUID.randomUUID().toString()
}
