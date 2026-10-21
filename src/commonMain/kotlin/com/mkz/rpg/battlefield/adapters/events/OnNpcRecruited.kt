package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.usecases.commands.RemoveOccupant
import com.mkz.rpg.npc.domain.NpcEvent.NpcRecruited
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

// A recruited npc leaves its tile, so the battle unit it joins the party as can be deployed on it.
class OnNpcRecruited(
    removeOccupant: RemoveOccupant,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<NpcRecruited> { event ->
            removeOccupant(occupantId = event.npcId)
        }
}
