package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.usecases.commands.FinishPlayerTurn
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestFinishPlayerTurn(
    private val finishPlayerTurn: FinishPlayerTurn,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.RequestFinishPlayerTurn> {
            finishPlayerTurn()
        }
}
