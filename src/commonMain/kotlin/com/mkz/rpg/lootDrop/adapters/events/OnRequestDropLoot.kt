package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.lootDrop.domain.LootDropEvent
import com.mkz.rpg.lootDrop.usecases.commands.DropLoot
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestDropLoot(
    dropLoot: DropLoot,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<LootDropEvent.RequestDropLoot> { event ->
            dropLoot(
                unitId = event.unitId,
                row = event.row,
                column = event.column,
            )
        }
}
