package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.usecases.commands.UpdateBattlefieldOccupancy
import com.mkz.rpg.npc.domain.NpcEvent.NpcPlaced
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

// An npc blocks its tile as a battle unit does. The tile occupied resolves no step, as no one awaits the npc.
class OnNpcPlaced(
    updateBattlefieldOccupancy: UpdateBattlefieldOccupancy,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<NpcPlaced> { event ->
            updateBattlefieldOccupancy(
                row = event.row,
                column = event.column,
                occupantId = event.npcId,
            )
        }
}
