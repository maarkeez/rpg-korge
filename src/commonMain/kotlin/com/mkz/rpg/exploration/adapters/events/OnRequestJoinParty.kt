package com.mkz.rpg.exploration.adapters.events

import com.mkz.rpg.exploration.domain.ExplorationEvent.RequestJoinParty
import com.mkz.rpg.exploration.usecases.commands.JoinParty
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
