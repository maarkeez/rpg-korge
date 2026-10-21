package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.party.domain.PartyEvent.RequestRemoveMemberEquippedEffects
import com.mkz.rpg.party.usecases.commands.RemoveMemberEquippedEffects
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestRemoveMemberEquippedEffects(
    removeMemberEquippedEffects: RemoveMemberEquippedEffects,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestRemoveMemberEquippedEffects> { event ->
            removeMemberEquippedEffects(battleUnitId = event.battleUnitId, lootableItemId = event.lootableItemId)
        }
}
