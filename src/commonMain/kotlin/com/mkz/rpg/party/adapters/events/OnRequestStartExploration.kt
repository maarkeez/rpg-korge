package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.usecases.commands.StartExploration
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestStartExploration(
    startExploration: StartExploration,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<PartyEvent.RequestStartExploration> {
            startExploration()
        }
}
