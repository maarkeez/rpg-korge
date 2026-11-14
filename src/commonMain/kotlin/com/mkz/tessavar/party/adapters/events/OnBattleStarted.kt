package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.party.usecases.commands.InterruptExploration
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnBattleStarted(
    interruptExploration: InterruptExploration,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.BattleStarted> {
            interruptExploration()
        }
}
