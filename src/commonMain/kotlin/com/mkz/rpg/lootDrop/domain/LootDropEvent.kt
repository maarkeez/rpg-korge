package com.mkz.rpg.lootDrop.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface LootDropEvent : DomainEvent {
    data class LootDropCreated(
        val unitId: String,
    ) : LootDropEvent

    object RequestInitialiseLootDrops : LootDropEvent

    // Requested for an item stored by an inventory, which leaves the tile at row and column it was lying on.
    data class RequestPickUpDroppedItem(
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : LootDropEvent

    /** An item was dropped on the tile at row and column, where it lies until an inventory stores it. */
    data class LootDropped(
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : LootDropEvent

    /** A battle unit stepped on a tile with dropped items, listed in the order they were dropped. They stay on the tile until an inventory stores them. */
    data class LootFound(
        val battleUnitId: String,
        val lootableItemIds: List<String>,
        val row: Int,
        val column: Int,
    ) : LootDropEvent

    data class DroppedItemPickedUp(
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : LootDropEvent
}
