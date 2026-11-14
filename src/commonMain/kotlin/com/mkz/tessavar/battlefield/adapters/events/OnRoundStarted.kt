package com.mkz.tessavar.battlefield.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battlefield.usecases.commands.ApplyTerrainEffects
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRoundStarted(
    private val applyTerrainEffects: ApplyTerrainEffects,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.RoundStarted> { event ->
            applyTerrainEffects(event.battleUnitIds)
        }
}
