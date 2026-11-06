package com.mkz.rpg.inventory.domain

sealed class InventoryError(
    message: String,
) : Throwable(message = message) {
    class InventoryLootableItemDoesNotExist : InventoryError("Inventory lootable item does not exist")

    class ItemNotStored : InventoryError("The item is not stored in the inventory")

    class LootableItemNotConsumable : InventoryError("The lootable item is not consumable")

    class AbilityCanNotBeLearned : InventoryError("The battle unit already knows the ability the item teaches, or can not know more abilities")

    class LootableItemNotEquippable : InventoryError("The lootable item is not equippable")

    class ItemAlreadyEquipped : InventoryError("The item is already equipped")

    class ItemNotEquipped : InventoryError("The item is not equipped")

    class EquippedItemCanNotBeDiscarded : InventoryError("The item must be unequipped before discarding it")

    class NegativeGold : InventoryError("The inventory gold is negative")

    class GoldAboveMaximum : InventoryError("The inventory gold is above its maximum gold")

    class NotEnoughGold : InventoryError("The inventory gold is not enough to pay or give the amount")

    class InvalidGoldAmount : InventoryError("The gold given must be at least 1")

    class NoStorageSpaceLeft : InventoryError("The inventory has no storage space left")

    class EquippedItemCanNotBeSold : InventoryError("The item must be unequipped before selling it")

    class NotTradingWithAMerchant : InventoryError("The battle unit can only trade as a party member, while the party talks to a merchant")

    class ItemNotSoldByTheMerchant : InventoryError("The merchant does not sell the item")

    class EquippedItemCanNotBeGiven : InventoryError("The item must be unequipped before giving it")

    class NotExploringTheParty : InventoryError("The items and the gold can only be given while the party explores")

    class NotAPartyMember : InventoryError("The inventory can only be used by a party member")

    class NotTheBattleUnitPlayerTurn : InventoryError("The inventory can only be used during the player turn of its battle unit's player")
}
