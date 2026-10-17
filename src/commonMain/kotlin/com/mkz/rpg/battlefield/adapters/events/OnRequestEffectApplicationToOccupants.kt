package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.usecases.commands.RequestEffectApplicationToOccupants
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestEffectApplicationToOccupants(
    private val requestEffectApplicationToOccupants: RequestEffectApplicationToOccupants,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattlefieldEvent.RequestEffectApplicationToOccupants> { event ->
            requestEffectApplicationToOccupants(event.battleUnitIds)
        }
}
