package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.shared.domain.EventBus

class RequestLeaveBattle(
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitId: String) {
        eventBus.publish(BattleEvent.RequestLeaveBattle(battleUnitId = battleUnitId))
    }
}
