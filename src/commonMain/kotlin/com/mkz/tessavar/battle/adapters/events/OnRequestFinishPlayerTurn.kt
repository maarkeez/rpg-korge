package com.mkz.tessavar.battle.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battle.usecases.commands.FinishPlayerTurn
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestFinishPlayerTurn(
    private val finishPlayerTurn: FinishPlayerTurn,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.RequestFinishPlayerTurn> {
            finishPlayerTurn()
        }
}
