package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.party.domain.PartyEvent.RequestJoinParty
import com.mkz.rpg.party.usecases.commands.JoinParty
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestJoinParty(
    joinParty: JoinParty,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestJoinParty> { event ->
            joinParty(battleUnitId = event.battleUnitId)
        }
}
