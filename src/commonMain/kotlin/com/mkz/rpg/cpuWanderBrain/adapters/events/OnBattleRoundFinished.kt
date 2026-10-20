package com.mkz.rpg.cpuWanderBrain.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.cpuWanderBrain.usecases.commands.WanderAfterRound
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleRoundFinished(
    wanderAfterRound: WanderAfterRound,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleEvent.BattleRoundFinished> { event ->
            wanderAfterRound(battleUnitIds = event.battleUnitIds)
        }
}
