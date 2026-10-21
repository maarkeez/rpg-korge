package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.party.domain.PartyEvent.RequestStartTalk
import com.mkz.rpg.party.usecases.commands.StartTalk
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestStartTalk(
    startTalk: StartTalk,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestStartTalk> { event ->
            startTalk(npcId = event.npcId)
        }
}
