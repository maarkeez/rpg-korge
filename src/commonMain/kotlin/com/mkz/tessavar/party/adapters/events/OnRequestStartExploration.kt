package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.party.usecases.commands.StartExploration
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestStartExploration(
    startExploration: StartExploration,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<PartyEvent.RequestStartExploration> {
            startExploration()
        }
}
