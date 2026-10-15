package com.mkz.rpg.lootTable.domain

sealed class LootTableError(
    message: String,
) : Throwable(message = message) {
    class EmptyLootTableEntries : LootTableError("At least one loot table entry is required")

    class NonPositiveLootTableWeight : LootTableError("Loot table entry weight must be greater than 0")

    class DuplicatedLootTableEntry : LootTableError("Loot table entries must not repeat a lootable item nor nothing")

    class InvalidLootTableEntryType : LootTableError("Invalid loot table entry type")

    class LootTableLootableItemDoesNotExist : LootTableError("Loot table lootable item does not exist")
}
