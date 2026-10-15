package com.mkz.rpg.inventory.domain

sealed class InventoryError(
    message: String,
) : Throwable(message = message) {
    class InventoryBattleUnitDoesNotExist : InventoryError("Inventory battle unit does not exist")

    class InventoryLootableItemDoesNotExist : InventoryError("Inventory lootable item does not exist")

    class InventoryItemNotFound : InventoryError("The item is not stored in the inventory")

    class LootableItemNotConsumable : InventoryError("The lootable item is not consumable")

    class NotTheBattleUnitPlayerTurn : InventoryError("The inventory can only be used during the turn of its battle unit player")
}
