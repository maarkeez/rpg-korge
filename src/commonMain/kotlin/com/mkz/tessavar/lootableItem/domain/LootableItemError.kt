package com.mkz.tessavar.lootableItem.domain

sealed class LootableItemError(
    message: String,
) : Throwable(message = message) {
    class InvalidLootType : LootableItemError("Invalid loot type")

    class NegativePrice : LootableItemError("Lootable item price is negative")

    class PriceAboveLimit : LootableItemError("Lootable item price is higher than 999")

    class LootableItemEffectDoesNotExist : LootableItemError("Lootable item effect does not exist")

    class ConsumableEffectTimedWhileEquipped : LootableItemError("A consumable lootable item effect can not be timed while equipped")

    class EquippableEffectNotTimedWhileEquipped : LootableItemError("An equippable lootable item effect must be timed while equipped")
}
