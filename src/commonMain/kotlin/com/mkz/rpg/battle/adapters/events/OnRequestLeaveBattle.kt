package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.usecases.commands.LeaveBattle
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestLeaveBattle(
    leaveBattle: LeaveBattle,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.RequestLeaveBattle> { event ->
            leaveBattle(event.battleUnitId)
        }
}
