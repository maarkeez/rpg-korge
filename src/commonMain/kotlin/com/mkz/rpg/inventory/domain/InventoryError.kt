package com.mkz.rpg.inventory.domain

sealed class InventoryError(
    message: String,
) : Throwable(message = message) {
    class InventoryBattleUnitDoesNotExist : InventoryError("Inventory battle unit does not exist")

    class InventoryLootableItemDoesNotExist : InventoryError("Inventory lootable item does not exist")

    class InventoryItemNotFound : InventoryError("The item is not stored in the inventory")

    class LootableItemNotConsumable : InventoryError("The lootable item is not consumable")

    class LootableItemNotEquippable : InventoryError("The lootable item is not equippable")

    class ItemAlreadyEquipped : InventoryError("The item is already equipped")

    class ItemNotEquipped : InventoryError("The item is not equipped")

    class EquippedItemCanNotBeDiscarded : InventoryError("The item must be unequipped before discarding it")

    class NotTheBattleUnitPlayerTurn : InventoryError("The inventory can only be used during the turn of its battle unit player")
}
