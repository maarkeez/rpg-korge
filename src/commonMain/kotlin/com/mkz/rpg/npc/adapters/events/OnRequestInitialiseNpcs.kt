package com.mkz.rpg.npc.adapters.events

import com.mkz.rpg.npc.domain.NpcEvent
import com.mkz.rpg.npc.usecases.commands.InitialiseNpcs
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestInitialiseNpcs(
    initialiseNpcs: InitialiseNpcs,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<NpcEvent.RequestInitialiseNpcs> {
            initialiseNpcs()
        }
}
