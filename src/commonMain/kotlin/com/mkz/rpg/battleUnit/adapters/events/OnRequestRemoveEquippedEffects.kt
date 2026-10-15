package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.RemoveEquippedEffects
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestRemoveEquippedEffects(
    private val removeEquippedEffects: RemoveEquippedEffects,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestRemoveEquippedEffects> { event ->
            removeEquippedEffects(battleUnitId = event.battleUnitId, lootableItemId = event.lootableItemId)
        }
}
