package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.usecases.commands.RemoveEquippedEffects
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestRemoveEquippedEffects(
    private val removeEquippedEffects: RemoveEquippedEffects,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestRemoveEquippedEffects> { event ->
            removeEquippedEffects(battleUnitId = event.battleUnitId, lootableItemId = event.lootableItemId)
        }
}
