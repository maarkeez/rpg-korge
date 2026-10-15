package com.mkz.rpg.lootableItem.domain

sealed class LootableItemError(
    message: String,
) : Throwable(message = message) {
    class InvalidLootType : LootableItemError("Invalid loot type")

    class LootableItemEffectDoesNotExist : LootableItemError("Lootable item effect does not exist")
}
