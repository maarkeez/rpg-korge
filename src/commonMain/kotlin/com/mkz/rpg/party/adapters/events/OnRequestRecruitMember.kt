package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.party.domain.PartyEvent.RequestRecruitMember
import com.mkz.rpg.party.usecases.commands.RecruitMember
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestRecruitMember(
    recruitMember: RecruitMember,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestRecruitMember> { event ->
            recruitMember(battleUnitId = event.battleUnitId, unitId = event.unitId, row = event.row, column = event.column)
        }
}
