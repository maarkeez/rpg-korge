package com.mkz.rpg.telemetry.adapters.events

import com.mkz.rpg.party.domain.PartyEvent.PartyJoined
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.telemetry.usecases.commands.RecordMemberRecruited

class OnPartyJoined(
    recordMemberRecruited: RecordMemberRecruited,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<PartyJoined> { event ->
            recordMemberRecruited(unitId = event.unitId)
        }
}
