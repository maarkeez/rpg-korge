package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.party.domain.PartyEvent.RequestJoinParty
import com.mkz.tessavar.party.usecases.commands.JoinParty
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestJoinParty(
    joinParty: JoinParty,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestJoinParty> { event ->
            joinParty(battleUnitId = event.battleUnitId)
        }
}
