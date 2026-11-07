package com.mkz.rpg.lootDrop.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface LootDropEvent : DomainEvent {
    data class LootDropCreated(
        val unitId: String,
    ) : LootDropEvent

    object RequestInitialiseLootDrops : LootDropEvent

    // Requested for an item a party member looted, which leaves the tile at row and column it was lying on.
    data class RequestLootDroppedItem(
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : LootDropEvent

    // Requested for the items an inventory had no space for, which lie on the tile at row and column, in the given order, with no unit that dropped them.
    data class RequestDropItems(
        val lootableItemIds: List<String>,
        val row: Int,
        val column: Int,
    ) : LootDropEvent

    /** An item was dropped on the tile at row and column, where it lies until a party member loots it. */
    data class ItemDropped(
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : LootDropEvent

    /** A battle unit found dropped items on a tile, listed in the order they were dropped. They stay on the tile until a party member loots them. */
    data class DroppedItemsFound(
        val battleUnitId: String,
        val lootableItemIds: List<String>,
        val row: Int,
        val column: Int,
    ) : LootDropEvent

    data class DroppedItemLooted(
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : LootDropEvent
}
