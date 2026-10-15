package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.lootDrop.domain.LootDropEvent
import com.mkz.rpg.lootDrop.usecases.commands.InitialiseLootDrops
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestInitialiseLootDrops(
    initialiseLootDrops: InitialiseLootDrops,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<LootDropEvent.RequestInitialiseLootDrops> {
            initialiseLootDrops()
        }
}
