package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.party.domain.PartyEvent.RequestApplyMemberEffect
import com.mkz.rpg.party.usecases.commands.ApplyMemberEffect
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestApplyMemberEffect(
    applyMemberEffect: ApplyMemberEffect,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestApplyMemberEffect> { event ->
            applyMemberEffect(battleUnitId = event.battleUnitId, lootableItemId = event.lootableItemId, effectId = event.effectId)
        }
}
