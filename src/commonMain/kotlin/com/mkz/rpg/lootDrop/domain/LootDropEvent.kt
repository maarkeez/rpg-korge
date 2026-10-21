package com.mkz.rpg.lootDrop.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface LootDropEvent : DomainEvent {
    data class LootDropCreated(
        val unitId: String,
    ) : LootDropEvent

    object RequestInitialiseLootDrops : LootDropEvent

    // Requested for a defeated battle unit, which drops the loot of its unit on the tile where it was defeated.
    data class RequestDropLoot(
        val unitId: String,
        val row: Int,
        val column: Int,
    ) : LootDropEvent

    data class LootDropped(
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : LootDropEvent
}
