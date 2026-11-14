package com.mkz.tessavar.battle.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battle.usecases.commands.LeaveBattle
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestLeaveBattle(
    leaveBattle: LeaveBattle,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.RequestLeaveBattle> { event ->
            leaveBattle(event.battleUnitId)
        }
}
