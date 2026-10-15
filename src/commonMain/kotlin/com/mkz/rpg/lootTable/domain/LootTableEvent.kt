package com.mkz.rpg.lootTable.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface LootTableEvent : DomainEvent {
    data class LootTableCreated(
        val lootTableId: String,
    ) : LootTableEvent

    object RequestInitialiseLootTables : LootTableEvent
}
