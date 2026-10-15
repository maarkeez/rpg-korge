package com.mkz.rpg.inventory.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface InventoryEvent : DomainEvent {
    data class ItemStored(
        val battleUnitId: String,
        val lootableItemId: String,
    ) : InventoryEvent

    /** The item was not stored: the player chooses whether to discard an item to store it, or to leave it behind. */
    data class InventoryFull(
        val battleUnitId: String,
        val lootableItemId: String,
    ) : InventoryEvent

    data class ItemConsumed(
        val battleUnitId: String,
        val lootableItemId: String,
    ) : InventoryEvent

    data class ItemDiscarded(
        val battleUnitId: String,
        val lootableItemId: String,
    ) : InventoryEvent
}
