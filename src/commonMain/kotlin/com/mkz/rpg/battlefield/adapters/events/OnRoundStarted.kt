package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battlefield.usecases.commands.ApplyTerrainEffects
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRoundStarted(
    private val applyTerrainEffects: ApplyTerrainEffects,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.RoundStarted> { event ->
            applyTerrainEffects(event.battleUnitIds)
        }
}
