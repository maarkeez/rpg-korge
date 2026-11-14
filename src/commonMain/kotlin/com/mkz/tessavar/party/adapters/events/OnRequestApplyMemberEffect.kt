package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.party.domain.PartyEvent.RequestApplyMemberEffect
import com.mkz.tessavar.party.usecases.commands.ApplyMemberEffect
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestApplyMemberEffect(
    applyMemberEffect: ApplyMemberEffect,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestApplyMemberEffect> { event ->
            applyMemberEffect(battleUnitId = event.battleUnitId, lootableItemId = event.lootableItemId, effectId = event.effectId)
        }
}
