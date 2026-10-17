package com.mkz.rpg.wandering.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.wandering.usecases.commands.WanderAfterRound

class OnBattleRoundFinished(
    wanderAfterRound: WanderAfterRound,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleEvent.BattleRoundFinished> { event ->
            wanderAfterRound(battleUnitIds = event.battleUnitIds)
        }
}
