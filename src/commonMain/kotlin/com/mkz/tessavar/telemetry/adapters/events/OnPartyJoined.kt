package com.mkz.tessavar.telemetry.adapters.events

import com.mkz.tessavar.party.domain.PartyEvent.PartyJoined
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.telemetry.usecases.commands.RecordMemberRecruited

class OnPartyJoined(
    recordMemberRecruited: RecordMemberRecruited,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<PartyJoined> { event ->
            recordMemberRecruited(unitId = event.unitId)
        }
}
