package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.party.domain.PartyEvent.RequestStartTalk
import com.mkz.tessavar.party.usecases.commands.StartTalk
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestStartTalk(
    startTalk: StartTalk,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestStartTalk> { event ->
            startTalk(npcId = event.npcId)
        }
}
