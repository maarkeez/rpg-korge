package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.party.usecases.commands.InterruptExploration
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleStarted(
    interruptExploration: InterruptExploration,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.BattleStarted> {
            interruptExploration()
        }
}
