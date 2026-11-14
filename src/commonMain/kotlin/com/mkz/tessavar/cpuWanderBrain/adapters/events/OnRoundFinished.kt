package com.mkz.tessavar.cpuWanderBrain.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.WanderAfterRound
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRoundFinished(
    wanderAfterRound: WanderAfterRound,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleEvent.RoundFinished> { event ->
            wanderAfterRound(battleUnitIds = event.battleUnitIds)
        }
}
