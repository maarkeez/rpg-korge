package com.mkz.rpg.lootDrop.domain

sealed class LootDropError(
    message: String,
) : Throwable(message = message) {
    class EmptyLootDropLootTables : LootDropError("At least one loot drop loot table is required")

    class DuplicatedLootDropLootTable : LootDropError("Loot drop loot tables must not be repeated")

    class LootDropUnitDoesNotExist : LootDropError("Loot drop unit does not exist")

    class LootDropLootTableDoesNotExist : LootDropError("Loot drop loot table does not exist")
}
