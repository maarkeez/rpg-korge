package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battlefield.usecases.commands.TriggerTerrainEffects
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleRoundStarted(
    private val triggerTerrainEffects: TriggerTerrainEffects,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.BattleRoundStarted> { event ->
            triggerTerrainEffects(event.battleUnitIds)
        }
}
