package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.party.domain.PartyEvent.RequestRecruitMember
import com.mkz.tessavar.party.usecases.commands.RecruitMember
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestRecruitMember(
    recruitMember: RecruitMember,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestRecruitMember> { event ->
            recruitMember(battleUnitId = event.battleUnitId, unitId = event.unitId, row = event.row, column = event.column)
        }
}
