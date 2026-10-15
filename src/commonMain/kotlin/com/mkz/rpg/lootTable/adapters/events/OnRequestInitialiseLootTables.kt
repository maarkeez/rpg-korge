package com.mkz.rpg.lootTable.adapters.events

import com.mkz.rpg.lootTable.domain.LootTableEvent
import com.mkz.rpg.lootTable.usecases.commands.InitialiseLootTables
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestInitialiseLootTables(
    initialiseLootTables: InitialiseLootTables,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<LootTableEvent.RequestInitialiseLootTables> {
            initialiseLootTables()
        }
}
