package com.mkz.rpg.lootDrop.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface LootDropEvent : DomainEvent {
    data class LootDropCreated(
        val unitId: String,
    ) : LootDropEvent

    object RequestInitialiseLootDrops : LootDropEvent

    data class LootDropped(
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : LootDropEvent
}
