package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.lootDrop.domain.LootDropEvent.RequestFindLoot
import com.mkz.rpg.lootDrop.usecases.commands.FindLoot
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestFindLoot(
    findLoot: FindLoot,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestFindLoot> { event ->
            // A teleport's path is only its destination.
            findLoot(battleUnitId = event.battleUnitId, path = event.path)
        }
}
