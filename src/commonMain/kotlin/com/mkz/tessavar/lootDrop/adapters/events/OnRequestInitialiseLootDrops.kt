package com.mkz.tessavar.lootDrop.adapters.events

import com.mkz.tessavar.lootDrop.domain.LootDropEvent
import com.mkz.tessavar.lootDrop.usecases.commands.InitialiseLootDrops
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestInitialiseLootDrops(
    initialiseLootDrops: InitialiseLootDrops,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<LootDropEvent.RequestInitialiseLootDrops> {
            initialiseLootDrops()
        }
}
