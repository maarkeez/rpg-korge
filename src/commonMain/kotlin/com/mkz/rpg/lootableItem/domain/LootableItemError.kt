package com.mkz.rpg.lootableItem.domain

sealed class LootableItemError(
    message: String,
) : Throwable(message = message) {
    class InvalidLootType : LootableItemError("Invalid loot type")

    class LootableItemEffectDoesNotExist : LootableItemError("Lootable item effect does not exist")

    class ConsumableEffectAppliedWhileEquipped : LootableItemError("A consumable lootable item effect can not be applied while equipped")

    class EquippableEffectNotAppliedWhileEquipped : LootableItemError("An equippable lootable item effect must be applied while equipped")
}
