package com.mkz.tessavar.battlefield.adapters.events

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.battlefield.usecases.commands.ApplyTerrainEffects
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestTerrainEffects(
    private val applyTerrainEffects: ApplyTerrainEffects,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattlefieldEvent.RequestTerrainEffects> { event ->
            applyTerrainEffects(event.occupantIds)
        }
}
