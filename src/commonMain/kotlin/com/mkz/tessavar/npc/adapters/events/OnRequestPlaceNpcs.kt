package com.mkz.tessavar.npc.adapters.events

import com.mkz.tessavar.npc.domain.NpcEvent
import com.mkz.tessavar.npc.usecases.commands.PlaceNpcs
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestPlaceNpcs(
    placeNpcs: PlaceNpcs,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<NpcEvent.RequestPlaceNpcs> {
            placeNpcs()
        }
}
