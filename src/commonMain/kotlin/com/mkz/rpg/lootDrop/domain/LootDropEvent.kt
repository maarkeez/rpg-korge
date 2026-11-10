package com.mkz.rpg.lootDrop.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface LootDropEvent : DomainEvent {
    data class LootDropCreated(
        val unitId: String,
    ) : LootDropEvent

    object RequestInitialiseLootDrops : LootDropEvent

    data class RequestLootDroppedItem(
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : LootDropEvent

    data class RequestDropItems(
        val lootableItemIds: List<String>,
        val row: Int,
        val column: Int,
    ) : LootDropEvent

    data class ItemDropped(
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : LootDropEvent

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
