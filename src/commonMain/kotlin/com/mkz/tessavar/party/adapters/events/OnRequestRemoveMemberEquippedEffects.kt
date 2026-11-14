package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.party.domain.PartyEvent.RequestRemoveMemberEquippedEffects
import com.mkz.tessavar.party.usecases.commands.RemoveMemberEquippedEffects
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestRemoveMemberEquippedEffects(
    removeMemberEquippedEffects: RemoveMemberEquippedEffects,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestRemoveMemberEquippedEffects> { event ->
            removeMemberEquippedEffects(battleUnitId = event.battleUnitId, lootableItemId = event.lootableItemId)
        }
}
