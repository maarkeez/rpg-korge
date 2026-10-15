package com.mkz.rpg.inventory.domain

import korlibs.io.util.UUID

object InventoryMother {
    fun inventory(
        battleUnitId: String = battleUnitId(),
        storedItems: List<String> = emptyList(),
    ): Inventory =
        storedItems
            .fold(Inventory.create(battleUnitId)) { inventory, lootableItemId -> inventory.store(lootableItemId) }
            .pullEvents()
            .second

    fun fullInventory(battleUnitId: String = battleUnitId()): Inventory = inventory(battleUnitId = battleUnitId, storedItems = List(12) { lootableItemId() })

    fun battleUnitId(): String = "inventory-battle-unit-" + UUID.randomUUID().toString()

    fun lootableItemId(): String = "inventory-lootable-item-" + UUID.randomUUID().toString()
}
