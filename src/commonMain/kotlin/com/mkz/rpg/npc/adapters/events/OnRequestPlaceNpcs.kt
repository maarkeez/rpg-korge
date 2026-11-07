package com.mkz.rpg.npc.adapters.events

import com.mkz.rpg.npc.domain.NpcEvent
import com.mkz.rpg.npc.usecases.commands.PlaceNpcs
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestPlaceNpcs(
    placeNpcs: PlaceNpcs,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<NpcEvent.RequestPlaceNpcs> {
            placeNpcs()
        }
}
