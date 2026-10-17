package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.cpuBrain.usecases.commands.UpdateEngagement
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleRoundFinished(
    updateEngagement: UpdateEngagement,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleEvent.BattleRoundFinished> { event ->
            updateEngagement(battleUnitIds = event.battleUnitIds)
        }
}
